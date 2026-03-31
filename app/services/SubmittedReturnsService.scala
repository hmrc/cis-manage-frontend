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
import models.{SubmittedMonthlyReturnData, SubmittedReturnsData, SubmittedSubmissionData}
import pages.SubmittedReturnsDataPage
import viewmodels.{LinkViewModel, SubmittedReturnsPageViewModel, SubmittedReturnsRowViewModel, TaxYearHistoryViewModel}
import viewmodels.StatusViewModel.Text

import java.time.format.{DateTimeFormatter, TextStyle}
import java.time.{Month, ZoneId}
import java.util.Locale
import javax.inject.{Inject, Singleton}

@Singleton
class SubmittedReturnsService @Inject() {

  private val ukTimezone: ZoneId                      = ZoneId.of("Europe/London")
  private val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def buildAllYearsViewModel(userAnswers: UserAnswers): Option[SubmittedReturnsPageViewModel] =
    userAnswers.get(SubmittedReturnsDataPage).map { data =>
      SubmittedReturnsPageViewModel(
        taxYears = buildTaxYearSections(data),
        selectedTaxYear = None
      )
    }

  def buildSingleYearViewModel(userAnswers: UserAnswers, taxYear: String): Option[SubmittedReturnsPageViewModel] =
    userAnswers.get(SubmittedReturnsDataPage).map { data =>
      val allSections = buildTaxYearSections(data)

      SubmittedReturnsPageViewModel(
        taxYears = allSections.filter(_.taxYearCaption == s"Tax year $taxYear to ${taxYear.toInt + 1}"),
        selectedTaxYear = Some(taxYear)
      )
    }

  private def buildTaxYearSections(data: SubmittedReturnsData): Seq[TaxYearHistoryViewModel] = {
    val rowsWithTaxYear =
      data.monthlyReturn
        .sortBy(mr => (mr.taxYear, mr.taxMonth))(Ordering.Tuple2(Ordering.Int, Ordering.Int).reverse)
        .map { monthlyReturn =>
          val submission   = data.submissions.find(_.activeObjectId == monthlyReturn.monthlyReturnId)
          val taxYearLabel = s"${monthlyReturn.taxYear} to ${monthlyReturn.taxYear + 1}"
          taxYearLabel -> toRowViewModel(monthlyReturn, submission)
        }

    rowsWithTaxYear
      .groupBy(_._1)
      .toSeq
      .sortBy(_._1)(Ordering.String.reverse)
      .map { case (taxYearLabel, rows) =>
        TaxYearHistoryViewModel(
          taxYearCaption = s"Tax year $taxYearLabel",
          rows = rows.map(_._2)
        )
      }
  }

  private def toRowViewModel(
    monthlyReturn: SubmittedMonthlyReturnData,
    submissionOpt: Option[SubmittedSubmissionData]
  ): SubmittedReturnsRowViewModel = {
    val periodEndText     = buildReturnPeriodEnd(monthlyReturn)
    val dateSubmittedText = submissionOpt
      .flatMap(_.acceptedTime)
      .map { instant =>
        instant.atZone(ukTimezone).toLocalDate.format(displayDateFormatter)
      }
      .getOrElse("")

    SubmittedReturnsRowViewModel(
      returnPeriodEnd = periodEndText,
      dateSubmitted = dateSubmittedText,
      monthlyReturn = LinkViewModel(
        text = "Print",
        url = "#", // TODO: F2 and F3 - replace with real route of page sr-04 Print monthly return
        hiddenText = s"monthly return for $periodEndText"
      ),
      submissionReceipt = LinkViewModel(
        text = "View",
        url = "#", // TODO: F2 and F3 - replace with real route of page SR-02-f View/save submitted return
        hiddenText = s"submission receipt for $periodEndText"
      ),
      status = Text(
        buildStatusText(monthlyReturn)
      )
    )
  }

  private def buildReturnPeriodEnd(monthlyReturn: SubmittedMonthlyReturnData): String = {
    // TODO: implement F2 return period ended format
    val monthName = Month.of(monthlyReturn.taxMonth).getDisplayName(TextStyle.FULL, Locale.UK)
    s"$monthName ${monthlyReturn.taxYear}"
  }

  private def buildStatusText(
    monthlyReturn: SubmittedMonthlyReturnData
    // TODO add submissionOpt: Option[SubmittedSubmissionData]
  ): String =
    // TODO: implement F4 and F5 status mapping rules
    monthlyReturn.status match {
      case "SUBMITTED_NO_RECEIPT" => "Awaiting confirmation"
      case "SUBMITTED"            => "Amend"
      case other                  => other
    }

}
