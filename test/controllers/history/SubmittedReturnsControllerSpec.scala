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
import models.history.{SubmittedMonthlyReturnData, SubmittedReturnsData, SubmittedSchemeData, SubmittedSubmissionData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
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

import java.time.Instant
import scala.concurrent.Future

class SubmittedReturnsControllerSpec extends SpecBase with MockitoSugar {

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

  trait Setup {
    val mockSubmittedReturnsService: SubmittedReturnsService = mock[SubmittedReturnsService]
    val mockManageService: ManageService                     = mock[ManageService]

    def application(userAnswers: UserAnswers): Application =
      applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SubmittedReturnsService].toInstance(mockSubmittedReturnsService),
          bind[ManageService].toInstance(mockManageService)
        )
        .build()
  }

  "SubmittedReturnsController" - {

    "onPageLoadSingleYear must return OK using SubmittedReturnsDataPage when present" in new Setup {
      val userAnswers =
        emptyUserAnswers.set(SubmittedReturnsDataPage, submittedReturnsData).success.value

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
      }
    }

    "onPageLoadAllYears must return OK using SubmittedReturnsDataPage when present" in new Setup {
      val userAnswers =
        emptyUserAnswers.set(SubmittedReturnsDataPage, submittedReturnsData).success.value

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
      }
    }

    "onPageLoadSingleYear must fetch data from manageService when SubmittedReturnsDataPage is missing and CisIdPage exists" in new Setup {
      val userAnswers =
        emptyUserAnswers.set(CisIdPage, "900063").success.value

      when(mockManageService.getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(submittedReturnsData))
      when(mockSubmittedReturnsService.buildSingleYearViewModel(any[SubmittedReturnsData], any[String]))
        .thenReturn(Some(viewModel))

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
      val userAnswers =
        emptyUserAnswers.set(CisIdPage, "900063").success.value

      when(mockManageService.getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(submittedReturnsData))
      when(mockSubmittedReturnsService.buildAllYearsViewModel(any[SubmittedReturnsData]))
        .thenReturn(Some(viewModel))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadAllYears().url)
        val result  = route(app, request).value

        status(result) mustEqual OK
        verify(mockManageService).getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier])
        verify(mockSubmittedReturnsService).buildAllYearsViewModel(any[SubmittedReturnsData])
      }
    }

    "onPageLoadSingleYear must redirect to JourneyRecovery when no data can be resolved" in new Setup {
      val app = application(emptyUserAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadSingleYear("2024").url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "onPageLoadAllYears must redirect to JourneyRecovery when no data can be resolved" in new Setup {
      val app = application(emptyUserAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadAllYears().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "onPageLoadSingleYear must redirect to JourneyRecovery when buildSingleYearViewModel returns None" in new Setup {
      val userAnswers =
        emptyUserAnswers.set(SubmittedReturnsDataPage, submittedReturnsData).success.value

      when(mockSubmittedReturnsService.buildSingleYearViewModel(submittedReturnsData, "2024"))
        .thenReturn(None)

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadSingleYear("2024").url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        verify(mockSubmittedReturnsService).buildSingleYearViewModel(submittedReturnsData, "2024")
      }
    }

    "onPageLoadSingleYear must redirect to JourneyRecovery when resolveSubmittedReturnsData fails" in new Setup {
      val userAnswers =
        emptyUserAnswers.set(CisIdPage, "900063").success.value

      when(mockManageService.getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadSingleYear("2024").url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        verify(mockManageService).getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier])
      }
    }

    "onPageLoadAllYears must redirect to JourneyRecovery when buildAllYearsViewModel returns None" in new Setup {
      val userAnswers =
        emptyUserAnswers.set(SubmittedReturnsDataPage, submittedReturnsData).success.value

      when(mockSubmittedReturnsService.buildAllYearsViewModel(submittedReturnsData))
        .thenReturn(None)

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadAllYears().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        verify(mockSubmittedReturnsService).buildAllYearsViewModel(submittedReturnsData)
      }
    }

    "onPageLoadAllYears must redirect to JourneyRecovery when resolveSubmittedReturnsData fails" in new Setup {
      val userAnswers =
        emptyUserAnswers.set(CisIdPage, "900063").success.value

      when(mockManageService.getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadAllYears().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        verify(mockManageService).getSubmittedMonthlyReturns(any[String])(any[HeaderCarrier])
      }
    }
  }
}
