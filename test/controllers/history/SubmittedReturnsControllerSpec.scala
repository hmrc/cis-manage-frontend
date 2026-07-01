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

package controllers.history

import base.SpecBase
import models.UserAnswers
import models.history.{SubcontractorPayment, SubmittedMonthlyReturnData, SubmittedReturnsData, SubmittedSchemeData, SubmittedSubmissionData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.CisIdPage
import pages.history.SubmittedReturnsDataPage
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{ManageService, SubmittedReturnsService}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.*
import views.html.history.SubmittedReturnsView
import views.html.monthlyreturns.SubmissionSuccessView

import java.time.Instant
import scala.concurrent.Future

class SubmittedReturnsControllerSpec extends SpecBase with MockitoSugar {

  private val cisId = "900063"

  private val viewModel = SubmittedReturnsPageViewModel(
    taxYears = Seq(
      TaxYearHistoryViewModel(
        fromYear = 2023,
        toYear = 2024,
        rows = Seq(
          SubmittedReturnsRowViewModel(
            returnPeriodEnd = "Mar 2024",
            returnType = ReturnTypeViewModel.Nil,
            dateSubmitted = "1 Apr 2024",
            monthlyReturn = LinkViewModel("/return/1", "Mar 2024"),
            submissionReceipt = StatusViewModel.Text("site.view"),
            status = StatusViewModel.Text("history.returnHistory.status.amend")
          )
        )
      )
    ),
    selectedTaxYear = Some("2024")
  )

  private val submittedReturnsData = SubmittedReturnsData(
    scheme = SubmittedSchemeData(
      name = "Test Scheme",
      taxOfficeNumber = "123",
      taxOfficeReference = "AB456"
    ),
    monthlyReturns = Seq(
      SubmittedMonthlyReturnData(
        monthlyReturnId = 1L,
        taxYear = 2024,
        taxMonth = 3,
        nilReturnIndicator = "Nil",
        status = "Submitted",
        amendment = "N",
        supersededBy = None,
        amendmentStatus = None,
        monthlyReturnItems = None
      )
    ),
    submissions = Seq(
      SubmittedSubmissionData(
        submissionId = 1L,
        submissionType = Some("MONTHLY_RETURN"),
        activeObjectId = Some(1L),
        status = "Submitted",
        hmrcMarkGenerated = Some("123"),
        hmrcMarkGgis = Some("123"),
        emailRecipient = Some("test@example.com"),
        acceptedTime = Some(Instant.parse("2024-04-01T12:00:00Z"))
      )
    )
  )

  private val submittedReturnsDataStarted =
    submittedReturnsData.copy(
      monthlyReturns = submittedReturnsData.monthlyReturns.map(
        _.copy(amendmentStatus = Some("STARTED"))
      )
    )

  private val submittedReturnsDataValidated =
    submittedReturnsData.copy(
      monthlyReturns = submittedReturnsData.monthlyReturns.map(
        _.copy(amendmentStatus = Some("VALIDATED"))
      )
    )

  private val receiptViewModelWithEmail = SubmissionReceiptViewModel(
    contractorName = "Test Contractor",
    payeReference = "123/ABC456",
    taxYear = 2024,
    taxMonth = 6,
    returnPeriodEnd = "June 2024",
    returnType = "Monthly",
    submissionType = "Monthly return",
    hmrcMark = Some("HMRC-123"),
    submittedAt = Some("11:30am on 1 July 2024"),
    emailRecipient = Some("user@example.com"),
    instanceId = "1",
    items = Seq(
      SubcontractorPayment("John Smith", "5000.00", "1000.00", "800.00")
    )
  )

  private val receiptViewModelWithoutEmail =
    receiptViewModelWithEmail.copy(emailRecipient = None)

  trait Setup {

    val mockSubmittedReturnsService: SubmittedReturnsService = mock[SubmittedReturnsService]
    val mockManageService: ManageService                     = mock[ManageService]

    def application(userAnswers: UserAnswers): Application =
      applicationBuilder(userAnswers = Some(userAnswers))
        .configure(
          "mongodb.timeToLiveInSeconds" -> 900,
          "cis-frontend.host"           -> "http://localhost:6993",
          "urls.confirmAmendment"       -> "/construction-industry-scheme/manage-cis-return/amend-monthly-return/confirm-amendments"
        )
        .overrides(
          bind[SubmittedReturnsService].toInstance(mockSubmittedReturnsService),
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

    def userAnswersWithCisId: UserAnswers =
      emptyUserAnswers
        .set(CisIdPage, cisId)
        .success
        .value

    def userAnswersWithSubmittedReturnsData: UserAnswers =
      userAnswersWithCisId
        .set(SubmittedReturnsDataPage, submittedReturnsData)
        .success
        .value

    def userAnswersWithInstanceId: UserAnswers =
      emptyUserAnswers
        .set(CisIdPage, "1")
        .success
        .value

    def mockManageServiceReturnsData(): Unit =
      when(mockManageService.getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(submittedReturnsData))

    def mockManageServiceReturnsDataStarted(): Unit =
      when(mockManageService.getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(submittedReturnsDataStarted))

    def mockManageServiceReturnsDataValidated(): Unit =
      when(mockManageService.getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(submittedReturnsDataValidated))

    def mockManageServiceFails(): Unit =
      when(mockManageService.getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

    def mockSingleYearViewModelReturns(model: Option[SubmittedReturnsPageViewModel]): Unit =
      when(
        mockSubmittedReturnsService.buildSingleYearViewModel(
          any[SubmittedReturnsData],
          any[String]
        )
      ).thenReturn(model)

    def mockAllYearsViewModelReturns(model: Option[SubmittedReturnsPageViewModel]): Unit =
      when(
        mockSubmittedReturnsService.buildAllYearsViewModel(
          any[SubmittedReturnsData]
        )
      ).thenReturn(model)

    def mockCreateAmendmentHandoffReturns(result: Either[String, String]): Unit =
      when(
        mockSubmittedReturnsService.createAmendmentHandoff(
          any[SubmittedReturnsData],
          any[String],
          any[Int],
          any[Int]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(result))

    def mockCreateAmendmentHandoffFails(): Unit =
      when(
        mockSubmittedReturnsService.createAmendmentHandoff(
          any[SubmittedReturnsData],
          any[String],
          any[Int],
          any[Int]
        )(any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("boom")))

    def mockReceiptReturns(result: Either[String, SubmissionReceiptViewModel]): Unit =
      when(
        mockSubmittedReturnsService.getMonthlyReturnComplete(
          any[String],
          any[Int],
          any[Int],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(result))

    def mockReceiptFails(): Unit =
      when(
        mockSubmittedReturnsService.getMonthlyReturnComplete(
          any[String],
          any[Int],
          any[Int],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("upstream error")))

    def confirmAmendmentUrl(handoffId: String): String =
      s"http://localhost:6993/construction-industry-scheme/manage-cis-return/amend-monthly-return/confirm-amendments?handoffId=$handoffId"

    def unauthorisedUrl: String =
      controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad().url

    def journeyRecoveryUrl: String =
      controllers.routes.JourneyRecoveryController.onPageLoad().url
  }

  "SubmittedReturnsController" - {

    "onPageLoadSingleYear must return OK using SubmittedReturnsDataPage when present" in new Setup {
      val userAnswers = userAnswersWithSubmittedReturnsData

      when(mockSubmittedReturnsService.buildSingleYearViewModel(submittedReturnsData, "2024"))
        .thenReturn(Some(viewModel))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadSingleYear("2024").url)
        val result  = route(app, request).value
        val view    = app.injector.instanceOf[SubmittedReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(app)).toString

        verify(mockSubmittedReturnsService).buildSingleYearViewModel(submittedReturnsData, "2024")
        verifyNoInteractions(mockManageService)
      }
    }

    "onPageLoadAllYears must return OK using SubmittedReturnsDataPage when present" in new Setup {
      val userAnswers = userAnswersWithSubmittedReturnsData

      when(mockSubmittedReturnsService.buildAllYearsViewModel(submittedReturnsData))
        .thenReturn(Some(viewModel))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadAllYears().url)
        val result  = route(app, request).value
        val view    = app.injector.instanceOf[SubmittedReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(app)).toString

        verify(mockSubmittedReturnsService).buildAllYearsViewModel(submittedReturnsData)
        verifyNoInteractions(mockManageService)
      }
    }

    "onPageLoadSingleYear must fetch data from manageService when SubmittedReturnsDataPage is missing and CisIdPage exists" in new Setup {
      val userAnswers = userAnswersWithCisId

      mockManageServiceReturnsData()
      mockSingleYearViewModelReturns(Some(viewModel))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadSingleYear("2024").url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        verify(mockManageService).getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier])
        verify(mockSubmittedReturnsService).buildSingleYearViewModel(any[SubmittedReturnsData], any[String])
      }
    }

    "onPageLoadAllYears must fetch data from manageService when SubmittedReturnsDataPage is missing and CisIdPage exists" in new Setup {
      val userAnswers = userAnswersWithCisId

      mockManageServiceReturnsData()
      mockAllYearsViewModelReturns(Some(viewModel))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadAllYears().url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        verify(mockManageService).getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier])
        verify(mockSubmittedReturnsService).buildAllYearsViewModel(any[SubmittedReturnsData])
      }
    }

    "onPageLoadSingleYear must redirect when CisIdPage is missing" in new Setup {
      val app = application(emptyUserAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadSingleYear("2024").url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual unauthorisedUrl
      }
    }

    "onPageLoadAllYears must redirect when CisIdPage is missing" in new Setup {
      val app = application(emptyUserAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadAllYears().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual unauthorisedUrl
      }
    }

    "onPageLoadSingleYear must redirect to JourneyRecovery when buildSingleYearViewModel returns None" in new Setup {
      val userAnswers = userAnswersWithSubmittedReturnsData

      when(mockSubmittedReturnsService.buildSingleYearViewModel(submittedReturnsData, "2024"))
        .thenReturn(None)

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadSingleYear("2024").url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl

        verify(mockSubmittedReturnsService).buildSingleYearViewModel(submittedReturnsData, "2024")
      }
    }

    "onPageLoadSingleYear must redirect to JourneyRecovery when resolveSubmittedReturnsData fails" in new Setup {
      val userAnswers = userAnswersWithCisId

      mockManageServiceFails()

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadSingleYear("2024").url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl

        verify(mockManageService).getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier])
      }
    }

    "onPageLoadAllYears must redirect to JourneyRecovery when buildAllYearsViewModel returns None" in new Setup {
      val userAnswers = userAnswersWithSubmittedReturnsData

      when(mockSubmittedReturnsService.buildAllYearsViewModel(submittedReturnsData))
        .thenReturn(None)

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadAllYears().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl

        verify(mockSubmittedReturnsService).buildAllYearsViewModel(submittedReturnsData)
      }
    }

    "onPageLoadAllYears must redirect to JourneyRecovery when resolveSubmittedReturnsData fails" in new Setup {
      val userAnswers = userAnswersWithCisId

      mockManageServiceFails()

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadAllYears().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl

        verify(mockManageService).getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier])
      }
    }

    "startAmendment must create handoff and redirect to confirm amendment URL" in new Setup {
      val userAnswers = userAnswersWithSubmittedReturnsData
      val handoffId   = "handoff-123"

      mockCreateAmendmentHandoffReturns(Right(handoffId))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.startAmendment(2024, 3).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual confirmAmendmentUrl(handoffId)

        verify(mockSubmittedReturnsService).createAmendmentHandoff(
          any[SubmittedReturnsData],
          any[String],
          any[Int],
          any[Int]
        )(any[HeaderCarrier])

        verifyNoInteractions(mockManageService)
      }
    }

    "startAmendment must fetch data from manageService when SubmittedReturnsDataPage is missing" in new Setup {
      val userAnswers = userAnswersWithCisId
      val handoffId   = "handoff-123"

      mockManageServiceReturnsData()
      mockCreateAmendmentHandoffReturns(Right(handoffId))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.startAmendment(2024, 3).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual confirmAmendmentUrl(handoffId)

        verify(mockManageService).getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier])

        verify(mockSubmittedReturnsService).createAmendmentHandoff(
          any[SubmittedReturnsData],
          any[String],
          any[Int],
          any[Int]
        )(any[HeaderCarrier])
      }
    }

    "startAmendment must redirect to JourneyRecovery when createAmendmentHandoff returns Left" in new Setup {
      val userAnswers = userAnswersWithSubmittedReturnsData

      mockCreateAmendmentHandoffReturns(Left("No monthly return found"))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.startAmendment(2024, 3).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl
      }
    }

    "startAmendment must redirect to JourneyRecovery when createAmendmentHandoff fails" in new Setup {
      val userAnswers = userAnswersWithSubmittedReturnsData

      mockCreateAmendmentHandoffFails()

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.startAmendment(2024, 3).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl
      }
    }

    "startAmendment must redirect when CisIdPage is missing" in new Setup {
      val app = application(emptyUserAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.startAmendment(2024, 3).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual unauthorisedUrl
      }
    }

    "viewSubmissionReceipt must return OK and render the success view when email is present" in new Setup {
      val userAnswers = userAnswersWithInstanceId

      mockReceiptReturns(Right(receiptViewModelWithEmail))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.viewSubmissionReceipt(2024, 6, "N").url)
        val result  = route(app, request).value
        val view    = app.injector.instanceOf[SubmissionSuccessView]

        status(result) mustEqual OK
        contentAsString(result) mustBe view(receiptViewModelWithEmail)(request, messages(app)).toString

        verify(mockSubmittedReturnsService)
          .getMonthlyReturnComplete(any[String], any[Int], any[Int], any[String])(any[HeaderCarrier])
      }
    }

    "viewSubmissionReceipt must return OK and render the confirmation view when email is absent" in new Setup {
      val userAnswers = userAnswersWithInstanceId

      mockReceiptReturns(Right(receiptViewModelWithoutEmail))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.viewSubmissionReceipt(2024, 6, "N").url)
        val result  = route(app, request).value
        val view    = app.injector.instanceOf[SubmissionSuccessView]

        status(result) mustEqual OK
        contentAsString(result) mustBe view(receiptViewModelWithoutEmail)(request, messages(app)).toString

        verify(mockSubmittedReturnsService)
          .getMonthlyReturnComplete(any[String], any[Int], any[Int], any[String])(any[HeaderCarrier])
      }
    }

    "viewSubmissionReceipt must redirect to JourneyRecovery when service guard fails" in new Setup {
      val userAnswers = userAnswersWithInstanceId

      mockReceiptReturns(Left("Submission guard failed: IRMark mismatch"))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.viewSubmissionReceipt(2024, 6, "N").url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl
      }
    }

    "viewSubmissionReceipt must redirect when CisIdPage is missing" in new Setup {
      val app = application(emptyUserAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.viewSubmissionReceipt(2024, 6, "N").url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual unauthorisedUrl
      }
    }

    "viewSubmissionReceipt must redirect to JourneyRecovery when the service call fails" in new Setup {
      val userAnswers = userAnswersWithInstanceId

      mockReceiptFails()

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.viewSubmissionReceipt(2024, 6, "N").url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl
      }
    }

    "onInProgressRedirect must redirect to journey recovery when amend status is STARTED" in new Setup {
      val app = application(userAnswersWithCisId)

      mockManageServiceReturnsDataStarted()

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onInProgressRedirect(1L).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "#" // TODO

        verify(mockManageService).getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier])
      }
    }

    "onInProgressRedirect must redirect to journey recovery when amend status is VALIDATED" in new Setup {
      val app = application(userAnswersWithCisId)

      mockManageServiceReturnsDataValidated()

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onInProgressRedirect(1L).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "#" // TODO

        verify(mockManageService).getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier])
      }
    }

    "onInProgressRedirect must redirect to journey recovery when amend status is not STARTED or VALIDATED" in new Setup {
      val app = application(userAnswersWithCisId)

      mockManageServiceReturnsData()

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onInProgressRedirect(3000L).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl

        verify(mockManageService).getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier])
      }
    }

    "onInProgressRedirect must redirect to journey recovery when service failed" in new Setup {
      val app = application(userAnswersWithCisId)

      mockManageServiceFails()

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onInProgressRedirect(3000L).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl

        verify(mockManageService).getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier])
      }
    }

    "onInProgressRedirect must redirect to journey recovery when CisIdPage is missing" in new Setup {
      val app = application(emptyUserAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onInProgressRedirect(3000L).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl
      }
    }
  }
}
