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
import controllers.actions.*
import pages.{AgentClientsPage, ContractorNamePage}
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.ReturnLandingViewModel
import views.html.ReturnsLandingView

class ReturnsLandingController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ReturnsLandingView
)(implicit appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(instanceId: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val contractorNameOpt: Option[String] =
        if (request.isAgent) {
          for {
            clients <- request.userAnswers.get(AgentClientsPage)
            client  <- clients.find(_.uniqueId == instanceId)
            name    <- client.schemeName
          } yield name
        } else {
          request.userAnswers.get(ContractorNamePage)
        }

      contractorNameOpt match {
        case Some(contractorName) =>
          val returnsList = Seq(
            ReturnLandingViewModel("August 2025", "Standard", "19 September 2025", "Accepted"),
            ReturnLandingViewModel("July 2025", "Nil", "19 August 2025", "Accepted"),
            ReturnLandingViewModel("June 2025", "Standard", "18 July 2025", "Accepted")
          )

          Ok(view(contractorName, returnsList))

        case None =>
          logger.warn(s"[ReturnsLandingController] contractorName missing (isAgent=${request.isAgent})")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }
}
