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

import models.verify.*
import models.verify.VerificationTaxYearSelection.TaxYearPeriod
import viewmodels.*

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import models.response.{GetSubmittedSubmission, GetSubmittedVerificationsResponse}

import java.time.{Instant, LocalDate, LocalDateTime, OffsetDateTime, ZoneOffset}
import scala.util.Try

@Singleton
class VerificationHistoryService @Inject() () {

  private val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

  def getSubmittedVerificationTaxYears(data: VerificationHistoryData): Seq[TaxYearPeriod] =
    data.verificationRequests
      .map(request => TaxYearPeriod(request.taxYear))
      .distinct
      .sortBy(_.startYear)(Ordering.Int.reverse)

  def buildAllYearsViewModel(
    data: VerificationHistoryData,
    instanceId: String
  ): Option[VerificationHistoryPageViewModel] = {
    val taxYearSections = buildTaxYearSections(data)
    if (taxYearSections.isEmpty) None
    else {
      Some(
        VerificationHistoryPageViewModel(
          taxYears = taxYearSections,
          selectedTaxYear = None,
          instanceId = instanceId
        )
      )
    }
  }

  def buildSingleYearViewModel(
    data: VerificationHistoryData,
    taxYearStart: Int,
    instanceId: String
  ): Option[VerificationHistoryPageViewModel] =
    val taxYearSections = buildTaxYearSections(data).filter(_.fromYear == taxYearStart)
    if (taxYearSections.isEmpty) None
    else {
      Some(
        VerificationHistoryPageViewModel(
          taxYears = taxYearSections,
          selectedTaxYear = Some(taxYearStart.toString),
          instanceId = instanceId
        )
      )
    }

  private def buildTaxYearSections(
    data: VerificationHistoryData
  ): Seq[VerificationTaxYearViewModel] = {
    val rowsWithTaxYear = data.verificationRequests
      .sortBy(_.dateSubmitted)(Ordering[java.time.LocalDate].reverse)
      .map { request =>
        val fromYear = request.taxYear
        fromYear -> toRowViewModel(request)
      }

    rowsWithTaxYear
      .groupBy(_._1)
      .toSeq
      .sortBy(_._1)(Ordering.Int.reverse)
      .map { case (fromYear, rows) =>
        VerificationTaxYearViewModel(
          fromYear = fromYear,
          toYear = fromYear + 1,
          rows = rows.map(_._2)
        )
      }
  }

  private def toRowViewModel(
    request: VerificationRequestData
  ): VerificationHistoryRowViewModel =
    VerificationHistoryRowViewModel(
      verificationNumber = request.verificationNumber,
      dateSubmitted = request.dateSubmitted.format(displayDateFormatter),
      verificationRequestLink =
        controllers.verify.routes.VerificationRequestController.onPageLoad(request.verificationNumber).url,
      submissionReceiptLink = "#"
    )

  def toVerificationHistoryData(
    response: GetSubmittedVerificationsResponse
  ): VerificationHistoryData = {

    val verificationRequests =
      response.verificationBatches
        .flatMap { batch =>
          for {
            verificationNumber <- batch.verificationNumber
            acceptedDate       <- Try(acceptedDateFor(batch.verificationBatchId, response.submissions)).toOption
          } yield VerificationRequestData(
            verificationNumber = verificationNumber,
            dateSubmitted = acceptedDate,
            taxYear = taxYearStart(acceptedDate)
          )
        }
        .sortBy(_.dateSubmitted)(Ordering[LocalDate].reverse)

    VerificationHistoryData(
      verificationRequests = verificationRequests
    )
  }

  private def taxYearStart(date: LocalDate): Int = {
    val taxYearStartDate =
      LocalDate.of(date.getYear, 4, 6)

    if (!date.isBefore(taxYearStartDate)) {
      date.getYear
    } else {
      date.getYear - 1
    }
  }

  private def acceptedDateFor(
    verificationBatchId: Long,
    submissions: Seq[GetSubmittedSubmission]
  ): LocalDate =
    submissions
      .filter(_.activeObjectId.contains(verificationBatchId))
      .map { submission =>
        submission.acceptedTime
          .map(parseAcceptedDate)
          .getOrElse {
            throw new IllegalStateException(
              s"Submitted verification $verificationBatchId is missing accepted date"
            )
          }
      }
      .maxOption
      .getOrElse {
        throw new IllegalStateException(
          s"Submitted verification $verificationBatchId has no matching accepted submission"
        )
      }

  private def parseAcceptedDate(value: String): LocalDate =
    Try(LocalDateTime.parse(value).toLocalDate)
      .orElse(Try(OffsetDateTime.parse(value).toLocalDate))
      .orElse(Try(Instant.parse(value).atZone(ZoneOffset.UTC).toLocalDate))
      .orElse(Try(LocalDate.parse(value)))
      .getOrElse {
        throw new IllegalStateException("Unable to parse submitted verification accepted date")
      }
}
