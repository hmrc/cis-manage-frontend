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
    monthlyReturns = Seq(
      SubmittedMonthlyReturnData(
        monthlyReturnId = 1L,
        taxYear = 2023,
        taxMonth = 3,
        nilReturnIndicator = "Standard",
        status = "SUBMITTED",
        supersededBy = None,
        amendmentStatus = None,
        monthlyReturnItems = None
      ),
      SubmittedMonthlyReturnData(
        monthlyReturnId = 2L,
        taxYear = 2022,
        taxMonth = 12,
        nilReturnIndicator = "Standard",
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
        activeObjectId = Some(1L),
        status = "Accepted",
        hmrcMarkGenerated = None,
        hmrcMarkGgis = None,
        emailRecipient = None,
        acceptedTime = Some(Instant.parse("2024-04-01T10:15:30Z"))
      )
    )
  )

  "SubmittedReturnsService" - {

    "buildAllYearsViewModel must return a view model when data exists" in {
      val result = service.buildAllYearsViewModel(submittedReturnsData)

      result.value.selectedTaxYear                                             shouldBe None
      result.value.taxYears.map(taxYear => (taxYear.fromYear, taxYear.toYear)) shouldBe Seq(
        2023 -> 2024
      )

      val firstRow = result.value.taxYears.head.rows.head
      firstRow.returnPeriodEnd          shouldBe "Mar 2023"
      firstRow.returnType               shouldBe ReturnTypeViewModel.Standard
      firstRow.dateSubmitted            shouldBe "1 Apr 2024"
      firstRow.monthlyReturn.url        shouldBe "#"
      firstRow.monthlyReturn.hiddenText shouldBe "Mar 2023"
    }

    "buildSingleYearViewModel must return only the selected tax year" in {
      val result = service.buildSingleYearViewModel(submittedReturnsData, "2023")

      result.value.selectedTaxYear                                             shouldBe Some("2023")
      result.value.taxYears.map(taxYear => (taxYear.fromYear, taxYear.toYear)) shouldBe Seq(
        2023 -> 2024
      )
      result.value.taxYears.head.rows.head.returnPeriodEnd                     shouldBe "Mar 2023"
    }

    "buildSingleYearViewModel must return None for invalid tax year" in {
      service.buildSingleYearViewModel(submittedReturnsData, "abc") shouldBe None
    }

    "buildAllYearsViewModel must use Unknown return type for unhandled nilReturnIndicator" in {
      val dataWithUnknownReturnType = submittedReturnsData.copy(
        monthlyReturns = Seq(
          SubmittedMonthlyReturnData(
            monthlyReturnId = 3L,
            taxYear = 2021,
            taxMonth = 6,
            nilReturnIndicator = "N",
            status = "SUBMITTED",
            supersededBy = None,
            amendmentStatus = None,
            monthlyReturnItems = None
          )
        ),
        submissions = Seq(
          SubmittedSubmissionData(
            submissionId = 12L,
            submissionType = Some("Original"),
            activeObjectId = Some(3L),
            status = "Accepted",
            hmrcMarkGenerated = None,
            hmrcMarkGgis = None,
            emailRecipient = None,
            acceptedTime = Some(Instant.parse("2024-04-01T10:15:30Z"))
          )
        )
      )

      val result = service.buildAllYearsViewModel(dataWithUnknownReturnType)

      val row = result.value.taxYears.head.rows.head
      row.returnType shouldBe ReturnTypeViewModel.Unknown
    }

    "buildAllYearsViewModel must use the raw status text for unhandled statuses" in {
      val submittedReturnsDataWithOtherStatus = SubmittedReturnsData(
        scheme = submittedReturnsData.scheme,
        monthlyReturns = Seq(
          SubmittedMonthlyReturnData(
            monthlyReturnId = 3L,
            taxYear = 2021,
            taxMonth = 6,
            nilReturnIndicator = "Standard",
            status = "IN_PROGRESS",
            supersededBy = None,
            amendmentStatus = None,
            monthlyReturnItems = None
          )
        ),
        submissions = Seq(
          SubmittedSubmissionData(
            submissionId = 13L,
            submissionType = Some("Original"),
            activeObjectId = Some(3L),
            status = "Accepted",
            hmrcMarkGenerated = None,
            hmrcMarkGgis = None,
            emailRecipient = None,
            acceptedTime = Some(Instant.parse("2024-04-01T10:15:30Z"))
          )
        )
      )

      val result = service.buildAllYearsViewModel(submittedReturnsDataWithOtherStatus)

      val row = result.value.taxYears.head.rows.head
      row.status shouldBe StatusViewModel.Text("IN_PROGRESS")
    }
  }
}
