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
  }
}
