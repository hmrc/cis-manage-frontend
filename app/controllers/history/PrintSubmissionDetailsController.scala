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

package controllers.history

import controllers.actions.*
import pages.CisIdPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.SubmittedMonthlyReturnToPrintQuery
import services.{ManageService, SubmittedReturnsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.history.PrintSubmissionDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PrintSubmissionDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  manageService: ManageService,
  submittedReturnsService: SubmittedReturnsService,
  val controllerComponents: MessagesControllerComponents,
  view: PrintSubmissionDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    (request.userAnswers.get(SubmittedMonthlyReturnToPrintQuery), request.userAnswers.get(CisIdPage)) match {
      case (Some(monthlyReturnToPrint), Some(instanceId)) =>
        manageService
          .getSubmittedMonthlyReturnsData(
            instanceId,
            monthlyReturnToPrint.taxYear,
            monthlyReturnToPrint.taxYear,
            monthlyReturnToPrint.amendmentStatus.getOrElse("N")
          )
          .map { response =>
            val lang     = messagesApi.preferred(request).lang
            val viewData = submittedReturnsService.buildSubmittedReturnPrintViewModel(response, lang)
            Ok(view(viewData))
          }
          .recover { case ex =>
            logger.error("[PrintSubmissionDetailsController] Failed to get submitted monthly return", ex)
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      case _                                              =>
        logger.error("[PrintSubmissionDetailsController] SubmittedMonthlyReturnToPrintQuery or CisID is missing")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }
}
