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

package controllers.agent

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import services.ConstructionIndustrySchemeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.agent.RetrievingClientView

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.{Inject, Named}

class RetrievingClientController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  @Named("AgentIdentifier") identify: IdentifierAction,
  cisService: ConstructionIndustrySchemeService,
  view: RetrievingClientView
)(implicit appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val MaxRetries               = 8
  private val RefreshIntervalInSeconds = 15

  def onPageLoad: Action[AnyContent] = identify { implicit request =>
    Ok(view())
      .withHeaders("Refresh" -> s"0; url=${routes.RetrievingClientController.start().url}")
  }

  def start: Action[AnyContent] = identify.async { implicit request =>
    cisService.startClientListRetrieval
      .map {
        case "succeeded"   => Redirect(controllers.agent.routes.ClientListSearchController.onPageLoad())
        case "failed"      => Redirect(controllers.agent.routes.FailedToRetrieveClientController.onPageLoad())
        case "in-progress" => refreshResult(nextRetry = 1)
        case _             => Redirect(controllers.routes.SystemErrorController.onPageLoad())
      }
      .recover { case _ =>
        Redirect(controllers.routes.SystemErrorController.onPageLoad())
      }
  }

  def poll(retryCount: Int = 0): Action[AnyContent] = identify.async { implicit request =>
    val nextRetry = retryCount + 1

    if (nextRetry > MaxRetries) {
      Future.successful(
        Redirect(routes.FailedToRetrieveClientController.onPageLoad())
      )
    } else {
      cisService.getClientListStatus
        .map {
          case "succeeded"   => Redirect(routes.ClientListSearchController.onPageLoad())
          case "failed"      => Redirect(routes.FailedToRetrieveClientController.onPageLoad())
          case "in-progress" => refreshResult(nextRetry)
          case _             => Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
        .recover { case _ =>
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }
  }

  private def refreshResult(nextRetry: Int)(implicit request: Request[_]): Result = {
    val refreshUrl = routes.RetrievingClientController.poll(retryCount = nextRetry).url

    Ok(view())
      .withHeaders("Refresh" -> s"$RefreshIntervalInSeconds; url=$refreshUrl")
  }
}
