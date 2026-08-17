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
import models.response.GetSubmittedVerification
import viewmodels.*

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import models.response.{GetSubmittedSubmission, GetSubmittedVerificationsResponse}

import java.time.{Instant, LocalDate, LocalDateTime, OffsetDateTime, ZoneOffset}
import scala.util.Try

@Singleton
class VerificationHistoryService @Inject() () {

  private val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
  private val timeFormatter: DateTimeFormatter        = DateTimeFormatter.ofPattern("HH:mm")
  private val fullDateFormatter: DateTimeFormatter    = DateTimeFormatter.ofPattern("dd MMMM yyyy")

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

  def buildVerificationRequestViewModel(
    data: VerificationHistoryData,
    verificationBatchId: Long,
    instanceId: String
  ): Option[VerificationRequestPageViewModel] =
    data.verificationRequests
      .find(_.verificationBatchId == verificationBatchId)
      .map { request =>
        VerificationRequestPageViewModel(
          submittedTime = request.acceptedDateTime.format(timeFormatter),
          submittedDate = request.acceptedDateTime.format(fullDateFormatter),
          verificationNumber = request.verificationNumber,
          contractorName = request.contractorName,
          employerReference = request.employerReference,
          receiptReferenceNumber = request.receiptReferenceNumber,
          subcontractorsToVerify =
            request.subcontractorsToVerify.map(s => SubcontractorRowViewModel(s.name, s.verificationNumber)),
          manageSubcontractorsUrl = controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId).url
        )
      }

  def buildSubmissionReceiptViewModel(
    data: VerificationHistoryData,
    verificationBatchId: Long,
    instanceId: String
  ): Option[SubcontractorSubmissionReceiptViewModel] =
    data.verificationRequests
      .find(_.verificationBatchId == verificationBatchId)
      .map { request =>
        SubcontractorSubmissionReceiptViewModel(
          submissionTime = request.acceptedDateTime.format(timeFormatter),
          submissionDate = request.acceptedDateTime.format(fullDateFormatter),
          contractorName = request.contractorName,
          employerReference = request.employerReference,
          receiptReferenceNumber = request.receiptReferenceNumber,
          verificationNumber = request.verificationNumber,
          cisId = instanceId
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
        controllers.verify.routes.VerificationRequestController.onPageLoad(request.verificationBatchId).url,
      submissionReceiptLink =
        controllers.verify.routes.SubcontractorSubmissionReceiptController.onPageLoad(request.verificationBatchId).url
    )

  def toVerificationHistoryData(
    response: GetSubmittedVerificationsResponse
  ): VerificationHistoryData = {

    val verificationRequests =
      response.verificationBatches
        .flatMap { batch =>
          for {
            verificationNumber <- batch.verificationNumber
            submission          = submissionFor(batch.verificationBatchId, response.submissions)
            acceptedDateTime    = acceptedDateTimeFor(batch.verificationBatchId, submission)
          } yield {
            val scheme = schemeFor(batch.schemeId, response)

            VerificationRequestData(
              verificationBatchId = batch.verificationBatchId,
              verificationNumber = verificationNumber,
              dateSubmitted = acceptedDateTime.toLocalDate,
              taxYear = taxYearStart(acceptedDateTime.toLocalDate),
              acceptedDateTime = acceptedDateTime,
              contractorName = contractorNameFor(scheme),
              employerReference = scheme.taxOfficeNumber,
              receiptReferenceNumber = receiptReferenceNumberFor(batch.verificationBatchId, submission),
              subcontractorsToVerify = subcontractorsFor(
                batch.verificationBatchId,
                verificationNumber,
                response.verifications,
                response.subcontractors
              )
            )
          }
        }
        .sortBy(_.dateSubmitted)(Ordering[LocalDate].reverse)

    VerificationHistoryData(
      verificationRequests = verificationRequests
    )
  }

  private def schemeFor(
    schemeId: Long,
    response: GetSubmittedVerificationsResponse
  ): models.response.GetSubmittedContractorScheme =
    response.scheme
      .find(_.schemeId.toLong == schemeId)
      .getOrElse {
        throw new IllegalStateException(
          s"Submitted verification scheme $schemeId is missing"
        )
      }

  private def contractorNameFor(
    scheme: models.response.GetSubmittedContractorScheme
  ): String =
    scheme.name.getOrElse {
      throw new IllegalStateException(
        s"Submitted verification scheme ${scheme.schemeId} is missing contractor name"
      )
    }

  private def receiptReferenceNumberFor(
    verificationBatchId: Long,
    submission: GetSubmittedSubmission
  ): String =
    submission.hmrcMarkGgis.getOrElse {
      throw new IllegalStateException(
        s"Submitted verification $verificationBatchId is missing receipt reference number"
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

  private def submissionFor(
    verificationBatchId: Long,
    submissions: Seq[GetSubmittedSubmission]
  ): GetSubmittedSubmission =
    submissions
      .filter(_.activeObjectId.contains(verificationBatchId))
      .map(submission => acceptedDateTimeFor(verificationBatchId, submission) -> submission)
      .sortBy(_._1)
      .map(_._2)
      .lastOption
      .getOrElse {
        throw new IllegalStateException(
          s"Submitted verification $verificationBatchId has no matching accepted submission"
        )
      }

  private def acceptedDateTimeFor(
    verificationBatchId: Long,
    submission: GetSubmittedSubmission
  ): LocalDateTime =
    submission.acceptedTime
      .map(parseAcceptedDateTime)
      .getOrElse {
        throw new IllegalStateException(
          s"Submitted verification $verificationBatchId is missing accepted date"
        )
      }

  private def parseAcceptedDateTime(value: String): LocalDateTime =
    Try(LocalDateTime.parse(value))
      .orElse(Try(OffsetDateTime.parse(value).toLocalDateTime))
      .orElse(Try(Instant.parse(value).atZone(ZoneOffset.UTC).toLocalDateTime))
      .orElse(Try(LocalDate.parse(value).atStartOfDay()))
      .getOrElse {
        throw new IllegalStateException("Unable to parse submitted verification accepted date")
      }

  private def subcontractorsFor(
    verificationBatchId: Long,
    batchVerificationNumber: String,
    verifications: Seq[GetSubmittedVerification],
    subcontractors: Seq[models.response.GetSubmittedSubcontractor]
  ): Seq[SubcontractorVerificationData] =
    verifications
      .filter(_.verificationBatchId.contains(verificationBatchId))
      .sortBy(_.verificationId)
      .map { verification =>
        val name   =
          verification.subcontractorName
            .orElse(
              verification.subcontractorId
                .flatMap(id => subcontractors.find(_.subcontractorId == id).map(_.displayName))
            )
            .getOrElse("No name provided")
        val number = verification.verificationNumber.getOrElse(batchVerificationNumber)

        SubcontractorVerificationData(name, number)
      }
}
