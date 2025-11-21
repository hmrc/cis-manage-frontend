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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ConstructionIndustrySchemeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.agent.RetrievingClientView
import scala.concurrent.ExecutionContext

import javax.inject.{Inject, Named}

class RetrievingClientController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  @Named("AgentIdentifier") identify: IdentifierAction,
  cisService: ConstructionIndustrySchemeService,
  view: RetrievingClientView
)(implicit appConfig: config.FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    cisService.getClientListStatus.map {
      case "succeeded"         => Redirect(routes.ClientListSearchController.onPageLoad())
      case "failed"            => Redirect(routes.FailedToRetrieveClientController.onPageLoad())
      case "system-error"      => Redirect(controllers.routes.SystemErrorController.onPageLoad())
      case "initiate-download" => Redirect(controllers.routes.SystemErrorController.onPageLoad())
      case "in-progress"       => Ok(view())
      case _                   => Ok(view())
    }
  }
}
