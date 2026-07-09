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
import viewmodels.*

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import models.response.GetSubmittedVerificationsResponse
import java.time.LocalDate

@Singleton
class VerificationHistoryService @Inject() () {

  private val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

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
    taxYear: String,
    instanceId: String
  ): Option[VerificationHistoryPageViewModel] =
    taxYear.toIntOption.flatMap { taxYearInt =>
      val taxYearSections = buildTaxYearSections(data).filter(_.fromYear == taxYearInt)
      if (taxYearSections.isEmpty) None
      else {
        Some(
          VerificationHistoryPageViewModel(
            taxYears = taxYearSections,
            selectedTaxYear = Some(taxYear),
            instanceId = instanceId
          )
        )
      }
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

    val submissionDatesByVerificationBatchId: Map[Long, LocalDate] =
      response.submissions
        .flatMap { submission =>
          for {
            verificationBatchId <- submission.activeObjectId
            submittedDate       <- submission.submissionRequestDate
                                     .orElse(submission.createDate)
                                     .map(_.toLocalDate)
          } yield verificationBatchId -> submittedDate
        }
        .groupBy(_._1)
        .view
        .mapValues(_.map(_._2).max)
        .toMap

    val verificationRequests =
      response.verificationBatches
        .flatMap { batch =>
          for {
            verificationNumber <- batch.verificationNumber
            submittedDate      <- submissionDatesByVerificationBatchId
                                    .get(batch.verificationBatchId)
                                    .orElse(batch.createDate.map(_.toLocalDate))
          } yield VerificationRequestData(
            verificationNumber = verificationNumber,
            dateSubmitted = submittedDate,
            taxYear = taxYearStart(submittedDate)
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
}
