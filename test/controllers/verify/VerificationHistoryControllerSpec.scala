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

package controllers.verify

import base.SpecBase
import models.UserAnswers
import models.verify.{VerificationHistoryData, VerificationRequestData, VerificationTaxYearSelection}
import models.verify.VerificationTaxYearSelection.TaxYear
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify as mockVerify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.CisIdPage
import pages.verify.{VerificationHistoryDataPage, VerificationHistorySelectTaxYearPage}
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{ManageService, VerificationHistoryService}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.*
import views.html.verify.VerificationHistoryView

import java.time.LocalDate
import scala.concurrent.Future

class VerificationHistoryControllerSpec extends SpecBase with MockitoSugar {

  private val cisId = "900063"

  private val viewModel = VerificationHistoryPageViewModel(
    taxYears = Seq(
      VerificationTaxYearViewModel(
        fromYear = 2026,
        toYear = 2027,
        rows = Seq(
          VerificationHistoryRowViewModel(
            verificationNumber = "V0004528765",
            dateSubmitted = "6 Apr 2026",
            verificationRequestLink = "#",
            submissionReceiptLink = "#"
          )
        )
      )
    ),
    selectedTaxYear = Some("2026"),
    instanceId = cisId
  )

  private val verificationHistoryData = VerificationHistoryData(
    verificationRequests = Seq(
      VerificationRequestData("V0004528765", LocalDate.of(2026, 4, 6), 2026)
    )
  )

  trait Setup {

    val mockVerificationHistoryService: VerificationHistoryService = mock[VerificationHistoryService]
    val mockManageService: ManageService                           = mock[ManageService]

    def application(userAnswers: UserAnswers): Application =
      applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[VerificationHistoryService].toInstance(mockVerificationHistoryService),
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

    def userAnswersWithCisId: UserAnswers =
      emptyUserAnswers
        .set(CisIdPage, cisId)
        .success
        .value

    def userAnswersWithCisIdAndTaxYearSelection: UserAnswers =
      userAnswersWithCisId
        .set(VerificationHistorySelectTaxYearPage, TaxYear("2026 to 2027 (current tax year)"))
        .success
        .value

    def userAnswersWithVerificationHistoryData: UserAnswers =
      userAnswersWithCisIdAndTaxYearSelection
        .set(VerificationHistoryDataPage, verificationHistoryData)
        .success
        .value

    def mockManageServiceReturnsData(): Unit =
      when(mockManageService.getVerificationHistory(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(verificationHistoryData))

    def mockManageServiceFails(): Unit =
      when(mockManageService.getVerificationHistory(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

    def mockSingleYearViewModelReturns(model: Option[VerificationHistoryPageViewModel]): Unit =
      when(
        mockVerificationHistoryService.buildSingleYearViewModel(
          any[VerificationHistoryData],
          any[String],
          any[String]
        )
      ).thenReturn(model)

    def mockAllYearsViewModelReturns(model: Option[VerificationHistoryPageViewModel]): Unit =
      when(
        mockVerificationHistoryService.buildAllYearsViewModel(
          any[VerificationHistoryData],
          any[String]
        )
      ).thenReturn(model)

    def unauthorisedUrl: String =
      controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad().url

    def journeyRecoveryUrl: String =
      controllers.routes.JourneyRecoveryController.onPageLoad().url
  }

  "VerificationHistoryController" - {

    "onPageLoadSingleYear must return OK using VerificationHistoryDataPage when present" in new Setup {
      val userAnswers = userAnswersWithVerificationHistoryData

      when(mockVerificationHistoryService.buildSingleYearViewModel(verificationHistoryData, "2026", cisId))
        .thenReturn(Some(viewModel))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoadSingleYear().url)
        val result  = route(app, request).value
        val view    = app.injector.instanceOf[VerificationHistoryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(app)).toString

        mockVerify(mockVerificationHistoryService).buildSingleYearViewModel(verificationHistoryData, "2026", cisId)
        verifyNoInteractions(mockManageService)
      }
    }

    "onPageLoadAllYears must return OK using VerificationHistoryDataPage when present" in new Setup {
      val userAnswers = userAnswersWithVerificationHistoryData

      when(mockVerificationHistoryService.buildAllYearsViewModel(verificationHistoryData, cisId))
        .thenReturn(Some(viewModel))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoadAllYears().url)
        val result  = route(app, request).value
        val view    = app.injector.instanceOf[VerificationHistoryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(app)).toString

        mockVerify(mockVerificationHistoryService).buildAllYearsViewModel(verificationHistoryData, cisId)
        verifyNoInteractions(mockManageService)
      }
    }

    "onPageLoadSingleYear must fetch data from manageService when VerificationHistoryDataPage is missing and CisIdPage exists" in new Setup {
      val userAnswers = userAnswersWithCisIdAndTaxYearSelection

      mockManageServiceReturnsData()
      mockSingleYearViewModelReturns(Some(viewModel))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoadSingleYear().url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        mockVerify(mockManageService).getVerificationHistory(any[String])(any[HeaderCarrier])
        mockVerify(mockVerificationHistoryService)
          .buildSingleYearViewModel(any[VerificationHistoryData], any[String], any[String])
      }
    }

    "onPageLoadAllYears must fetch data from manageService when VerificationHistoryDataPage is missing and CisIdPage exists" in new Setup {
      val userAnswers = userAnswersWithCisId

      mockManageServiceReturnsData()
      mockAllYearsViewModelReturns(Some(viewModel))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoadAllYears().url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        mockVerify(mockManageService).getVerificationHistory(any[String])(any[HeaderCarrier])
        mockVerify(mockVerificationHistoryService).buildAllYearsViewModel(any[VerificationHistoryData], any[String])
      }
    }

    "onPageLoadSingleYear must redirect when CisIdPage is missing" in new Setup {
      val app = application(emptyUserAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoadSingleYear().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual unauthorisedUrl
      }
    }

    "onPageLoadAllYears must redirect when CisIdPage is missing" in new Setup {
      val app = application(emptyUserAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoadAllYears().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual unauthorisedUrl
      }
    }

    "onPageLoadSingleYear must redirect to JourneyRecovery when buildSingleYearViewModel returns None" in new Setup {
      val userAnswers = userAnswersWithVerificationHistoryData

      when(mockVerificationHistoryService.buildSingleYearViewModel(verificationHistoryData, "2026", cisId))
        .thenReturn(None)

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoadSingleYear().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl

        mockVerify(mockVerificationHistoryService).buildSingleYearViewModel(verificationHistoryData, "2026", cisId)
      }
    }

    "onPageLoadAllYears must redirect to JourneyRecovery when buildAllYearsViewModel returns None" in new Setup {
      val userAnswers = userAnswersWithVerificationHistoryData

      when(mockVerificationHistoryService.buildAllYearsViewModel(verificationHistoryData, cisId))
        .thenReturn(None)

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoadAllYears().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl

        mockVerify(mockVerificationHistoryService).buildAllYearsViewModel(verificationHistoryData, cisId)
      }
    }

    "onPageLoadSingleYear must redirect to JourneyRecovery when tax year selection is missing from session" in new Setup {
      val userAnswers = userAnswersWithCisId

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoadSingleYear().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl

        verifyNoInteractions(mockManageService)
        verifyNoInteractions(mockVerificationHistoryService)
      }
    }

    "onPageLoadSingleYear must redirect to JourneyRecovery when resolveVerificationHistoryData fails" in new Setup {
      val userAnswers = userAnswersWithCisIdAndTaxYearSelection

      mockManageServiceFails()

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoadSingleYear().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl

        mockVerify(mockManageService).getVerificationHistory(any[String])(any[HeaderCarrier])
      }
    }

    "onPageLoadAllYears must redirect to JourneyRecovery when resolveVerificationHistoryData fails" in new Setup {
      val userAnswers = userAnswersWithCisId

      mockManageServiceFails()

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoadAllYears().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl

        mockVerify(mockManageService).getVerificationHistory(any[String])(any[HeaderCarrier])
      }
    }
  }
}
