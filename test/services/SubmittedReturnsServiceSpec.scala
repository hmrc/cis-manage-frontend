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
import models.history.*
import org.scalatest.matchers.should.Matchers.*
import pages.history.SubmittedReturnsDataPage
import viewmodels.{ReturnTypeViewModel, StatusViewModel}
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

      result.value.selectedTaxYear                                             shouldBe None
      result.value.taxYears.map(taxYear => (taxYear.fromYear, taxYear.toYear)) shouldBe Seq(
        2023 -> 2024,
        2022 -> 2023
      )

      val firstRow = result.value.taxYears.head.rows.head
      firstRow.returnPeriodEnd          shouldBe "Mar 2023"
      firstRow.returnType               shouldBe ReturnTypeViewModel.Nil
      firstRow.dateSubmitted            shouldBe "1 Apr 2024"
      firstRow.monthlyReturn.url        shouldBe "#"
      firstRow.monthlyReturn.hiddenText shouldBe "Mar 2023"
      firstRow.submissionReceipt        shouldBe StatusViewModel.Text("site.view")
      firstRow.status                   shouldBe StatusViewModel.Text("history.returnHistory.status.amend")
    }

    "buildSingleYearViewModel must return only the selected tax year" in {
      val result = service.buildSingleYearViewModel(userAnswers, "2023")

      result.value.selectedTaxYear                                             shouldBe Some("2023")
      result.value.taxYears.map(taxYear => (taxYear.fromYear, taxYear.toYear)) shouldBe Seq(
        2023 -> 2024
      )
      result.value.taxYears.head.rows.head.returnPeriodEnd                     shouldBe "Mar 2023"
    }

    "return None when SubmittedReturnsDataPage is missing" in {
      service.buildAllYearsViewModel(emptyUserAnswers)           shouldBe None
      service.buildSingleYearViewModel(emptyUserAnswers, "2023") shouldBe None
    }

    "buildAllYearsViewModel must use the raw status text for unhandled statuses" in {
      val submittedReturnsDataWithOtherStatus = submittedReturnsData.copy(
        monthlyReturn = submittedReturnsData.monthlyReturn :+ SubmittedMonthlyReturnData(
          monthlyReturnId = 3L,
          taxYear = 2021,
          taxMonth = 6,
          nilReturnIndicator = "N",
          status = "IN_PROGRESS",
          supersededBy = None,
          amendmentStatus = None,
          monthlyReturnItems = None
        )
      )

      val userAnswersWithOtherStatus =
        emptyUserAnswers.set(SubmittedReturnsDataPage, submittedReturnsDataWithOtherStatus).success.value

      val result = service.buildAllYearsViewModel(userAnswersWithOtherStatus)

      val row = result.value.taxYears
        .find(t => t.fromYear == 2021 && t.toYear == 2022)
        .value
        .rows
        .head

      row.status shouldBe StatusViewModel.Text("IN_PROGRESS")
    }
  }
}
