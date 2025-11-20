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

package controllers

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import models.UserAnswers

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.ConstructionIndustrySchemeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IntroductionView

import scala.concurrent.{ExecutionContext, Future}

class IntroductionController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  sessionRepository: SessionRepository,
  cisService: ConstructionIndustrySchemeService,
  view: IntroductionView
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(view())
  }

  def affinityGroupRouting: Action[AnyContent] = identify.async { implicit request =>
    val userAnswers = UserAnswers(request.userId)

    sessionRepository.set(userAnswers).flatMap { _ =>
      if (request.isAgent) {
        cisService.startClientListRetrieval
          .map(redirectForStatus)
          .recover { case _ =>
            Redirect(controllers.routes.SystemErrorController.onPageLoad())
          }
      } else {
        Future.successful(
          Redirect(controllers.contractor.routes.ContractorLandingController.onPageLoad())
        )
      }
    }
  }

  private def redirectForStatus(status: String): Result =
    status match {
      case "succeeded"                          => Redirect(controllers.agent.routes.ClientListSearchController.onPageLoad())
      case "failed"                             => Redirect(controllers.agent.routes.FailedToRetrieveClientController.onPageLoad())
      case "system-error" | "initiate-download" => Redirect(controllers.routes.SystemErrorController.onPageLoad())
      case "in-progress"                        =>
        Redirect(controllers.agent.routes.RetrievingClientController.onPageLoad().url + "?RetryCount=1")
      case _                                    => Redirect(controllers.routes.SystemErrorController.onPageLoad())
    }

}
