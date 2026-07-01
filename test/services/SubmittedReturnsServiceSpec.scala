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
import config.FrontendAppConfig
import connectors.ConstructionIndustrySchemeConnector

import java.time.Instant
import models.MonthlyReturnItem
import models.history.*
import models.history.SubmittedReturnsHistorySource.SingleYear
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import models.response.GetSubmittedMonthlyReturnsDataResponse
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.i18n.Lang
import viewmodels.*
import viewmodels.StatusViewModel.Text

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmittedReturnsServiceSpec extends SpecBase with MockitoSugar {

  private val instanceId = "1"

  private val amendUrl = "/construction-industry-scheme/management/history/amend/2023/3"

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

  trait Setup {
    val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
    val mockAppConfig: FrontendAppConfig                   = mock[FrontendAppConfig]

    when(mockAppConfig.continueAmendReturnJourneyUrl(any(), any(), any())).thenReturn("#")

    implicit val appConfig: FrontendAppConfig = mockAppConfig

    val service: SubmittedReturnsService = new SubmittedReturnsService(mockConnector)

    def singleRow(testData: SubmittedReturnsData): SubmittedReturnsRowViewModel =
      service.buildAllYearsViewModel(testData, instanceId).value.taxYears.head.rows.head
  }

  "SubmittedReturnsService" - {

    "buildAllYearsViewModel returns expected row for happy path" in new Setup {
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
      row.status                   shouldBe StatusViewModel.Link(
        link = LinkViewModel(
          url = amendUrl,
          hiddenText = "Mar 2023"
        ),
        textKey = "history.returnHistory.status.amend",
        hiddenTextKey = "history.returnHistory.hidden.status.amend"
      )
    }

    "buildSingleYearViewModel returns only the selected tax year" in new Setup {
      val testData = data(
        monthlyReturns = Seq(monthlyReturn()),
        submissions = Seq(submission())
      )

      val result = service.buildSingleYearViewModel(testData, "2022", instanceId)

      result.value.selectedTaxYear                           shouldBe Some("2022")
      result.value.taxYears.map(t => (t.fromYear, t.toYear)) shouldBe Seq(2022 -> 2023)

      result.value.taxYears.head.rows.head.monthlyReturn.url shouldBe
        controllers.history.routes.PrintSubmissionDetailsController
          .onPageLoad(
            monthlyReturn().taxYear,
            monthlyReturn().taxMonth,
            monthlyReturn().amendment,
            SingleYear.queryValue
          )
          .url
    }

    "buildSingleYearViewModel returns None for invalid tax year" in new Setup {
      val testData = data(
        monthlyReturns = Seq(monthlyReturn()),
        submissions = Seq(submission())
      )

      service.buildSingleYearViewModel(testData, "abc", instanceId) shouldBe None
    }

    "uses Unknown return type for unhandled nilReturnIndicator" in new Setup {
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

    "returns notAvailable when acceptedTime is missing" in new Setup {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(id = 3L)
          ),
          submissions = Seq(
            submission(submissionId = 13L, activeObjectId = Some(3L), acceptedTime = None)
          )
        )
      )

      row.status shouldBe StatusViewModel.Text("history.returnHistory.status.notAvailable")
    }

    "returns awaitingConfirmation for SUBMITTED_NO_RECEIPT" in new Setup {
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

    "returns notAvailable for SUBMITTED before amendment cutoff" in new Setup {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(id = 5L)
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

    "returns inProgress for superseded return with amendment status STARTED" in new Setup {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(
              id = 6L,
              supersededBy = Some(99L),
              amendmentStatus = Some("STARTED")
            )
          ),
          submissions = Seq(
            submission(submissionId = 16L, activeObjectId = Some(6L))
          )
        )
      )

      row.status shouldBe StatusViewModel.Link(
        link = LinkViewModel(
          url = controllers.history.routes.SubmittedReturnsController.onInProgressRedirect(6L).url,
          hiddenText = "Mar 2023"
        ),
        textKey = "history.returnHistory.status.inProgress",
        hiddenTextKey = "history.returnHistory.hidden.status.inProgress"
      )
    }

    "returns awaitingConfirmation for superseded return with amendment status PENDING" in new Setup {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(
              id = 7L,
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

    "returns amend for superseded return with amendment status SUBMITTED" in new Setup {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(
              id = 8L,
              supersededBy = Some(99L),
              amendmentStatus = Some("SUBMITTED")
            )
          ),
          submissions = Seq(
            submission(submissionId = 18L, activeObjectId = Some(8L))
          )
        )
      )

      row.status shouldBe StatusViewModel.Link(
        link = LinkViewModel(
          url = amendUrl,
          hiddenText = "Mar 2023"
        ),
        textKey = "history.returnHistory.status.amend",
        hiddenTextKey = "history.returnHistory.hidden.status.amend"
      )
    }

    "returns notAvailable for superseded return with amendment status FATAL_ERROR" in new Setup {
      val row = singleRow(
        data(
          monthlyReturns = Seq(
            monthlyReturn(
              id = 9L,
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

    "returns empty text for unhandled monthly return status" in new Setup {
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

    "submissionReceipt is a Link when IRMark sent and received match" in new Setup {
      val row = singleRow(
        data(
          monthlyReturns = Seq(monthlyReturn()),
          submissions = Seq(
            submission().copy(
              hmrcMarkGenerated = Some("MARK-ABC"),
              hmrcMarkGgis = Some("MARK-ABC")
            )
          )
        )
      )

      row.submissionReceipt shouldBe a[StatusViewModel.Link]
      val link = row.submissionReceipt.asInstanceOf[StatusViewModel.Link]
      link.link.url should include("taxYear=2023")
      link.link.url should include("taxMonth=3")
      link.link.url should include("amendment=N")
    }

    "submissionReceipt is empty Text when IRMarks do not match" in new Setup {
      val row = singleRow(
        data(
          monthlyReturns = Seq(monthlyReturn()),
          submissions = Seq(
            submission().copy(
              hmrcMarkGenerated = Some("MARK-A"),
              hmrcMarkGgis = Some("MARK-B")
            )
          )
        )
      )

      row.submissionReceipt shouldBe StatusViewModel.Text("")
    }

    "submissionReceipt is empty Text when both IRMarks are None" in new Setup {
      val row = singleRow(
        data(
          monthlyReturns = Seq(monthlyReturn()),
          submissions = Seq(
            submission().copy(
              hmrcMarkGenerated = None,
              hmrcMarkGgis = None
            )
          )
        )
      )

      row.submissionReceipt shouldBe StatusViewModel.Text("")
    }

    "createAmendmentHandoff must create journey handoff and return handoff id" in new Setup {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val handoffId = "handoff-123"

      val testData = data(
        monthlyReturns = Seq(
          monthlyReturn(
            id = 1L,
            taxMonth = 3,
            nilReturnIndicator = "Standard"
          )
        ),
        submissions = Seq(
          submission(
            activeObjectId = Some(1L),
            acceptedTime = Some(Instant.parse("2024-04-01T10:15:30Z"))
          )
        )
      )

      val expectedPayload = Json.obj(
        "instanceId"         -> instanceId,
        "taxYear"            -> 2023,
        "taxMonth"           -> 3,
        "contractorName"     -> "Test Scheme",
        "originalReturnType" -> "MonthlyStandardReturn",
        "acceptedTime"       -> "2024-04-01T10:15:30Z"
      )

      when(
        mockConnector.createJourneyHandoff(
          "amend-monthly-return",
          expectedPayload
        )
      ).thenReturn(Future.successful(handoffId))

      val result = service
        .createAmendmentHandoff(
          data = testData,
          instanceId = instanceId,
          taxYear = 2023,
          taxMonth = 3
        )
        .futureValue

      result shouldBe Right(handoffId)

      verify(mockConnector).createJourneyHandoff(
        "amend-monthly-return",
        expectedPayload
      )
    }

    "createAmendmentHandoff must create payload without acceptedTime when matching submission has no acceptedTime" in new Setup {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val handoffId = "handoff-123"

      val testData = data(
        monthlyReturns = Seq(
          monthlyReturn(
            id = 1L,
            taxMonth = 3,
            nilReturnIndicator = "Nil"
          )
        ),
        submissions = Seq(
          submission(
            activeObjectId = Some(1L),
            acceptedTime = None
          )
        )
      )

      val expectedPayload = Json.obj(
        "instanceId"         -> instanceId,
        "taxYear"            -> 2023,
        "taxMonth"           -> 3,
        "contractorName"     -> "Test Scheme",
        "originalReturnType" -> "MonthlyNilReturn"
      )

      when(
        mockConnector.createJourneyHandoff(
          "amend-monthly-return",
          expectedPayload
        )
      ).thenReturn(Future.successful(handoffId))

      val result = service
        .createAmendmentHandoff(
          data = testData,
          instanceId = instanceId,
          taxYear = 2023,
          taxMonth = 3
        )
        .futureValue

      result shouldBe Right(handoffId)

      verify(mockConnector).createJourneyHandoff(
        "amend-monthly-return",
        expectedPayload
      )
    }

    "createAmendmentHandoff must return Left when no matching monthly return exists" in new Setup {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val testData = data(
        monthlyReturns = Seq(
          monthlyReturn(id = 1L, taxMonth = 3)
        ),
        submissions = Seq(
          submission(activeObjectId = Some(1L))
        )
      )

      val result = service
        .createAmendmentHandoff(
          data = testData,
          instanceId = instanceId,
          taxYear = 2025,
          taxMonth = 4
        )
        .futureValue

      result shouldBe Left("No monthly return found for tax year 2025 and month 4")

      verify(mockConnector, never()).createJourneyHandoff(any[String], any[JsObject])(any[HeaderCarrier])
    }

    "createAmendmentHandoff must propagate connector failure" in new Setup {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val testData = data(
        monthlyReturns = Seq(
          monthlyReturn(id = 1L, taxMonth = 3)
        ),
        submissions = Seq(
          submission(activeObjectId = Some(1L))
        )
      )

      when(
        mockConnector.createJourneyHandoff(
          any[String],
          any[JsObject]
        )(any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val result = service.createAmendmentHandoff(
        data = testData,
        instanceId = instanceId,
        taxYear = 2023,
        taxMonth = 3
      )

      whenReady(result.failed) { ex =>
        ex.getMessage shouldBe "boom"
      }
    }

    "getMonthlyReturnComplete must build a SubmissionReceiptViewModel from connector response" in new Setup {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val response = MonthlyReturnCompleteResponse(
        scheme = Seq(CompleteSchemeData(1, "INST001", "123P", "123", "ABC456", None, Some("Test Contractor"), None)),
        monthlyReturn =
          Seq(CompleteMonthlyReturnData(100L, 2024, 6, Some("N"), None, None, Some("SUBMITTED"), None, None, None)),
        subcontractors = Seq.empty,
        monthlyReturnItems = Seq(
          CompleteMonthlyReturnItemData(
            100L,
            301L,
            Some("5000.00"),
            Some("1000.00"),
            Some("800.00"),
            Some(200L),
            Some("John Smith"),
            None
          )
        ),
        submission = Seq(
          CompleteSubmissionData(
            400L,
            "Original",
            Some(100L),
            Some("SUBMITTED"),
            Some("HMRC-123-ABC"),
            Some("HMRC-123-ABC"),
            Some("user@example.com"),
            Some("2024-07-01T10:30:00")
          )
        )
      )

      when(mockConnector.getMonthlyReturnComplete("INST001", 2024, 6, "N"))
        .thenReturn(Future.successful(response))

      val result = service.getMonthlyReturnComplete("INST001", 2024, 6, "N").futureValue

      result shouldBe a[Right[_, _]]
      val vm = result.toOption.get
      vm.contractorName  shouldBe "Test Contractor"
      vm.payeReference   shouldBe "123/ABC456"
      vm.taxYear         shouldBe 2024
      vm.taxMonth        shouldBe 6
      vm.returnPeriodEnd shouldBe "June 2024"
      vm.returnType      shouldBe "submissionConfirmation.returnType.monthly"
      vm.submissionType  shouldBe "Original"
      vm.hmrcMark        shouldBe Some("HMRC-123-ABC")
      vm.emailRecipient  shouldBe Some("user@example.com")
      vm.instanceId      shouldBe "INST001"
      vm.items.size      shouldBe 1

      val item = vm.items.head
      item.name            shouldBe "John Smith"
      item.paymentsMade    shouldBe "£5,000.00"
      item.costOfMaterials shouldBe "£1,000.00"
      item.taxDeducted     shouldBe "£800.00"
    }

    "getMonthlyReturnComplete must identify nil returns correctly" in new Setup {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val response = MonthlyReturnCompleteResponse(
        scheme = Seq(CompleteSchemeData(1, "INST001", "123P", "123", "ABC456", None, Some("Nil Co"), None)),
        monthlyReturn =
          Seq(CompleteMonthlyReturnData(100L, 2024, 3, Some("Y"), None, None, Some("SUBMITTED"), None, None, None)),
        subcontractors = Seq.empty,
        monthlyReturnItems = Seq.empty,
        submission = Seq(
          CompleteSubmissionData(
            400L,
            "Original",
            Some(100L),
            Some("SUBMITTED"),
            Some("MARK-ABC"),
            Some("MARK-ABC"),
            None,
            None
          )
        )
      )

      when(mockConnector.getMonthlyReturnComplete(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(response))

      val result = service.getMonthlyReturnComplete("INST001", 2024, 3, "N").futureValue

      result shouldBe a[Right[_, _]]
      val vm = result.toOption.get
      vm.returnType     shouldBe "submissionConfirmation.returnType.nil"
      vm.contractorName shouldBe "Nil Co"
      vm.items          shouldBe empty
      vm.submittedAt    shouldBe None
    }

    "getMonthlyReturnComplete must fail guard when status is not SUBMITTED and amendment is not Y" in new Setup {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val response = MonthlyReturnCompleteResponse(
        scheme = Seq(CompleteSchemeData(1, "INST001", "123P", "123", "ABC456", None, Some("Test Co"), None)),
        monthlyReturn =
          Seq(CompleteMonthlyReturnData(100L, 2024, 6, Some("N"), None, None, Some("SUBMITTED"), None, None, None)),
        subcontractors = Seq.empty,
        monthlyReturnItems = Seq.empty,
        submission = Seq(
          CompleteSubmissionData(
            400L,
            "Original",
            Some(100L),
            Some("Accepted"),
            Some("MARK-A"),
            Some("MARK-A"),
            None,
            None
          )
        )
      )

      when(mockConnector.getMonthlyReturnComplete(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(response))

      val result = service.getMonthlyReturnComplete("INST001", 2024, 6, "N").futureValue

      result                 shouldBe a[Left[_, _]]
      result.left.toOption.get should include("guard failed")
    }

    "getMonthlyReturnComplete must pass guard when amendment is Y even if status is not SUBMITTED" in new Setup {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val response = MonthlyReturnCompleteResponse(
        scheme = Seq(CompleteSchemeData(1, "INST001", "123P", "123", "ABC456", None, Some("Test Co"), None)),
        monthlyReturn =
          Seq(CompleteMonthlyReturnData(100L, 2024, 6, Some("N"), None, None, Some("SUBMITTED"), None, None, None)),
        subcontractors = Seq.empty,
        monthlyReturnItems = Seq.empty,
        submission = Seq(
          CompleteSubmissionData(
            400L,
            "Original",
            Some(100L),
            Some("Accepted"),
            Some("MARK-A"),
            Some("MARK-A"),
            None,
            None
          )
        )
      )

      when(mockConnector.getMonthlyReturnComplete(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(response))

      val result = service.getMonthlyReturnComplete("INST001", 2024, 6, "Y").futureValue

      result shouldBe a[Right[_, _]]
    }

    "getMonthlyReturnComplete must fail guard when IRMarks do not match" in new Setup {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val response = MonthlyReturnCompleteResponse(
        scheme = Seq(CompleteSchemeData(1, "INST001", "123P", "123", "ABC456", None, Some("Test Co"), None)),
        monthlyReturn =
          Seq(CompleteMonthlyReturnData(100L, 2024, 6, Some("N"), None, None, Some("SUBMITTED"), None, None, None)),
        subcontractors = Seq.empty,
        monthlyReturnItems = Seq.empty,
        submission = Seq(
          CompleteSubmissionData(
            400L,
            "Original",
            Some(100L),
            Some("SUBMITTED"),
            Some("MARK-A"),
            Some("MARK-B"),
            None,
            None
          )
        )
      )

      when(mockConnector.getMonthlyReturnComplete(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(response))

      val result = service.getMonthlyReturnComplete("INST001", 2024, 6, "N").futureValue

      result                 shouldBe a[Left[_, _]]
      result.left.toOption.get should include("IRMark")
    }

    "getMonthlyReturnComplete must fail guard when IRMark is null" in new Setup {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val response = MonthlyReturnCompleteResponse(
        scheme = Seq(CompleteSchemeData(1, "INST001", "123P", "123", "ABC456", None, Some("Test Co"), None)),
        monthlyReturn =
          Seq(CompleteMonthlyReturnData(100L, 2024, 6, Some("N"), None, None, Some("SUBMITTED"), None, None, None)),
        subcontractors = Seq.empty,
        monthlyReturnItems = Seq.empty,
        submission = Seq(
          CompleteSubmissionData(400L, "Original", Some(100L), Some("SUBMITTED"), None, None, None, None)
        )
      )

      when(mockConnector.getMonthlyReturnComplete(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(response))

      val result = service.getMonthlyReturnComplete("INST001", 2024, 6, "N").futureValue

      result                 shouldBe a[Left[_, _]]
      result.left.toOption.get should include("IRMark")
    }

    "SubmittedReturnPrintViewModel should return correct data without payment details" in new Setup {
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

    "SubmittedReturnPrintViewModel should return correct data with payment details" in new Setup {
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
            totalPayments = Some("10,000.00"),
            costOfMaterials = Some("10,000.00"),
            totalDeducted = Some("10,000.00"),
            unmatchedTaxRateIndicator = None,
            subcontractorId = None,
            subcontractorName = Some("Contractor 01"),
            verificationNumber = None,
            itemResourceReference = None
          ),
          MonthlyReturnItem(
            monthlyReturnId = 3000L,
            monthlyReturnItemId = 2L,
            totalPayments = Some("20,000.00"),
            costOfMaterials = Some("20,000.00"),
            totalDeducted = Some("20,000.00"),
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
      out.totalPaymentsMade mustBe "£30,000.00"
      out.totalCostOfMaterials mustBe "£30,000.00"
      out.totalTaxDeducted mustBe "£30,000.00"
      out.subcontractors mustBe Seq(
        SubcontractorPayment("Contractor 01", "£10,000.00", "£10,000.00", "£10,000.00"),
        SubcontractorPayment("Contractor 02", "£20,000.00", "£20,000.00", "£20,000.00")
      )

    }
  }
}
