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

import models.verify.{VerificationHistoryData, VerificationRequestData}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import java.time.LocalDate
import models.response.*
import java.time.LocalDateTime

class VerificationHistoryServiceSpec extends AnyFreeSpec with Matchers {

  private val service    = new VerificationHistoryService()
  private val instanceId = "900063"

  private val data = VerificationHistoryData(
    verificationRequests = Seq(
      VerificationRequestData("V001", LocalDate.of(2026, 4, 6), 2026),
      VerificationRequestData("V002", LocalDate.of(2026, 6, 6), 2026),
      VerificationRequestData("V003", LocalDate.of(2025, 4, 6), 2025),
      VerificationRequestData("V004", LocalDate.of(2025, 6, 6), 2025)
    )
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

  private def submittedSubmission(
    activeObjectId: Option[Long],
    submissionRequestDate: Option[LocalDateTime] = None,
    createDate: Option[LocalDateTime] = None
  ): GetSubmittedSubmission =
    GetSubmittedSubmission(
      submissionId = 1L,
      submissionType = "Verification",
      activeObjectId = activeObjectId,
      status = None,
      hmrcMarkGenerated = None,
      hmrcMarkGgis = None,
      emailRecipient = None,
      acceptedTime = None,
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
        row.verificationRequestLink must include("verificationNumber=V002")
      }

      "must set submission receipt link to dead link" in {
        val result = service.buildAllYearsViewModel(data, instanceId)

        val vm  = result.get
        val row = vm.taxYears.head.rows.head
        row.submissionReceiptLink mustBe "#"
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

      "must map submitted verification batches to history data using submission request date and calculate tax years" in {
        val response = GetSubmittedVerificationsResponse(
          scheme = Seq.empty,
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
              submissionRequestDate = Some(LocalDateTime.of(2026, 4, 6, 10, 0))
            ),
            submittedSubmission(
              activeObjectId = Some(2L),
              submissionRequestDate = Some(LocalDateTime.of(2026, 2, 6, 10, 0))
            )
          )
        )

        service.toVerificationHistoryData(response) mustBe VerificationHistoryData(
          verificationRequests = Seq(
            VerificationRequestData(
              verificationNumber = "V001",
              dateSubmitted = LocalDate.of(2026, 4, 6),
              taxYear = 2026
            ),
            VerificationRequestData(
              verificationNumber = "V002",
              dateSubmitted = LocalDate.of(2026, 2, 6),
              taxYear = 2025
            )
          )
        )
      }

      "must use submission create date when submission request date is absent" in {
        val response = GetSubmittedVerificationsResponse(
          scheme = Seq.empty,
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
              createDate = Some(LocalDateTime.of(2026, 6, 6, 10, 0))
            )
          )
        )

        service.toVerificationHistoryData(response) mustBe VerificationHistoryData(
          verificationRequests = Seq(
            VerificationRequestData(
              verificationNumber = "V001",
              dateSubmitted = LocalDate.of(2026, 6, 6),
              taxYear = 2026
            )
          )
        )
      }

      "must use verification batch create date when there is no matching submission" in {
        val response = GetSubmittedVerificationsResponse(
          scheme = Seq.empty,
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

        service.toVerificationHistoryData(response) mustBe VerificationHistoryData(
          verificationRequests = Seq(
            VerificationRequestData(
              verificationNumber = "V001",
              dateSubmitted = LocalDate.of(2026, 3, 31),
              taxYear = 2025
            )
          )
        )
      }

      "must exclude batches without a verification number or a usable date" in {
        val response = GetSubmittedVerificationsResponse(
          scheme = Seq.empty,
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
          submissions = Seq.empty
        )

        service.toVerificationHistoryData(response) mustBe VerificationHistoryData(
          verificationRequests = Seq.empty
        )
      }

      "must use the newest submission date when more than one submission exists for a batch" in {
        val response = GetSubmittedVerificationsResponse(
          scheme = Seq.empty,
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
              submissionRequestDate = Some(LocalDateTime.of(2026, 4, 6, 10, 0))
            ),
            submittedSubmission(
              activeObjectId = Some(1L),
              submissionRequestDate = Some(LocalDateTime.of(2026, 6, 6, 10, 0))
            )
          )
        )

        service.toVerificationHistoryData(response) mustBe VerificationHistoryData(
          verificationRequests = Seq(
            VerificationRequestData(
              verificationNumber = "V001",
              dateSubmitted = LocalDate.of(2026, 6, 6),
              taxYear = 2026
            )
          )
        )
      }
    }
  }
}
