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
import models.MonthlyReturnItem
import models.history.*
import models.response.GetSubmittedMonthlyReturnsDataResponse
import org.scalatest.matchers.should.Matchers.*
import play.api.i18n.Lang
import viewmodels.StatusViewModel.Text
import viewmodels.{ReturnTypeViewModel, StatusViewModel, SubmittedReturnsRowViewModel}

import java.time.Instant

class SubmittedReturnsServiceSpec extends SpecBase {

  private val service = new SubmittedReturnsService()

  private val baseScheme = SubmittedSchemeData(
    name = "Test Scheme",
    taxOfficeNumber = "123",
    taxOfficeReference = "ABC123"
  )

  private def monthlyReturn(
    id: Long = 1L,
    taxYear: Int = 2023,
    taxMonth: Int = 3,
    nilReturnIndicator: String = "Standard",
    status: String = "SUBMITTED",
    supersededBy: Option[Long] = None,
    amendmentStatus: Option[String] = None,
    monthlyReturnItems: Option[String] = None
  ): SubmittedMonthlyReturnData =
    SubmittedMonthlyReturnData(
      monthlyReturnId = id,
      taxYear = taxYear,
      taxMonth = taxMonth,
      nilReturnIndicator = nilReturnIndicator,
      status = status,
      amendment = "N",
      supersededBy = supersededBy,
      amendmentStatus = amendmentStatus,
      monthlyReturnItems = monthlyReturnItems
    )

  private def submission(
    submissionId: Long = 11L,
    activeObjectId: Option[Long] = Some(1L),
    acceptedTime: Option[Instant] = Some(Instant.parse("2024-04-01T10:15:30Z"))
  ): SubmittedSubmissionData =
    SubmittedSubmissionData(
      submissionId = submissionId,
      submissionType = Some("Original"),
      activeObjectId = activeObjectId,
      status = "Accepted",
      hmrcMarkGenerated = None,
      hmrcMarkGgis = None,
      emailRecipient = None,
      acceptedTime = acceptedTime
    )

  private def data(
    monthlyReturns: Seq[SubmittedMonthlyReturnData],
    submissions: Seq[SubmittedSubmissionData]
  ): SubmittedReturnsData =
    SubmittedReturnsData(
      scheme = baseScheme,
      monthlyReturns = monthlyReturns,
      submissions = submissions
    )

  private def singleRow(testData: SubmittedReturnsData): SubmittedReturnsRowViewModel =
    service.buildAllYearsViewModel(testData).value.taxYears.head.rows.head

  "SubmittedReturnsService" - {

    "buildAllYearsViewModel returns expected row for happy path" in {
      val row = singleRow(
        data(
          monthlyReturns = Seq(monthlyReturn()),
          submissions = Seq(submission())
        )
      )

      row.returnPeriodEnd          shouldBe "Mar 2023"
      row.returnType               shouldBe ReturnTypeViewModel.Standard
      row.dateSubmitted            shouldBe "1 Apr 2024"
      row.monthlyReturn.url        shouldBe controllers.history.routes.PrintSubmissionDetailsController
        .onPageLoad(
          monthlyReturn().taxYear,
          monthlyReturn().taxMonth,
          monthlyReturn().amendment
        )
        .url
      row.monthlyReturn.hiddenText shouldBe "Mar 2023"
      row.status                   shouldBe StatusViewModel.Text("history.returnHistory.status.amend")
    }

    "buildSingleYearViewModel returns only the selected tax year" in {
      val testData = data(
        monthlyReturns = Seq(monthlyReturn()),
        submissions = Seq(submission())
      )

      val result = service.buildSingleYearViewModel(testData, "2023")

      result.value.selectedTaxYear                           shouldBe Some("2023")
      result.value.taxYears.map(t => (t.fromYear, t.toYear)) shouldBe Seq(2023 -> 2024)
    }

    "buildSingleYearViewModel returns None for invalid tax year" in {
      val testData = data(
        monthlyReturns = Seq(monthlyReturn()),
        submissions = Seq(submission())
      )

      service.buildSingleYearViewModel(testData, "abc") shouldBe None
    }

    "uses Unknown return type for unhandled nilReturnIndicator" in {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(id = 2L, nilReturnIndicator = "N")
          ),
          submissions = Seq(
            submission(submissionId = 12L, activeObjectId = Some(2L))
          )
        )
      )

      row.returnType shouldBe ReturnTypeViewModel.Unknown
    }

    "returns notAvailable when acceptedTime is missing" in {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(id = 3L, status = "SUBMITTED")
          ),
          submissions = Seq(
            submission(submissionId = 13L, activeObjectId = Some(3L), acceptedTime = None)
          )
        )
      )

      row.status shouldBe StatusViewModel.Text("history.returnHistory.status.notAvailable")
    }

    "returns awaitingConfirmation for SUBMITTED_NO_RECEIPT" in {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(id = 4L, status = "SUBMITTED_NO_RECEIPT")
          ),
          submissions = Seq(
            submission(submissionId = 14L, activeObjectId = Some(4L))
          )
        )
      )

      row.status shouldBe StatusViewModel.Text("history.returnHistory.status.awaitingConfirmation")
    }

    "returns notAvailable for SUBMITTED before amendment cutoff" in {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(id = 5L, status = "SUBMITTED")
          ),
          submissions = Seq(
            submission(
              submissionId = 15L,
              activeObjectId = Some(5L),
              acceptedTime = Some(Instant.parse("2013-01-01T00:00:00Z"))
            )
          )
        )
      )

      row.status shouldBe StatusViewModel.Text("history.returnHistory.status.notAvailable")
    }

    "returns inProgress for superseded return with amendment status STARTED" in {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(
              id = 6L,
              status = "SUBMITTED",
              supersededBy = Some(99L),
              amendmentStatus = Some("STARTED")
            )
          ),
          submissions = Seq(
            submission(submissionId = 16L, activeObjectId = Some(6L))
          )
        )
      )

      row.status shouldBe StatusViewModel.Text("history.returnHistory.status.inProgress")
    }

    "returns awaitingConfirmation for superseded return with amendment status PENDING" in {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(
              id = 7L,
              status = "SUBMITTED",
              supersededBy = Some(99L),
              amendmentStatus = Some("PENDING")
            )
          ),
          submissions = Seq(
            submission(submissionId = 17L, activeObjectId = Some(7L))
          )
        )
      )

      row.status shouldBe StatusViewModel.Text("history.returnHistory.status.awaitingConfirmation")
    }

    "returns amend for superseded return with amendment status SUBMITTED" in {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(
              id = 8L,
              status = "SUBMITTED",
              supersededBy = Some(99L),
              amendmentStatus = Some("SUBMITTED")
            )
          ),
          submissions = Seq(
            submission(submissionId = 18L, activeObjectId = Some(8L))
          )
        )
      )

      row.status shouldBe StatusViewModel.Text("history.returnHistory.status.amend")
    }

    "returns notAvailable for superseded return with amendment status FATAL_ERROR" in {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(
              id = 9L,
              status = "SUBMITTED",
              supersededBy = Some(99L),
              amendmentStatus = Some("FATAL_ERROR")
            )
          ),
          submissions = Seq(
            submission(submissionId = 19L, activeObjectId = Some(9L))
          )
        )
      )

      row.status shouldBe StatusViewModel.Text("history.returnHistory.status.notAvailable")
    }

    "returns empty text for unhandled monthly return status" in {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(id = 10L, status = "IN_PROGRESS")
          ),
          submissions = Seq(
            submission(submissionId = 20L, activeObjectId = Some(10L))
          )
        )
      )

      row.status shouldBe StatusViewModel.Text("")
    }

    "SubmittedReturnPrintViewModel should return correct data without payment details" in {
      val input = GetSubmittedMonthlyReturnsDataResponse(
        scheme = SubmittedSchemeData("PAL 355 Scheme", "163", "AB0063"),
        monthlyReturnId = 3000L,
        taxYear = 2026,
        taxMonth = 4,
        nilReturnIndicator = "Y",
        monthlyReturnItems = Seq.empty,
        submission = SubmittedSubmissionData(
          submissionId = 10L,
          submissionType = Some("Original"),
          activeObjectId = Some(20L),
          status = "Accepted",
          hmrcMarkGenerated = Some("mark1"),
          hmrcMarkGgis = Some("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"),
          emailRecipient = Some("test@example.com"),
          acceptedTime = Some(Instant.parse("2026-04-01T10:15:30Z"))
        )
      )

      val out = service.buildSubmittedReturnPrintViewModel(input, Lang("en"))
      out.monthYear mustBe "April 2026"
      out.submittedDate mustBe "1 April 2026"
      out.receiptReferenceNumber mustBe "AAIIGECRQ4QJFCZQ2OHUCFETKFKZOYM5W7RZ5OY"
      out.submissionType mustBe "nil"
      out.contractorName mustBe "PAL 355 Scheme"
      out.payeReference mustBe "163/AB0063"
      out.totalPaymentsMade mustBe "£0.00"
      out.totalCostOfMaterials mustBe "£0.00"
      out.totalTaxDeducted mustBe "£0.00"
      out.subcontractors mustBe Seq.empty

    }

    "SubmittedReturnPrintViewModel should return correct data with payment details" in {
      val input = GetSubmittedMonthlyReturnsDataResponse(
        scheme = SubmittedSchemeData("PAL 355 Scheme", "163", "AB0063"),
        monthlyReturnId = 3000L,
        taxYear = 2026,
        taxMonth = 4,
        nilReturnIndicator = "N",
        monthlyReturnItems = Seq(
          MonthlyReturnItem(
            monthlyReturnId = 3000L,
            monthlyReturnItemId = 1L,
            totalPayments = Some("100"),
            costOfMaterials = Some("100"),
            totalDeducted = Some("100"),
            unmatchedTaxRateIndicator = None,
            subcontractorId = None,
            subcontractorName = Some("Contractor 01"),
            verificationNumber = None,
            itemResourceReference = None
          ),
          MonthlyReturnItem(
            monthlyReturnId = 3000L,
            monthlyReturnItemId = 2L,
            totalPayments = Some("200"),
            costOfMaterials = Some("200"),
            totalDeducted = Some("200"),
            unmatchedTaxRateIndicator = None,
            subcontractorId = None,
            subcontractorName = Some("Contractor 02"),
            verificationNumber = None,
            itemResourceReference = None
          )
        ),
        submission = SubmittedSubmissionData(
          submissionId = 10L,
          submissionType = Some("Original"),
          activeObjectId = Some(20L),
          status = "Accepted",
          hmrcMarkGenerated = Some("mark1"),
          hmrcMarkGgis = None,
          emailRecipient = Some("test@example.com"),
          acceptedTime = Some(Instant.parse("2026-04-01T10:15:30Z"))
        )
      )

      val out = service.buildSubmittedReturnPrintViewModel(input, Lang("en"))
      out.monthYear mustBe "April 2026"
      out.submittedDate mustBe "1 April 2026"
      out.receiptReferenceNumber mustBe ""
      out.submissionType mustBe "standard"
      out.contractorName mustBe "PAL 355 Scheme"
      out.payeReference mustBe "163/AB0063"
      out.totalPaymentsMade mustBe "£300.00"
      out.totalCostOfMaterials mustBe "£300.00"
      out.totalTaxDeducted mustBe "£300.00"
      out.subcontractors mustBe Seq(
        SubcontractorPayment("Contractor 01", "£100.00", "£100.00", "£100.00"),
        SubcontractorPayment("Contractor 02", "£200.00", "£200.00", "£200.00")
      )

    }
  }
}
