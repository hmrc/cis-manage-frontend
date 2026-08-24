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

package controllers

import controllers.actions.*
import models.agent.{ClientListCheckReturnTarget, ClientListStatus}
import models.Mode
import play.api.Logging

import javax.inject.{Inject, Named}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import services.ConstructionIndustrySchemeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SecurityCheckView
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class SecurityCheckController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  @Named("AgentIdentifier") agentIdentify: IdentifierAction,
  cisService: ConstructionIndustrySchemeService,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: SecurityCheckView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val MaxRetries               = 2
  private val RefreshIntervalInSeconds = 15

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    Ok(view())
  }

  def onClientListCheck(
    returnTo: String,
    instanceId: Option[String],
    mode: Option[String]
  ): Action[AnyContent] =
    (agentIdentify andThen getData andThen requireData) { implicit request =>
      returnCall(returnTo, instanceId, mode) match {
        case Some(_) =>
          refreshResult(
            returnTo = returnTo,
            instanceId = instanceId,
            mode = mode,
            retryCount = 0
          )

        case None =>
          logger.warn("[SecurityCheckController] Invalid client list return target=$returnTo")
          systemError
      }
    }

  def pollClientListCheck(
    returnTo: String,
    instanceId: Option[String],
    mode: Option[String],
    retryCount: Int = 0
  ): Action[AnyContent] =
    (agentIdentify andThen getData andThen requireData).async { implicit request =>
      returnCall(returnTo, instanceId, mode) match {

        case None =>
          Future.successful(systemError)

        case Some(successfulCall) =>
          val nextRetry = retryCount + 1
          if nextRetry > MaxRetries then Future.successful(systemError)
          else
            cisService.getClientListStatus
              .map {
                case ClientListStatus.Succeeded                                  =>
                  Redirect(successfulCall)
                case ClientListStatus.InProgress                                 =>
                  refreshResult(
                    returnTo = returnTo,
                    instanceId = instanceId,
                    mode = mode,
                    retryCount = nextRetry
                  )
                case ClientListStatus.Failed | ClientListStatus.InitiateDownload =>
                  systemError
              }
              .recover { case NonFatal(e) =>
                logger.error(
                  s"[SecurityCheckController] Client list polling failed)",
                  e
                )
                systemError
              }
      }
    }

  private def refreshResult(
    returnTo: String,
    instanceId: Option[String],
    mode: Option[String],
    retryCount: Int
  )(implicit request: Request[?]): Result = {

    val refreshUrl = routes.SecurityCheckController
      .pollClientListCheck(returnTo, instanceId, mode, retryCount)
      .url

    Ok(view())
      .withHeaders("Refresh" -> s"$RefreshIntervalInSeconds; url=$refreshUrl")
  }

  private def returnCall(
    returnTo: String,
    instanceId: Option[String],
    mode: Option[String]
  ): Option[Call] =
    returnTo match {

      case ClientListCheckReturnTarget.AgentDashboard.key =>
        instanceId.map { id =>
          controllers.agent.routes.AgentLandingController.onPageLoad(id)
        }

      case ClientListCheckReturnTarget.FileMonthlyReturns.key =>
        Some(controllers.agent.routes.ClientListSearchController.onPageLoad())

      case ClientListCheckReturnTarget.ManageClientDetails.key =>
        Some(controllers.clientdetails.routes.ManageClientDetailsController.onPageLoad())

      case ClientListCheckReturnTarget.ChangeClientReference.key =>
        mode
          .flatMap(Mode.fromString)
          .map { parsedMode =>
            controllers.clientdetails.routes.ChangeClientReferenceController.onPageLoad(parsedMode)
          }

      case ClientListCheckReturnTarget.RemoveClient.key =>
        mode
          .flatMap(Mode.fromString)
          .map { parsedMode =>
            controllers.clientdetails.routes.RemoveClientYesNoController.onPageLoad(parsedMode)
          }
      case _                                            =>
        None
    }

  private def systemError: Result =
    Redirect(controllers.routes.SystemErrorController.onPageLoad())

}
