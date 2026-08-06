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

import models.verify.{SubcontractorVerificationData, VerificationHistoryData, VerificationRequestData}
import models.verify.VerificationTaxYearSelection.TaxYearPeriod
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues

import java.time.LocalDate
import models.response.*
import viewmodels.SubcontractorRowViewModel
import java.time.LocalDateTime

class VerificationHistoryServiceSpec extends AnyFreeSpec with Matchers with OptionValues {

  private val service    = new VerificationHistoryService()
  private val instanceId = "900063"

  private val receiptReferenceNumber = "Pyy1LRJh053AE+nuyp0GJR7oESw="

  private def verificationRequestData(
    verificationNumber: String,
    dateSubmitted: LocalDate,
    taxYear: Int,
    verificationBatchId: Long
  ): VerificationRequestData =
    VerificationRequestData(
      verificationBatchId = verificationBatchId,
      verificationNumber = verificationNumber,
      dateSubmitted = dateSubmitted,
      taxYear = taxYear,
      acceptedDateTime = dateSubmitted.atStartOfDay(),
      contractorName = "Test Scheme",
      employerReference = "123PA000001",
      receiptReferenceNumber = receiptReferenceNumber,
      subcontractorsToVerify = Seq.empty,
      subcontractorsToReverify = Seq.empty
    )

  private val data = VerificationHistoryData(
    verificationRequests = Seq(
      verificationRequestData("V001", LocalDate.of(2026, 4, 6), 2026, 1L),
      verificationRequestData("V002", LocalDate.of(2026, 6, 6), 2026, 2L),
      verificationRequestData("V003", LocalDate.of(2025, 4, 6), 2025, 3L),
      verificationRequestData("V004", LocalDate.of(2025, 6, 6), 2025, 4L)
    )
  )

  private def submittedScheme(): GetSubmittedContractorScheme =
    GetSubmittedContractorScheme(
      schemeId = 1,
      instanceId = instanceId,
      accountsOfficeReference = "123PA000001",
      taxOfficeNumber = "123",
      taxOfficeReference = "AB456",
      name = Some("Test Scheme")
    )

  private def expectedRequest(
    verificationBatchId: Long,
    verificationNumber: String,
    acceptedDateTime: LocalDateTime,
    taxYear: Int,
    subcontractorsToVerify: Seq[SubcontractorVerificationData] = Seq.empty,
    subcontractorsToReverify: Seq[SubcontractorVerificationData] = Seq.empty
  ): VerificationRequestData =
    VerificationRequestData(
      verificationBatchId = verificationBatchId,
      verificationNumber = verificationNumber,
      dateSubmitted = acceptedDateTime.toLocalDate,
      taxYear = taxYear,
      acceptedDateTime = acceptedDateTime,
      contractorName = "Test Scheme",
      employerReference = "123PA000001",
      receiptReferenceNumber = receiptReferenceNumber,
      subcontractorsToVerify = subcontractorsToVerify,
      subcontractorsToReverify = subcontractorsToReverify
    )

  private def submittedVerificationBatch(
    verificationBatchId: Long,
    verificationNumber: Option[String],
    createDate: Option[LocalDateTime] = None
  ): GetSubmittedVerificationBatch =
    GetSubmittedVerificationBatch(
      verificationBatchId = verificationBatchId,
      schemeId = 1L,
      verificationsCounter = None,
      verifBatchResourceRef = None,
      proceedSession = None,
      confirmArrangement = None,
      confirmCorrect = None,
      status = None,
      verificationNumber = verificationNumber,
      createDate = createDate,
      lastUpdate = None,
      version = None
    )

  private def submittedVerification(
    verificationId: Long,
    verificationBatchId: Long,
    subcontractorName: Option[String],
    verificationNumber: Option[String] = None,
    actionIndicator: Option[String] = Some("A")
  ): GetSubmittedVerification =
    GetSubmittedVerification(
      verificationId = verificationId,
      matched = None,
      verificationNumber = verificationNumber,
      taxTreatment = None,
      actionIndicator = actionIndicator,
      verificationBatchId = Some(verificationBatchId),
      schemeId = Some(1L),
      subcontractorId = None,
      subcontractorName = subcontractorName,
      verificationResourceRef = None,
      proceed = None,
      createDate = None,
      lastUpdate = None,
      version = None
    )

  private def submittedSubmission(
    activeObjectId: Option[Long],
    acceptedTime: Option[String] = Some("2026-04-06T10:00:00"),
    submissionRequestDate: Option[LocalDateTime] = None,
    createDate: Option[LocalDateTime] = None
  ): GetSubmittedSubmission =
    GetSubmittedSubmission(
      submissionId = 1L,
      submissionType = "Verification",
      activeObjectId = activeObjectId,
      status = None,
      hmrcMarkGenerated = None,
      hmrcMarkGgis = Some(receiptReferenceNumber),
      emailRecipient = None,
      acceptedTime = acceptedTime,
      createDate = createDate,
      lastUpdate = None,
      schemeId = 1L,
      agentId = None,
      l_Migrated = None,
      submissionRequestDate = submissionRequestDate,
      govTalkErrorCode = None,
      govTalkErrorType = None,
      govTalkErrorMessage = None
    )

  "VerificationHistoryService" - {

    "buildAllYearsViewModel" - {

      "must return a view model with all tax years grouped and sorted descending" in {
        val result = service.buildAllYearsViewModel(data, instanceId)

        result mustBe defined

        val vm = result.get
        vm.selectedTaxYear mustBe None
        vm.instanceId mustBe instanceId
        vm.taxYears.size mustBe 2
        vm.taxYears.head.fromYear mustBe 2026
        vm.taxYears.head.toYear mustBe 2027
        vm.taxYears(1).fromYear mustBe 2025
        vm.taxYears(1).toYear mustBe 2026
      }

      "must group rows correctly per tax year" in {
        val result = service.buildAllYearsViewModel(data, instanceId)

        val vm = result.get
        vm.taxYears.head.rows.size mustBe 2
        vm.taxYears(1).rows.size mustBe 2
      }

      "must format date submitted correctly" in {
        val result = service.buildAllYearsViewModel(data, instanceId)

        val vm   = result.get
        val rows = vm.taxYears.head.rows
        rows.head.dateSubmitted mustBe "6 Jun 2026"
        rows(1).dateSubmitted mustBe "6 Apr 2026"
      }

      "must set verification request link to the verification request page" in {
        val result = service.buildAllYearsViewModel(data, instanceId)

        val vm  = result.get
        val row = vm.taxYears.head.rows.head
        row.verificationRequestLink must include("/verify/verification-request")
        row.verificationRequestLink must include("verificationBatchId=2")
      }

      "must set submission receipt link to the submission receipt page" in {
        val result = service.buildAllYearsViewModel(data, instanceId)

        val vm  = result.get
        val row = vm.taxYears.head.rows.head
        row.submissionReceiptLink must include("/verify/history/submission-receipt")
        row.submissionReceiptLink must include("verificationBatchId=2")
      }

      "must return None when there are no verification requests" in {
        val emptyData = VerificationHistoryData(verificationRequests = Seq.empty)
        val result    = service.buildAllYearsViewModel(emptyData, instanceId)

        result mustBe None
      }
    }

    "buildSingleYearViewModel" - {

      "must return a view model filtered to the selected tax year" in {
        val result = service.buildSingleYearViewModel(data, "2026", instanceId)

        result mustBe defined

        val vm = result.get
        vm.selectedTaxYear mustBe Some("2026")
        vm.instanceId mustBe instanceId
        vm.taxYears.size mustBe 1
        vm.taxYears.head.fromYear mustBe 2026
        vm.taxYears.head.rows.size mustBe 2
      }

      "must return None for an invalid (non-numeric) tax year" in {
        val result = service.buildSingleYearViewModel(data, "invalid", instanceId)

        result mustBe None
      }

      "must return None for a year with no data" in {
        val result = service.buildSingleYearViewModel(data, "2020", instanceId)

        result mustBe None
      }
    }

    "toVerificationHistoryData" - {

      "must map submitted verification batches to history data using accepted date and calculate tax years" in {
        val response = GetSubmittedVerificationsResponse(
          scheme = Seq(submittedScheme()),
          subcontractors = Seq.empty,
          verificationBatches = Seq(
            submittedVerificationBatch(
              verificationBatchId = 1L,
              verificationNumber = Some("V001"),
              createDate = Some(LocalDateTime.of(2026, 4, 1, 9, 0))
            ),
            submittedVerificationBatch(
              verificationBatchId = 2L,
              verificationNumber = Some("V002"),
              createDate = Some(LocalDateTime.of(2026, 1, 1, 9, 0))
            )
          ),
          verifications = Seq.empty,
          submissions = Seq(
            submittedSubmission(
              activeObjectId = Some(1L),
              acceptedTime = Some("2026-04-06T10:00:00")
            ),
            submittedSubmission(
              activeObjectId = Some(2L),
              acceptedTime = Some("2026-02-06T10:00:00")
            )
          )
        )

        service.toVerificationHistoryData(response) mustBe VerificationHistoryData(
          verificationRequests = Seq(
            expectedRequest(1L, "V001", LocalDateTime.of(2026, 4, 6, 10, 0), 2026),
            expectedRequest(2L, "V002", LocalDateTime.of(2026, 2, 6, 10, 0), 2025)
          )
        )
      }

      "must throw when accepted date is missing" in {
        val response = GetSubmittedVerificationsResponse(
          scheme = Seq(submittedScheme()),
          subcontractors = Seq.empty,
          verificationBatches = Seq(
            submittedVerificationBatch(
              verificationBatchId = 1L,
              verificationNumber = Some("V001"),
              createDate = Some(LocalDateTime.of(2026, 1, 1, 9, 0))
            )
          ),
          verifications = Seq.empty,
          submissions = Seq(
            submittedSubmission(
              activeObjectId = Some(1L),
              acceptedTime = None,
              createDate = Some(LocalDateTime.of(2026, 6, 6, 10, 0))
            )
          )
        )

        val exception = intercept[IllegalStateException] {
          service.toVerificationHistoryData(response)
        }

        exception.getMessage must include("missing accepted date")
      }

      "must throw when there is no matching accepted submission" in {
        val response = GetSubmittedVerificationsResponse(
          scheme = Seq(submittedScheme()),
          subcontractors = Seq.empty,
          verificationBatches = Seq(
            submittedVerificationBatch(
              verificationBatchId = 1L,
              verificationNumber = Some("V001"),
              createDate = Some(LocalDateTime.of(2026, 3, 31, 9, 0))
            )
          ),
          verifications = Seq.empty,
          submissions = Seq.empty
        )

        val exception = intercept[IllegalStateException] {
          service.toVerificationHistoryData(response)
        }

        exception.getMessage must include("no matching accepted submission")
      }

      "must exclude batches without a verification number" in {
        val response = GetSubmittedVerificationsResponse(
          scheme = Seq(submittedScheme()),
          subcontractors = Seq.empty,
          verificationBatches = Seq(
            submittedVerificationBatch(
              verificationBatchId = 1L,
              verificationNumber = None,
              createDate = Some(LocalDateTime.of(2026, 4, 6, 9, 0))
            ),
            submittedVerificationBatch(
              verificationBatchId = 2L,
              verificationNumber = Some("V002"),
              createDate = None
            )
          ),
          verifications = Seq.empty,
          submissions = Seq(
            submittedSubmission(activeObjectId = Some(1L)),
            submittedSubmission(activeObjectId = Some(2L))
          )
        )

        service.toVerificationHistoryData(response) mustBe VerificationHistoryData(
          verificationRequests = Seq(
            expectedRequest(2L, "V002", LocalDateTime.of(2026, 4, 6, 10, 0), 2026)
          )
        )
      }

      "must use the newest accepted date when more than one submission exists for a batch" in {
        val response = GetSubmittedVerificationsResponse(
          scheme = Seq(submittedScheme()),
          subcontractors = Seq.empty,
          verificationBatches = Seq(
            submittedVerificationBatch(
              verificationBatchId = 1L,
              verificationNumber = Some("V001")
            )
          ),
          verifications = Seq.empty,
          submissions = Seq(
            submittedSubmission(
              activeObjectId = Some(1L),
              acceptedTime = Some("2026-04-06T10:00:00")
            ),
            submittedSubmission(
              activeObjectId = Some(1L),
              acceptedTime = Some("2026-06-06T10:00:00")
            )
          )
        )

        service.toVerificationHistoryData(response) mustBe VerificationHistoryData(
          verificationRequests = Seq(
            expectedRequest(1L, "V001", LocalDateTime.of(2026, 6, 6, 10, 0), 2026)
          )
        )
      }

      "must calculate tax year from 6 April boundary" in {
        val response = GetSubmittedVerificationsResponse(
          scheme = Seq(submittedScheme()),
          subcontractors = Seq.empty,
          verificationBatches = Seq(
            submittedVerificationBatch(
              verificationBatchId = 1L,
              verificationNumber = Some("V001")
            ),
            submittedVerificationBatch(
              verificationBatchId = 2L,
              verificationNumber = Some("V002")
            )
          ),
          verifications = Seq.empty,
          submissions = Seq(
            submittedSubmission(
              activeObjectId = Some(1L),
              acceptedTime = Some("2026-04-05T10:00:00")
            ),
            submittedSubmission(
              activeObjectId = Some(2L),
              acceptedTime = Some("2026-04-06T10:00:00")
            )
          )
        )

        service.toVerificationHistoryData(response) mustBe VerificationHistoryData(
          verificationRequests = Seq(
            expectedRequest(2L, "V002", LocalDateTime.of(2026, 4, 6, 10, 0), 2026),
            expectedRequest(1L, "V001", LocalDateTime.of(2026, 4, 5, 10, 0), 2025)
          )
        )
      }

      "must map batch, submission, scheme and verification details for history and detail pages" in {
        val response = GetSubmittedVerificationsResponse(
          scheme = Seq(submittedScheme()),
          subcontractors = Seq.empty,
          verificationBatches = Seq(
            submittedVerificationBatch(
              verificationBatchId = 1L,
              verificationNumber = Some("V001")
            )
          ),
          verifications = Seq(
            submittedVerification(
              verificationId = 1L,
              verificationBatchId = 1L,
              subcontractorName = Some("Amity Marine Contractors")
            ),
            submittedVerification(
              verificationId = 2L,
              verificationBatchId = 1L,
              subcontractorName = Some("Orca Industrial"),
              verificationNumber = Some("V001/L"),
              actionIndicator = Some("R")
            )
          ),
          submissions = Seq(
            submittedSubmission(
              activeObjectId = Some(1L),
              acceptedTime = Some("2026-04-06T14:30:00")
            )
          )
        )

        service.toVerificationHistoryData(response) mustBe VerificationHistoryData(
          verificationRequests = Seq(
            expectedRequest(
              verificationBatchId = 1L,
              verificationNumber = "V001",
              acceptedDateTime = LocalDateTime.of(2026, 4, 6, 14, 30),
              taxYear = 2026,
              subcontractorsToVerify = Seq(
                SubcontractorVerificationData("Amity Marine Contractors", "V001")
              ),
              subcontractorsToReverify = Seq(
                SubcontractorVerificationData("Orca Industrial", "V001/L")
              )
            )
          )
        )
      }
    }

    "buildVerificationRequestViewModel" - {

      "must build request details from the selected verification history row" in {
        val requestData = expectedRequest(
          verificationBatchId = 1L,
          verificationNumber = "V001",
          acceptedDateTime = LocalDateTime.of(2026, 4, 6, 14, 30),
          taxYear = 2026,
          subcontractorsToVerify = Seq(SubcontractorVerificationData("Amity Marine Contractors", "V001")),
          subcontractorsToReverify = Seq(SubcontractorVerificationData("Orca Industrial", "V001/L"))
        )

        val result = service.buildVerificationRequestViewModel(
          VerificationHistoryData(Seq(requestData)),
          1L,
          instanceId
        )

        result.value.submittedTime mustBe "14:30"
        result.value.submittedDate mustBe "6 April 2026"
        result.value.verificationNumber mustBe "V001"
        result.value.contractorName mustBe "Test Scheme"
        result.value.employerReference mustBe "123PA000001"
        result.value.receiptReferenceNumber mustBe receiptReferenceNumber
        result.value.subcontractorsToVerify mustBe Seq(SubcontractorRowViewModel("Amity Marine Contractors", "V001"))
        result.value.subcontractorsToReverify mustBe Seq(SubcontractorRowViewModel("Orca Industrial", "V001/L"))
      }

      "must select the matching batch when verification numbers are duplicated" in {
        val firstRequest  = expectedRequest(
          verificationBatchId = 1L,
          verificationNumber = "V001",
          acceptedDateTime = LocalDateTime.of(2026, 4, 6, 14, 30),
          taxYear = 2026,
          subcontractorsToVerify = Seq(SubcontractorVerificationData("First Subcontractor", "V001"))
        )
        val secondRequest = expectedRequest(
          verificationBatchId = 2L,
          verificationNumber = "V001",
          acceptedDateTime = LocalDateTime.of(2026, 4, 7, 14, 30),
          taxYear = 2026,
          subcontractorsToVerify = Seq(SubcontractorVerificationData("Second Subcontractor", "V001"))
        )

        val result = service.buildVerificationRequestViewModel(
          VerificationHistoryData(Seq(firstRequest, secondRequest)),
          2L,
          instanceId
        )

        result.value.subcontractorsToVerify mustBe Seq(SubcontractorRowViewModel("Second Subcontractor", "V001"))
      }
    }

    "buildSubmissionReceiptViewModel" - {

      "must build receipt details from the selected verification history row" in {
        val requestData = expectedRequest(
          verificationBatchId = 1L,
          verificationNumber = "V001",
          acceptedDateTime = LocalDateTime.of(2026, 4, 6, 14, 30),
          taxYear = 2026
        )

        val result = service.buildSubmissionReceiptViewModel(
          VerificationHistoryData(Seq(requestData)),
          1L,
          instanceId
        )

        result.value.submissionTime mustBe "14:30"
        result.value.submissionDate mustBe "6 April 2026"
        result.value.contractorName mustBe "Test Scheme"
        result.value.employerReference mustBe "123PA000001"
        result.value.receiptReferenceNumber mustBe receiptReferenceNumber
        result.value.verificationNumber mustBe "V001"
      }
    }

    "getSubmittedVerificationTaxYears" - {

      "must return distinct tax years sorted descending" in {
        service.getSubmittedVerificationTaxYears(data) mustBe Seq(
          TaxYearPeriod(2026),
          TaxYearPeriod(2025)
        )
      }
    }
  }
}
