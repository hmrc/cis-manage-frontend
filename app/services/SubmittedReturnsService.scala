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

import models.UserAnswers
import models.history.{SubmittedMonthlyReturnData, SubmittedReturnsData, SubmittedSubmissionData}
import pages.history.SubmittedReturnsDataPage
import viewmodels.{LinkViewModel, ReturnTypeViewModel, StatusViewModel, SubmittedReturnsPageViewModel, SubmittedReturnsRowViewModel, TaxYearHistoryViewModel}
import viewmodels.StatusViewModel.Text

import java.time.format.DateTimeFormatter
import java.time.{YearMonth, ZoneId}
import javax.inject.{Inject, Singleton}

@Singleton
class SubmittedReturnsService @Inject() {

  private val ukTimezone: ZoneId                         = ZoneId.of("Europe/London")
  private val displayDateFormatter: DateTimeFormatter    = DateTimeFormatter.ofPattern("d MMM yyyy")
  private val shortMonthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

  def buildAllYearsViewModel(userAnswers: UserAnswers): Option[SubmittedReturnsPageViewModel] =
    userAnswers.get(SubmittedReturnsDataPage).map { data =>
      SubmittedReturnsPageViewModel(
        taxYears = buildTaxYearSections(data),
        selectedTaxYear = None
      )
    }

  def buildSingleYearViewModel(userAnswers: UserAnswers, taxYear: String): Option[SubmittedReturnsPageViewModel] =
    userAnswers.get(SubmittedReturnsDataPage).map { data =>
      SubmittedReturnsPageViewModel(
        taxYears = buildTaxYearSections(data).filter(_.fromYear == taxYear.toInt),
        selectedTaxYear = Some(taxYear)
      )
    }

  private def buildTaxYearSections(data: SubmittedReturnsData): Seq[TaxYearHistoryViewModel] = {
    val rowsWithTaxYear =
      data.monthlyReturn
        .sortBy(mr => (mr.taxYear, mr.taxMonth))(Ordering.Tuple2(Ordering.Int, Ordering.Int).reverse)
        .map { monthlyReturn =>
          val submission = data.submissions.find(_.activeObjectId == monthlyReturn.monthlyReturnId)
          monthlyReturn.taxYear -> toRowViewModel(monthlyReturn, submission)
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
      status = buildStatus(monthlyReturn)
    )
  }

  private def buildReturnPeriodEnd(monthlyReturn: SubmittedMonthlyReturnData): String =
    YearMonth
      .of(monthlyReturn.taxYear, monthlyReturn.taxMonth)
      .format(shortMonthYearFormatter)

  private def buildReturnType(monthlyReturn: SubmittedMonthlyReturnData): ReturnTypeViewModel =
    monthlyReturn.nilReturnIndicator match {
      case "Y" => ReturnTypeViewModel.Nil
      case "N" => ReturnTypeViewModel.Standard
      case _   => ReturnTypeViewModel.Unknown
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
      Text("site.view")
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
    monthlyReturn: SubmittedMonthlyReturnData
    // TODO add submissionOpt: Option[SubmittedSubmissionData]
  ): StatusViewModel =
    // TODO: implement F4 and F5 status mapping rules
    monthlyReturn.status match {
      case "SUBMITTED_NO_RECEIPT" => Text("history.returnHistory.status.awaitingConfirmation")
      case "SUBMITTED"            => Text("history.returnHistory.status.amend")
      case other                  => Text(other)
    }

}
