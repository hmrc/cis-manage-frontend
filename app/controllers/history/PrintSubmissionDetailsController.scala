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
import models.history.SubcontractorPayment
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.SubmittedMonthlyReturnToPrintQuery
import services.ManageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.IrMarkReferenceGenerator
import views.html.history.PrintSubmissionDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PrintSubmissionDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  service: ManageService,
  val controllerComponents: MessagesControllerComponents,
  view: PrintSubmissionDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(SubmittedMonthlyReturnToPrintQuery) match {
      case Some(monthlyReturnToPrint) =>
        service
          .getSubmittedMonthlyReturns(
            monthlyReturnToPrint.instanceId,
            monthlyReturnToPrint.taxYear,
            monthlyReturnToPrint.taxYear,
            monthlyReturnToPrint.amendment.getOrElse("N")
          )
          .map { response =>
            val langCode               = messagesApi.preferred(request).lang.code
            val monthYear              = monthlyReturnToPrint.monthYear(langCode) // "April 2026"
            val submittedTime          = "8:46am"
            val submittedDate          = "16 March 2025"
            val receiptReferenceNumber = response.receiptReferenceNumber
              .map(IrMarkReferenceGenerator.fromBase64)
              .getOrElse("") // "6QEDAHDREBY455GDNCPMDCNDFBDBJSJSJDNDDHDJDZ5"
            val submissionType         = response.returnType.toLowerCase // "Monthly return"
            val contractorName         = response.contractorName // "PAL 355 Scheme"
            val payeReference          = s"${response.taxOfficeNumber}/${response.taxOfficeReference}" // "123/AB456"
            val totalPaymentsMade      = "£1900"
            val totalCostOfMaterials   = "£616"
            val totalTaxDeducted       = "£380"
            val subcontractors         = Seq(
              SubcontractorPayment("BuildRight Construction", "£165", "£95", "£95"),
              SubcontractorPayment("Northern Trades Ltd", "£75", "£55", "£55"),
              SubcontractorPayment("TyneWear Ltd", "£165", "£125", "£55")
            )
            Ok(
              view(
                monthYear,
                submittedTime,
                submittedDate,
                receiptReferenceNumber,
                submissionType,
                contractorName,
                payeReference,
                totalPaymentsMade,
                totalCostOfMaterials,
                totalTaxDeducted,
                subcontractors
              )
            )
          }
          .recover { case ex =>
            logger.error("[PrintSubmissionDetailsController] Failed to get submitted monthly return", ex)
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      case _                          =>
        logger.error("[PrintSubmissionDetailsController] SubmittedMonthlyReturnToPrintQuery missing")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }
}
