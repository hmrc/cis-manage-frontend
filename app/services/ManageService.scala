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
import models.requests.DataRequest
import models.{CisTaxpayerSearchResult, UserAnswers}
import org.apache.pekko.actor.typed.delivery.internal.ProducerControllerImpl.Request
import pages.*
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

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
            for {
              updatedUaWithCisId      <- Future.fromTry(ua.set(CisIdPage, cisId))
              updatedUaWithContractor <- Future.fromTry(updatedUaWithCisId.set(ContractorNamePage, contractorName))
              _                       <- sessionRepository.set(updatedUaWithContractor)
            } yield (cisId, updatedUaWithContractor)
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
}
