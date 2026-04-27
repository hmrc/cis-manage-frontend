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

package services

import models.history.*
import models.response.GetSubmittedMonthlyReturnsDataResponse
import play.api.i18n.Lang
import utils.{DateTimeFormats, IrMarkReferenceGenerator, Utils}
import viewmodels.StatusViewModel.Text
import viewmodels.*

import java.time.format.DateTimeFormatter
import java.time.*
import javax.inject.{Inject, Singleton}

@Singleton
class SubmittedReturnsService @Inject() {

  private val ukTimezone: ZoneId                         = ZoneId.of("Europe/London")
  private val displayDateFormatter: DateTimeFormatter    = DateTimeFormatter.ofPattern("d MMM yyyy")
  private val shortMonthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
  private val amendmentCutOffInstant: Instant            = ZonedDateTime.of(2016, 2, 5, 0, 0, 0, 0, ZoneOffset.UTC).toInstant

  def buildAllYearsViewModel(data: SubmittedReturnsData): Option[SubmittedReturnsPageViewModel] =
    Some(
      SubmittedReturnsPageViewModel(
        taxYears = buildTaxYearSections(data),
        selectedTaxYear = None
      )
    )

  def buildSingleYearViewModel(data: SubmittedReturnsData, taxYear: String): Option[SubmittedReturnsPageViewModel] =
    taxYear.toIntOption.map { taxYearInt =>
      SubmittedReturnsPageViewModel(
        taxYears = buildTaxYearSections(data).filter(_.fromYear == taxYearInt),
        selectedTaxYear = Some(taxYear)
      )
    }

  def buildSubmittedReturnPrintViewModel(
    data: GetSubmittedMonthlyReturnsDataResponse,
    lang: Lang
  ): SubmittedReturnPrintViewModel = {
    val langCode = lang.code

    val submittedTime = data.submission.acceptedTime
      .map(_.atZone(ukTimezone))
      .map(_.format(DateTimeFormats.timeFormat()(lang)).toLowerCase)
      .getOrElse("")

    val submittedDate = data.submission.acceptedTime
      .map(_.atZone(ukTimezone))
      .map(_.format(DateTimeFormats.dateTimeFormat()(lang)))
      .getOrElse("")

    val receiptReferenceNumber = data.submission.hmrcMarkGgis.map(IrMarkReferenceGenerator.fromBase64).getOrElse("")

    val totalPaymentsMade    =
      Utils.formatCurrency(data.monthlyReturnItems.flatMap(_.totalPayments).map(BigDecimal(_)).sum)
    val totalCostOfMaterials =
      Utils.formatCurrency(data.monthlyReturnItems.flatMap(_.costOfMaterials).map(BigDecimal(_)).sum)
    val totalTaxDeducted     =
      Utils.formatCurrency(data.monthlyReturnItems.flatMap(_.totalDeducted).map(BigDecimal(_)).sum)
    val subcontractors       =
      data.monthlyReturnItems.map { item =>
        SubcontractorPayment(
          item.subcontractorName.getOrElse(""),
          Utils.formatCurrency(Utils.toBigDecimal(item.totalPayments)),
          Utils.formatCurrency(Utils.toBigDecimal(item.costOfMaterials)),
          Utils.formatCurrency(Utils.toBigDecimal(item.totalDeducted))
        )
      }

    SubmittedReturnPrintViewModel(
      monthYear = Utils.monthYear(data.taxYear, data.taxMonth, langCode),
      submittedTime = submittedTime,
      submittedDate = submittedDate,
      receiptReferenceNumber = receiptReferenceNumber,
      submissionType = if (data.nilReturnIndicator == "Y") {
        "standard"
      } else {
        "nil"
      },
      contractorName = data.scheme.name,
      payeReference = s"${data.scheme.taxOfficeNumber}/${data.scheme.taxOfficeReference}",
      totalPaymentsMade = totalPaymentsMade,
      totalCostOfMaterials = totalCostOfMaterials,
      totalTaxDeducted = totalTaxDeducted,
      subcontractors = subcontractors
    )
  }

  private def buildTaxYearSections(data: SubmittedReturnsData): Seq[TaxYearHistoryViewModel] = {
    val rowsWithTaxYear =
      data.monthlyReturns
        .sortBy(mr => (mr.taxYear, mr.taxMonth))(Ordering.Tuple2(Ordering.Int, Ordering.Int).reverse)
        .flatMap { monthlyReturn =>
          data.submissions
            .find(_.activeObjectId.contains(monthlyReturn.monthlyReturnId))
            .map { submission =>
              monthlyReturn.taxYear -> toRowViewModel(monthlyReturn, Some(submission))
            }
        }

    rowsWithTaxYear
      .groupBy(_._1)
      .toSeq
      .sortBy(_._1)(Ordering.Int.reverse)
      .map { case (fromYear, rows) =>
        TaxYearHistoryViewModel(
          fromYear = fromYear,
          toYear = fromYear + 1,
          rows = rows.map(_._2)
        )
      }
  }

  private def toRowViewModel(
    monthlyReturn: SubmittedMonthlyReturnData,
    submissionOpt: Option[SubmittedSubmissionData]
  ): SubmittedReturnsRowViewModel = {
    val periodEndText     = buildReturnPeriodEnd(monthlyReturn)
    val returnType        = buildReturnType(monthlyReturn)
    val dateSubmittedText = buildDateSubmittedText(submissionOpt)

    SubmittedReturnsRowViewModel(
      returnPeriodEnd = periodEndText,
      returnType = returnType,
      dateSubmitted = dateSubmittedText,
      monthlyReturn = LinkViewModel(
        url = "#", // TODO: F2 and F3 - replace with real route of page sr-04 Print monthly return
        hiddenText = periodEndText
      ),
      submissionReceipt = buildSubmissionReceipt(submissionOpt, periodEndText),
      status = buildStatus(monthlyReturn, submissionOpt)
    )
  }

  private def buildReturnPeriodEnd(monthlyReturn: SubmittedMonthlyReturnData): String =
    YearMonth
      .of(monthlyReturn.taxYear, monthlyReturn.taxMonth)
      .format(shortMonthYearFormatter)

  private def buildReturnType(monthlyReturn: SubmittedMonthlyReturnData): ReturnTypeViewModel =
    monthlyReturn.nilReturnIndicator match {
      case "Nil"      => ReturnTypeViewModel.Nil
      case "Standard" => ReturnTypeViewModel.Standard
      case _          => ReturnTypeViewModel.Unknown
    }

  private def buildDateSubmittedText(submissionOpt: Option[SubmittedSubmissionData]): String =
    submissionOpt
      .flatMap(_.acceptedTime)
      .map { instant =>
        instant.atZone(ukTimezone).toLocalDate.format(displayDateFormatter)
      }
      .getOrElse("")

  private def buildSubmissionReceipt(
    submissionOpt: Option[SubmittedSubmissionData],
    periodEndText: String
  ): StatusViewModel =
    if (isSubmissionReceiptAvailable(submissionOpt)) {
      StatusViewModel.Link(
        link = LinkViewModel(
          url = "#", // TODO: F2 and F3 - replace with real route of page SR-02-f View/save submitted return
          hiddenText = s"submission receipt for $periodEndText"
        ),
        textKey = "site.view",
        hiddenTextKey = "history.returnHistory.hidden.submissionReceipt"
      )
    } else {
      Text("")
    }

  private def isSubmissionReceiptAvailable(submissionOpt: Option[SubmittedSubmissionData]): Boolean =
    submissionOpt.exists { submission =>
      val irMarkSent     = submission.hmrcMarkGenerated
      val irMarkReceived = submission.hmrcMarkGgis

      irMarkSent.isDefined &&
      irMarkReceived.isDefined &&
      irMarkSent == irMarkReceived
    }

  private def buildStatus(
    monthlyReturn: SubmittedMonthlyReturnData,
    submissionOpt: Option[SubmittedSubmissionData]
  ): StatusViewModel = {
    val acceptedTimeOpt = submissionOpt.flatMap(_.acceptedTime)

    acceptedTimeOpt match {
      case None =>
        StatusViewModel.Text("history.returnHistory.status.notAvailable")

      case Some(acceptedTime) =>
        monthlyReturn.status match {
          case "SUBMITTED" =>
            if (isSuperseded(monthlyReturn)) {
              buildAmendmentStatus(monthlyReturn)
            } else if (!acceptedTime.isBefore(amendmentCutOffInstant)) {
              StatusViewModel.Text("history.returnHistory.status.amend")
            } else {
              StatusViewModel.Text("history.returnHistory.status.notAvailable")
            }

          case "SUBMITTED_NO_RECEIPT" =>
            StatusViewModel.Text("history.returnHistory.status.awaitingConfirmation")

          case _ =>
            StatusViewModel.Text("")
        }
    }
  }

  private def buildAmendmentStatus(monthlyReturn: SubmittedMonthlyReturnData): StatusViewModel =
    monthlyReturn.amendmentStatus match {
      case Some("STARTED") | Some("VALIDATED")                               =>
        StatusViewModel.Text("history.returnHistory.status.inProgress")
      case Some("PENDING") | Some("ACCEPTED") | Some("SUBMITTED_NO_RECEIPT") =>
        StatusViewModel.Text("history.returnHistory.status.awaitingConfirmation")
      case Some("SUBMITTED")                                                 =>
        StatusViewModel.Text("history.returnHistory.status.amend")
      case Some("DEPARTMENTAL_ERROR") | Some("FATAL_ERROR")                  =>
        StatusViewModel.Text("history.returnHistory.status.notAvailable")
      case _                                                                 => StatusViewModel.Text("")
    }

  private def isSuperseded(monthlyReturn: SubmittedMonthlyReturnData): Boolean =
    monthlyReturn.supersededBy.exists(_ > 0)

}
