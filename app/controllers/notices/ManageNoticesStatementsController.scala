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

package controllers.notices

import config.FrontendAppConfig
import controllers.actions.*
import pages.{AgentClientsPage, ContractorNamePage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.notices.{ManageNoticesStatementsPageViewModel, ManageNoticesStatementsRowViewModel}
import views.html.notices.ManageNoticesStatementsView

import javax.inject.Inject

class ManageNoticesStatementsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ManageNoticesStatementsView
)(implicit appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(instanceId: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      implicit val messages: Messages = messagesApi.preferred(request)

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

      val notices = Seq(
        ManageNoticesStatementsRowViewModel(
          date = "15 October 2025",
          noticeType = messages("manageNoticesStatements.noticeType.penaltyWarnings"),
          description = messages("manageNoticesStatements.description.penaltyWarning", "5 October 2025"),
          status = messages("manageNoticesStatements.status.unread"),
          statusColour = "govuk-tag--red",
          actionUrl = appConfig.noticesAndStatementsUrl
        ),
        ManageNoticesStatementsRowViewModel(
          date = "20 September 2025",
          noticeType = messages("manageNoticesStatements.noticeType.confirmationStatements"),
          description = messages("manageNoticesStatements.description.confirmation", "August 2025"),
          status = messages("manageNoticesStatements.status.read"),
          statusColour = "govuk-tag--blue",
          actionUrl = appConfig.noticesAndStatementsUrl
        ),
        ManageNoticesStatementsRowViewModel(
          date = "20 August 2025",
          noticeType = messages("manageNoticesStatements.noticeType.confirmationStatements"),
          description = messages("manageNoticesStatements.description.confirmation", "July 2025"),
          status = messages("manageNoticesStatements.status.read"),
          statusColour = "govuk-tag--blue",
          actionUrl = appConfig.noticesAndStatementsUrl
        )
      )

      val pageViewModel = ManageNoticesStatementsPageViewModel(notices)

      contractorNameOpt match {
        case Some(contractorName) =>
          Ok(view(contractorName, instanceId, pageViewModel))

        case None =>
          logger.warn(s"[ManageNoticesStatementsController] contractorName missing (isAgent=${request.isAgent})")
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
      }
  }
}
