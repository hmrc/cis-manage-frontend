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

import base.SpecBase

import java.time.Instant
import models.*
import org.scalatest.matchers.should.Matchers.*
import pages.SubmittedReturnsDataPage
import viewmodels.StatusViewModel.Text

class SubmittedReturnsServiceSpec extends SpecBase {

  private val service = new SubmittedReturnsService()

  private val submittedReturnsData = SubmittedReturnsData(
    scheme = SubmittedSchemeData(
      name = "Test Scheme",
      taxOfficeNumber = "123",
      taxOfficeReference = "ABC123"
    ),
    monthlyReturn = Seq(
      SubmittedMonthlyReturnData(
        monthlyReturnId = 1L,
        taxYear = 2023,
        taxMonth = 3,
        nilReturnIndicator = "N",
        status = "SUBMITTED",
        supersededBy = None,
        amendmentStatus = None,
        monthlyReturnItems = None
      ),
      SubmittedMonthlyReturnData(
        monthlyReturnId = 2L,
        taxYear = 2022,
        taxMonth = 12,
        nilReturnIndicator = "N",
        status = "SUBMITTED_NO_RECEIPT",
        supersededBy = None,
        amendmentStatus = None,
        monthlyReturnItems = None
      )
    ),
    submissions = Seq(
      SubmittedSubmissionData(
        submissionId = 11L,
        submissionType = Some("Original"),
        activeObjectId = 1L,
        status = "Accepted",
        hmrcMarkGenerated = None,
        hmrcMarkGgis = None,
        emailRecipient = None,
        acceptedTime = Some(Instant.parse("2024-04-01T10:15:30Z"))
      )
    )
  )

  private val userAnswers =
    emptyUserAnswers.set(SubmittedReturnsDataPage, submittedReturnsData).success.value

  "SubmittedReturnsService" - {

    "buildAllYearsViewModel must return a view model when data exists" in {
      val result = service.buildAllYearsViewModel(userAnswers)

      result.value.selectedTaxYear                shouldBe None
      result.value.taxYears.map(_.taxYearCaption) shouldBe Seq(
        "Tax year 2023 to 2024",
        "Tax year 2022 to 2023"
      )

      val firstRow = result.value.taxYears.head.rows.head
      firstRow.returnPeriodEnd        shouldBe "March 2023"
      firstRow.dateSubmitted          shouldBe "1 April 2024"
      firstRow.monthlyReturn.text     shouldBe "Print"
      firstRow.submissionReceipt.text shouldBe "View"
      firstRow.status                 shouldBe Text("Amend")
    }

    "buildSingleYearViewModel must return only the selected tax year" in {
      val result = service.buildSingleYearViewModel(userAnswers, "2023")

      result.value.selectedTaxYear                         shouldBe Some("2023")
      result.value.taxYears.map(_.taxYearCaption)          shouldBe Seq("Tax year 2023 to 2024")
      result.value.taxYears.head.rows.head.returnPeriodEnd shouldBe "March 2023"
    }

    "return None when SubmittedReturnsDataPage is missing" in {
      service.buildAllYearsViewModel(emptyUserAnswers)           shouldBe None
      service.buildSingleYearViewModel(emptyUserAnswers, "2023") shouldBe None
    }
  }
}
