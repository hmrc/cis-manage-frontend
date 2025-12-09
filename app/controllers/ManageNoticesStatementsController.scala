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
import controllers.actions._
import pages.ContractorNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{ManageNoticesStatementsPageViewModel, ManageNoticesStatementsRowViewModel}
import views.html.ManageNoticesStatementsView

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

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    implicit val messages: Messages = messagesApi.preferred(request)

    val contractorName = request.userAnswers.get(ContractorNamePage).getOrElse {
      logger.error("[ManageNoticesStatementsController] contractorName missing from userAnswers")
      throw new IllegalStateException("contractorName missing from userAnswers")
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

    Ok(view(contractorName, pageViewModel))
  }
}
