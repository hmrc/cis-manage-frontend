/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.actions

import controllers.actions.ClientListCheckRedirects.{systemError, unauthorised}
import models.audit.AuthFailureAuditEventModel
import models.requests.{DataRequest, IdentifierRequest}
import pages.{AgentClientsPage, CisIdPage}
import play.api.Logging
import play.api.mvc.*
import repositories.SessionRepository
import services.{AuditService, ConstructionIndustrySchemeService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class SchemeAuthorisationGuard @Inject() (
  cisService: ConstructionIndustrySchemeService,
  sessionRepository: SessionRepository,
  auditService: AuditService
)(using ec: ExecutionContext)
    extends Logging {

  def forInstanceId(instanceId: String): ActionFilter[DataRequest] =
    new ActionFilter[DataRequest] {

      override protected def executionContext: ExecutionContext = ec

      override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] =
        if request.isAgent then {
          checkAgentForInstanceId(request, instanceId, unauthorised)
        } else {
          val authorised = request.userAnswers.get(CisIdPage).contains(instanceId)
          Future.successful(
            if authorised then None
            else Some(unauthorised)
          )
        }
    }

  def validateCachedInstanceId(instanceId: String): ActionFilter[DataRequest] =
    new ActionFilter[DataRequest] {

      override protected def executionContext: ExecutionContext = ec

      override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] = {

        val authorised =
          if request.isAgent then {
            AgentClientsPage
              .findClient(request.userAnswers, instanceId)
              .isDefined
          } else {
            request.userAnswers.get(CisIdPage).contains(instanceId)
          }

        Future.successful(
          if authorised then None
          else Some(unauthorised)
        )
      }
    }

  def currentClient: ActionFilter[DataRequest] =
    new ActionFilter[DataRequest] {

      override protected def executionContext: ExecutionContext = ec

      override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] =
        request.userAnswers.get(CisIdPage) match {

          case None =>
            logger.warn(s"[SchemeAuthorisationGuard] CisId missing in UserAnswers")
            Future.successful(Some(systemError))

          case Some(_) if !request.isAgent =>
            Future.successful(None)

          case Some(instanceId) =>
            checkAgentForInstanceId(request, instanceId, systemError)
        }
    }

  private[actions] def checkCurrentAgentClient[A](request: IdentifierRequest[A]): Future[Option[Result]] =
    if !request.isAgent then {
      Future.successful(None)
    } else {

      sessionRepository
        .get(request.userId)
        .flatMap {
          case None =>
            logger.warn(s"[SchemeAuthorisationGuard] UserAnswers missing")
            Future.successful(Some(systemError))

          case Some(userAnswers) =>
            userAnswers.get(CisIdPage) match {
              case None =>
                logger.warn(s"[SchemeAuthorisationGuard] CisId missing in UserAnswers")
                Future.successful(Some(systemError))

              case Some(instanceId) =>
                AgentClientsPage.findClient(userAnswers, instanceId) match {
                  case None =>
                    logger.warn(s"[SchemeAuthorisationGuard] Client not found for instanceId: $instanceId")
                    Future.successful(Some(systemError))

                  case Some(client) =>
                    checkAgentClient(request, client.taxOfficeNumber, client.taxOfficeRef, instanceId)
                }
            }
        }
        .recover { case NonFatal(ex) =>
          logger.error(s"[SchemeAuthorisationGuard] Error checking current agent client", ex)
          Some(systemError)
        }
    }

  private def checkAgentForInstanceId[A](
    request: DataRequest[A],
    instanceId: String,
    clientNotFoundResult: Result
  ): Future[Option[Result]] =
    AgentClientsPage.findClient(request.userAnswers, instanceId) match {
      case None =>
        logger.warn(s"[SchemeAuthorisationGuard] Client not found for instanceId: $instanceId")
        Future.successful(Some(clientNotFoundResult))

      case Some(client) =>
        checkAgentClient(request, client.taxOfficeNumber, client.taxOfficeRef, instanceId)
    }

  private def checkAgentClient[A](
    request: Request[A],
    taxOfficeNumber: String,
    taxOfficeReference: String,
    instanceId: String
  ): Future[Option[Result]] = {

    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    given Request[?]    = request

    checkClient(taxOfficeNumber, taxOfficeReference, instanceId)

  }

  private def checkClient(
    taxOfficeNumber: String,
    taxOfficeReference: String,
    instanceId: String
  )(using HeaderCarrier, Request[?]): Future[Option[Result]] =
    if taxOfficeNumber.isEmpty || taxOfficeReference.isEmpty then {
      logger.warn(s"[SchemeAuthorisationGuard] Tax office number or reference is empty for instanceId: $instanceId")
      Future.successful(Some(systemError))
    } else {
      cisService
        .hasClient(taxOfficeNumber, taxOfficeReference)
        .flatMap {
          case true  =>
            Future.successful(None)
          case false =>
            logger.warn(s"[SchemeAuthorisationGuard] Agent does not have client for instanceId: $instanceId")
            auditService
              .sendEvent(AuthFailureAuditEventModel())
              .map(_ => Some(systemError))
              .recover { case NonFatal(ex) =>
                logger.error(s"[SchemeAuthorisationGuard] Error sending audit event for instanceId: $instanceId", ex)
                Some(systemError)
              }
        }
        .recover { case NonFatal(ex) =>
          logger.error(s"[SchemeAuthorisationGuard] Error checking client for instanceId: $instanceId", ex)
          Some(systemError)
        }
    }

}
