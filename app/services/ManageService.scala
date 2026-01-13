/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import connectors.ConstructionIndustrySchemeConnector
import models.{CisTaxpayerSearchResult, UnsubmittedMonthlyReturnsResponse, UserAnswers}
import pages.*
import play.api.Logging
import play.api.libs.json.Json
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.agent.AgentLandingViewModel

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ManageService @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends Logging {

  def resolveAndStoreCisId(ua: UserAnswers)(implicit hc: HeaderCarrier): Future[(String, UserAnswers)] =
    ua.get(CisIdPage) match {
      case Some(cisId) => Future.successful((cisId, ua))
      case None        =>
        logger.info("[resolveAndStoreCisId] cache-miss: fetching CIS taxpayer from backend")
        cisConnector.getCisTaxpayer().flatMap { tp =>
          logger.info(s"[resolveAndStoreCisId] taxpayer payload:\n${Json.prettyPrint(Json.toJson(tp))}")
          val cisId = tp.uniqueId.trim
          if (cisId.isEmpty) {
            Future.failed(new RuntimeException("Empty cisId (uniqueId) returned from /cis/taxpayer"))
          } else {
            val contractorName = tp.schemeName.getOrElse("")
            val employerRef    = s"${tp.taxOfficeNumber}/${tp.taxOfficeRef}"
            val utr            = tp.utr.getOrElse("")
            for {
              ua1 <- Future.fromTry(ua.set(CisIdPage, cisId))
              ua2 <- Future.fromTry(ua1.set(ContractorNamePage, contractorName))
              ua3 <- Future.fromTry(ua2.set(EmployerReferencePage, employerRef))
              ua4 <- Future.fromTry(ua3.set(UniqueTaxReferencePage, utr))
              _   <- sessionRepository.set(ua4)
            } yield (cisId, ua4)
          }
        }
    }

  def resolveAndStoreAgentClients(
    userAnswers: UserAnswers
  )(using HeaderCarrier): Future[(List[CisTaxpayerSearchResult], UserAnswers)] =
    userAnswers.get(AgentClientsPage) match {
      case Some(clientList) => Future.successful((clientList, userAnswers))
      case None             =>
        logger.info("[resolveAndStoreAgentClients] cache-miss: fetching agent clients from backend")
        for {
          clients        <- cisConnector.getAllClients
          updatedAnswers <- Future.fromTry(userAnswers.set(AgentClientsPage, clients))
          _              <- sessionRepository.set(updatedAnswers)
        } yield (clients, updatedAnswers)
    }

  def getAgentLandingData(
    uniqueId: String,
    ua: UserAnswers
  )(using HeaderCarrier): Future[AgentLandingViewModel] =
    ua.get(AgentClientsPage) match {
      case None =>
        Future.failed(new RuntimeException("AgentClientsPage missing in UserAnswers"))

      case Some(clients) =>
        clients.find(_.uniqueId == uniqueId) match {
          case None =>
            Future.failed(new RuntimeException(s"Client with uniqueId=$uniqueId not found in AgentClientsPage"))

          case Some(client) =>
            cisConnector.getAgentClientTaxpayer(client.taxOfficeNumber, client.taxOfficeRef).flatMap { taxpayer =>
              val utrOpt        = taxpayer.utr
              val updatedClient = client.copy(utr = utrOpt)
              val updatedList   =
                clients.map(c => if (c.uniqueId == client.uniqueId) updatedClient else c)

              for {
                updatedUa <- Future.fromTry(ua.set(AgentClientsPage, updatedList))
                _         <- sessionRepository.set(updatedUa)
              } yield AgentLandingViewModel(
                clientName = updatedClient.schemeName.getOrElse(""),
                employerRef = s"${updatedClient.taxOfficeNumber}/${updatedClient.taxOfficeRef}",
                utr = utrOpt
              )
            }
        }
    }

  def getUnsubmittedMonthlyReturns(instanceId: String)(implicit
    hc: HeaderCarrier
  ): Future[UnsubmittedMonthlyReturnsResponse] =
    cisConnector.getUnsubmittedMonthlyReturns(instanceId)
}
