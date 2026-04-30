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

import models.history.{SubmittedMonthlyReturnData, SubmittedReturnsData, SubmittedSubmissionData}
import viewmodels.{LinkViewModel, ReturnTypeViewModel, StatusViewModel, SubmittedReturnsPageViewModel, SubmittedReturnsRowViewModel, TaxYearHistoryViewModel}
import viewmodels.StatusViewModel.Text

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, YearMonth, ZoneId, ZoneOffset, ZonedDateTime}
import javax.inject.{Inject, Singleton}

@Singleton
class SubmittedReturnsService @Inject() {

  private val ukTimezone: ZoneId                         = ZoneId.of("Europe/London")
  private val displayDateFormatter: DateTimeFormatter    = DateTimeFormatter.ofPattern("d MMM yyyy")
  private val shortMonthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
  private val amendmentCutOffInstant: Instant            = ZonedDateTime.of(2016, 2, 5, 0, 0, 0, 0, ZoneOffset.UTC).toInstant

  def buildAllYearsViewModel(data: SubmittedReturnsData): Option[SubmittedReturnsPageViewModel] =
    val taxYearSections = buildTaxYearSections(data)

    Some(
      SubmittedReturnsPageViewModel(
        taxYears = taxYearSections,
        selectedTaxYear = None,
        showReturnToTaxYearsLink = taxYearSections.size > 1
      )
    )

  def buildSingleYearViewModel(data: SubmittedReturnsData, taxYear: String): Option[SubmittedReturnsPageViewModel] =
    taxYear.toIntOption.map { taxYearInt =>
      val taxYearSections = buildTaxYearSections(data)

      SubmittedReturnsPageViewModel(
        taxYears = taxYearSections.filter(_.fromYear == taxYearInt),
        selectedTaxYear = Some(taxYear),
        showReturnToTaxYearsLink = taxYearSections.size > 1
      )
    }

  private def buildTaxYearSections(data: SubmittedReturnsData): Seq[TaxYearHistoryViewModel] = {
    val submissionsByMonthlyReturnId =
      data.submissions
        .flatMap(submission => submission.activeObjectId.map(_ -> submission))
        .toMap

    val rowsWithTaxYear =
      data.monthlyReturns
        .sortBy(mr => (mr.taxYear, mr.taxMonth))(Ordering.Tuple2(Ordering.Int, Ordering.Int).reverse)
        .flatMap { monthlyReturn =>
          submissionsByMonthlyReturnId
            .get(monthlyReturn.monthlyReturnId)
            .map { submission =>
              val fromYear = taxYearFromYear(monthlyReturn)
              fromYear -> toRowViewModel(monthlyReturn, Some(submission))
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

  private def taxYearFromYear(monthlyReturn: SubmittedMonthlyReturnData): Int =
    val returnPeriodDate = LocalDate.of(monthlyReturn.taxYear, monthlyReturn.taxMonth, 5)
    val taxYearStartDate = LocalDate.of(monthlyReturn.taxYear, 4, 6)

    if (returnPeriodDate.isBefore(taxYearStartDate)) {
      monthlyReturn.taxYear - 1
    } else {
      monthlyReturn.taxYear
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
        url = "#",
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
          url = "#", // TODO
          hiddenText = periodEndText
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
              StatusViewModel.Link(
                link = LinkViewModel(
                  url = "#", // TODO
                  hiddenText = buildReturnPeriodEnd(monthlyReturn)
                ),
                textKey = "history.returnHistory.status.amend",
                hiddenTextKey = "history.returnHistory.hidden.status.amend"
              )
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
        StatusViewModel.Link(
          link = LinkViewModel(
            url = "#", // TODO
            hiddenText = buildReturnPeriodEnd(monthlyReturn)
          ),
          textKey = "history.returnHistory.status.inProgress",
          hiddenTextKey = "history.returnHistory.hidden.status.inProgress"
        )
      case Some("PENDING") | Some("ACCEPTED") | Some("SUBMITTED_NO_RECEIPT") =>
        StatusViewModel.Text("history.returnHistory.status.awaitingConfirmation")
      case Some("SUBMITTED")                                                 =>
        StatusViewModel.Link(
          link = LinkViewModel(
            url = "#", // TODO
            hiddenText = buildReturnPeriodEnd(monthlyReturn)
          ),
          textKey = "history.returnHistory.status.amend",
          hiddenTextKey = "history.returnHistory.hidden.status.amend"
        )
      case Some("DEPARTMENTAL_ERROR") | Some("FATAL_ERROR")                  =>
        StatusViewModel.Text("history.returnHistory.status.notAvailable")
      case _                                                                 =>
        StatusViewModel.Text("")
    }

  private def isSuperseded(monthlyReturn: SubmittedMonthlyReturnData): Boolean =
    monthlyReturn.supersededBy.exists(_ > 0)

}
