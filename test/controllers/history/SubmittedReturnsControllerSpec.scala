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
import models.history.{SubmittedMonthlyReturnData, SubmittedReturnsData, SubmittedSchemeData, SubmittedSubmissionData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.history.SubmittedReturnsDataPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SubmittedReturnsService
import viewmodels.*
import views.html.history.SubmittedReturnsView

import java.time.Instant

class SubmittedReturnsControllerSpec extends SpecBase with MockitoSugar {

  private val mockService = mock[SubmittedReturnsService]

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
    monthlyReturn = Seq(
      SubmittedMonthlyReturnData(
        monthlyReturnId = 1L,
        taxYear = 2024,
        taxMonth = 3,
        nilReturnIndicator = "Y",
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
        activeObjectId = 1L,
        status = "Submitted",
        hmrcMarkGenerated = Some("123"),
        hmrcMarkGgis = Some("123"),
        emailRecipient = Some("test@example.com"),
        acceptedTime = Some(Instant.parse("2024-04-01T12:00:00Z"))
      )
    )
  )

  "SubmittedReturnsController" - {

    "onPageLoadSingleYear must return OK and the correct view when the service returns a view model" in {
      when(mockService.buildSingleYearViewModel(any(), any[String]))
        .thenReturn(Some(viewModel))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmittedReturnsService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadSingleYear("2024").url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SubmittedReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "onPageLoadSingleYear must redirect to JourneyRecovery when the service returns None" in {
      when(mockService.buildSingleYearViewModel(any(), any[String]))
        .thenReturn(None)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmittedReturnsService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadSingleYear("2024").url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "onPageLoadAllYears must return OK and the correct view when the service returns a view model" in {
      when(mockService.buildAllYearsViewModel(any()))
        .thenReturn(Some(viewModel))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmittedReturnsService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadAllYears().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SubmittedReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "onPageLoadAllYears must redirect to JourneyRecovery when the service returns None" in {
      when(mockService.buildAllYearsViewModel(any()))
        .thenReturn(None)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmittedReturnsService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadAllYears().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "onPageLoadSingleYear must use existing SubmittedReturnsDataPage when already present" in {
      val userAnswersWithSubmittedReturnsData =
        emptyUserAnswers.set(SubmittedReturnsDataPage, submittedReturnsData).success.value

      when(mockService.buildSingleYearViewModel(any(), any[String]))
        .thenReturn(Some(viewModel))

      val application = applicationBuilder(userAnswers = Some(userAnswersWithSubmittedReturnsData))
        .overrides(bind[SubmittedReturnsService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadSingleYear("2024").url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        verify(mockService).buildSingleYearViewModel(userAnswersWithSubmittedReturnsData, "2024")
      }
    }

    "onPageLoadAllYears must use existing SubmittedReturnsDataPage when already present" in {
      val userAnswersWithSubmittedReturnsData =
        emptyUserAnswers.set(SubmittedReturnsDataPage, submittedReturnsData).success.value

      when(mockService.buildAllYearsViewModel(any()))
        .thenReturn(Some(viewModel))

      val application = applicationBuilder(userAnswers = Some(userAnswersWithSubmittedReturnsData))
        .overrides(bind[SubmittedReturnsService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoadAllYears().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        verify(mockService).buildAllYearsViewModel(userAnswersWithSubmittedReturnsData)
      }
    }
  }
}
