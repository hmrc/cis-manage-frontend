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

import connectors.ConstructionIndustrySchemeConnector
import models.history.*
import models.history.AmendmentHandoffData.given
import models.response.GetSubmittedMonthlyReturnsDataResponse
import play.api.i18n.Lang
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.*
import utils.{DateTimeFormats, IrMarkReferenceGenerator, Utils}
import viewmodels.*
import viewmodels.StatusViewModel.Text
import play.api.libs.json.Json

import java.time.*
import java.time.format.{DateTimeFormatter, TextStyle}
import java.util.Locale
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmittedReturnsService @Inject() (
  connector: ConstructionIndustrySchemeConnector
)(implicit ec: ExecutionContext) {

  private val ukTimezone: ZoneId                         = ZoneId.of("Europe/London")
  private val displayDateFormatter: DateTimeFormatter    = DateTimeFormatter.ofPattern("d MMM yyyy")
  private val shortMonthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
  private val amendmentCutOffInstant: Instant            = ZonedDateTime.of(2016, 2, 5, 0, 0, 0, 0, ZoneOffset.UTC).toInstant
  private val displayTimeFormatter: DateTimeFormatter    = DateTimeFormatter.ofPattern("h:mma", Locale.UK)

  def buildAllYearsViewModel(data: SubmittedReturnsData): Option[SubmittedReturnsPageViewModel] =
    Some(
      SubmittedReturnsPageViewModel(
        taxYears = buildTaxYearSections(data),
        selectedTaxYear = None
      )
    )

  def buildSingleYearViewModel(
    data: SubmittedReturnsData,
    taxYear: String
  ): Option[SubmittedReturnsPageViewModel] =
    taxYear.toIntOption.map { taxYearInt =>
      SubmittedReturnsPageViewModel(
        taxYears = buildTaxYearSections(data).filter(_.fromYear == taxYearInt),
        selectedTaxYear = Some(taxYear)
      )
    }

  def createAmendmentHandoff(
    data: SubmittedReturnsData,
    instanceId: String,
    taxYear: Int,
    taxMonth: Int
  )(implicit hc: HeaderCarrier): Future[Either[String, String]] = {
    val monthlyReturnOpt = data.monthlyReturns.find(mr => mr.taxYear == taxYear && mr.taxMonth == taxMonth)

    val submissionOpt = monthlyReturnOpt.flatMap { monthlyReturn =>
      data.submissions.find(_.activeObjectId.contains(monthlyReturn.monthlyReturnId))
    }

    monthlyReturnOpt match {
      case Some(monthlyReturn) =>
        val payload = AmendmentHandoffData(
          instanceId = instanceId,
          taxYear = taxYear,
          taxMonth = taxMonth,
          returnType = monthlyReturn.nilReturnIndicator,
          acceptedTime = submissionOpt.flatMap(_.acceptedTime).map(_.toString)
        )

        connector
          .createJourneyHandoff(
            journeyType = "amend-monthly-return",
            data = Json.toJsObject(payload)
          )
          .map(Right(_))

      case None =>
        Future.successful(Left(s"No monthly return found for tax year $taxYear and month $taxMonth"))
    }
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
      Utils.formatCurrency(
        data.monthlyReturnItems.flatMap(_.totalPayments).map(_.replace(",", "")).map(BigDecimal(_)).sum
      )
    val totalCostOfMaterials =
      Utils.formatCurrency(
        data.monthlyReturnItems.flatMap(_.costOfMaterials).map(_.replace(",", "")).map(BigDecimal(_)).sum
      )
    val totalTaxDeducted     =
      Utils.formatCurrency(
        data.monthlyReturnItems.flatMap(_.totalDeducted).map(_.replace(",", "")).map(BigDecimal(_)).sum
      )
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
        ReturnTypeViewModel.Nil.toString.toLowerCase
      } else {
        ReturnTypeViewModel.Standard.toString.toLowerCase
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
    val amendUrl          = buildAmendUrl(monthlyReturn)

    SubmittedReturnsRowViewModel(
      returnPeriodEnd = periodEndText,
      returnType = returnType,
      dateSubmitted = dateSubmittedText,
      monthlyReturn = LinkViewModel(
        url = controllers.history.routes.PrintSubmissionDetailsController
          .onPageLoad(
            monthlyReturn.taxYear,
            monthlyReturn.taxMonth,
            monthlyReturn.amendment
          )
          .url,
        hiddenText = periodEndText
      ),
      submissionReceipt =
        buildSubmissionReceipt(submissionOpt, periodEndText, monthlyReturn.taxYear, monthlyReturn.taxMonth),
      status = buildStatus(monthlyReturn, submissionOpt, amendUrl)
    )
  }

  private def buildReturnPeriodEnd(monthlyReturn: SubmittedMonthlyReturnData): String =
    YearMonth
      .of(monthlyReturn.taxYear, monthlyReturn.taxMonth)
      .format(shortMonthYearFormatter)

  private def buildReturnType(monthlyReturn: SubmittedMonthlyReturnData): ReturnTypeViewModel =
    monthlyReturn.nilReturnIndicator match {
      case ReturnTypeViewModel.Nil.toString      => ReturnTypeViewModel.Nil
      case ReturnTypeViewModel.Standard.toString => ReturnTypeViewModel.Standard
      case _                                     => ReturnTypeViewModel.Unknown
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
    periodEndText: String,
    taxYear: Int,
    taxMonth: Int
  ): StatusViewModel =
    if (isSubmissionReceiptAvailable(submissionOpt)) {

      StatusViewModel.Link(
        link = LinkViewModel(
          url =
            s"/construction-industry-scheme/management/monthly-return/confirmation-history?taxYear=$taxYear&taxMonth=$taxMonth&amendment=N",
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
    submissionOpt: Option[SubmittedSubmissionData],
    amendUrl: String
  ): StatusViewModel = {
    val acceptedTimeOpt = submissionOpt.flatMap(_.acceptedTime)

    acceptedTimeOpt match {
      case None =>
        StatusViewModel.Text("history.returnHistory.status.notAvailable")

      case Some(acceptedTime) =>
        monthlyReturn.status match {
          case "SUBMITTED" =>
            if (isSuperseded(monthlyReturn)) {
              buildAmendmentStatus(monthlyReturn, amendUrl)
            } else if (!acceptedTime.isBefore(amendmentCutOffInstant)) {
              StatusViewModel.Link(
                link = LinkViewModel(
                  url = amendUrl,
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

  private def buildAmendmentStatus(monthlyReturn: SubmittedMonthlyReturnData, amendUrl: String): StatusViewModel =
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
            url = amendUrl,
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

  private def buildAmendUrl(monthlyReturn: SubmittedMonthlyReturnData): String =
    controllers.history.routes.SubmittedReturnsController
      .startAmendment(
        taxYear = monthlyReturn.taxYear,
        taxMonth = monthlyReturn.taxMonth
      )
      .url

  private def isSuperseded(monthlyReturn: SubmittedMonthlyReturnData): Boolean =
    monthlyReturn.supersededBy.exists(_ > 0)

  def getMonthlyReturnComplete(
    instanceId: String,
    taxYear: Int,
    taxMonth: Int,
    amendment: String
  )(implicit hc: HeaderCarrier): Future[Either[String, SubmissionReceiptViewModel]] =
    connector.getMonthlyReturnComplete(instanceId, taxYear, taxMonth, amendment).map { response =>
      val submission = response.submission.headOption

      submission match {
        case Some(sub) if !isSubmissionValid(sub, amendment) =>
          Left(s"Submission guard failed: status=${sub.status.getOrElse("None")}, amendment=$amendment")
        case Some(sub) if !hasValidIrMark(sub)               =>
          Left("Submission guard failed: IRMark sent/received is null or does not match")
        case _                                               =>
          Right(buildReceiptViewModel(response, instanceId, taxYear, taxMonth))
      }
    }

  private def isSubmissionValid(sub: models.history.CompleteSubmissionData, amendment: String): Boolean =
    sub.status.contains("SUBMITTED") || amendment.equalsIgnoreCase("Y")

  private def hasValidIrMark(sub: models.history.CompleteSubmissionData): Boolean =
    (sub.hmrcMarkGenerated, sub.hmrcMarkGgis) match {
      case (Some(sent), Some(received)) if sent.nonEmpty && received.nonEmpty => sent == received
      case _                                                                  => false
    }

  private def buildReceiptReturnType(nilIndicator: Option[String]): String =
    nilIndicator match {
      case Some(n) if n.trim.equalsIgnoreCase("Y") => "submissionConfirmation.returnType.nil"
      case _                                       => "submissionConfirmation.returnType.monthly"
    }

  private def buildReceiptViewModel(
    response: MonthlyReturnCompleteResponse,
    instanceId: String,
    taxYear: Int,
    taxMonth: Int
  ): SubmissionReceiptViewModel = {
    val scheme     = response.scheme.headOption
    val mr         = response.monthlyReturn.headOption
    val submission = response.submission.headOption

    val monthName = Month.of(taxMonth).getDisplayName(TextStyle.FULL, Locale.UK)

    val returnType = buildReceiptReturnType(mr.flatMap(_.nilReturnIndicator))

    val submissionType = submission.map(_.submissionType).getOrElse("Monthly return")

    val payeReference = scheme
      .map { s =>
        s"${s.taxOfficeNumber}/${s.taxOfficeReference}"
      }
      .getOrElse("")

    val submittedAt = submission
      .flatMap(_.acceptedTime)
      .flatMap { ts =>
        scala.util.Try {
          val dateTime = LocalDateTime.parse(ts.take(19)).atZone(ukTimezone)
          val time     = dateTime.format(displayTimeFormatter)
          val date     = dateTime.toLocalDate.format(displayDateFormatter)
          s"$time on $date"
        }.toOption
      }

    val items = response.monthlyReturnItems.map { item =>
      SubcontractorPayment(
        item.subcontractorName.getOrElse(""),
        Utils.formatCurrency(Utils.toBigDecimal(item.totalPayments)),
        Utils.formatCurrency(Utils.toBigDecimal(item.costOfMaterials)),
        Utils.formatCurrency(Utils.toBigDecimal(item.totalDeducted))
      )
    }

    SubmissionReceiptViewModel(
      contractorName = scheme.flatMap(_.name).getOrElse(""),
      payeReference = payeReference,
      taxYear = taxYear,
      taxMonth = taxMonth,
      returnPeriodEnd = s"$monthName $taxYear",
      returnType = returnType,
      submissionType = submissionType,
      hmrcMark = submission.flatMap(_.hmrcMarkGenerated).map { mark =>
        scala.util.Try(utils.IrMarkReferenceGenerator.fromBase64(mark)).getOrElse(mark)
      },
      submittedAt = submittedAt,
      emailRecipient = submission.flatMap(_.emailRecipient),
      instanceId = instanceId,
      items = items
    )
  }
}
