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
import forms.verify.VerificationHistorySelectTaxYearFormProvider
import models.NormalMode
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.{VerificationHistoryDataPage, VerificationHistorySelectTaxYearPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import models.verify.{VerificationHistoryData, VerificationRequestData}
import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear, TaxYearPeriod}
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.verify.VerificationHistorySelectTaxYearView

import java.time.LocalDate
import scala.concurrent.Future

class VerificationHistorySelectTaxYearControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private def verificationRequestData(
    verificationNumber: String,
    dateSubmitted: LocalDate,
    taxYear: Int
  ): VerificationRequestData =
    VerificationRequestData(
      verificationBatchId = 1L,
      verificationNumber = verificationNumber,
      dateSubmitted = dateSubmitted,
      taxYear = taxYear,
      acceptedDateTime = dateSubmitted.atStartOfDay(),
      contractorName = "Test Scheme",
      employerReference = "123PA000001",
      receiptReferenceNumber = "Pyy1LRJh053AE+nuyp0GJR7oESw=",
      subcontractorsToVerify = Seq.empty
    )

  lazy val verificationHistorySelectTaxYearRoute =
    controllers.verify.routes.VerificationHistorySelectTaxYearController.onPageLoad().url

  val taxYears: Seq[TaxYearPeriod] =
    Seq(
      TaxYearPeriod(2026),
      TaxYearPeriod(2025),
      TaxYearPeriod(2024)
    )

  val formProvider = new VerificationHistorySelectTaxYearFormProvider()
  val form         = formProvider(taxYears.map(_.startYear.toString))

  val mode = NormalMode

  val verificationHistoryData: VerificationHistoryData =
    VerificationHistoryData(
      verificationRequests = Seq(
        verificationRequestData("V001", LocalDate.of(2026, 4, 6), 2026),
        verificationRequestData("V002", LocalDate.of(2025, 4, 6), 2025),
        verificationRequestData("V003", LocalDate.of(2025, 4, 5), 2024)
      )
    )

  val userAnswersWithVerificationHistoryData =
    userAnswersWithCisId
      .set(VerificationHistoryDataPage, verificationHistoryData)
      .success
      .value

  "VerificationHistorySelectTaxYear Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithVerificationHistoryData)).build()

      running(application) {
        val request = FakeRequest(GET, verificationHistorySelectTaxYearRoute)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[VerificationHistorySelectTaxYearView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, mode, taxYears)(request, messages(application)).toString
      }
    }

    "must populate the view correctly when previously answered with AllTaxYears" in {

      val userAnswers = userAnswersWithVerificationHistoryData
        .set(
          VerificationHistorySelectTaxYearPage,
          AllTaxYears
        )
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, verificationHistorySelectTaxYearRoute)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[VerificationHistorySelectTaxYearView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill("all"), mode, taxYears)(request, messages(application)).toString
      }
    }

    "must populate the view correctly when previously answered with a tax year" in {

      val userAnswers = userAnswersWithVerificationHistoryData
        .set(
          VerificationHistorySelectTaxYearPage,
          TaxYear(2026)
        )
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, verificationHistorySelectTaxYearRoute)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[VerificationHistorySelectTaxYearView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill("2026"), mode, taxYears)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery when no user answers exist and form submitted" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, verificationHistorySelectTaxYearRoute)
            .withFormUrlEncodedBody("value" -> "all")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must treat 'all' as AllTaxYears and redirect to all tax years verification history" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithVerificationHistoryData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, verificationHistorySelectTaxYearRoute)
            .withFormUrlEncodedBody("value" -> "all")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.verify.routes.VerificationHistoryController
            .onPageLoadAllYears()
            .url
      }
    }

    "must redirect to single year verification history when a tax year is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithVerificationHistoryData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, verificationHistorySelectTaxYearRoute)
            .withFormUrlEncodedBody(
              "value" -> "2026"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.verify.routes.VerificationHistoryController
            .onPageLoadSingleYear()
            .url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())
        captor.getValue.get(VerificationHistorySelectTaxYearPage).value mustEqual TaxYear(2026)
      }
    }

    "must return BAD_REQUEST when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithVerificationHistoryData)).build()

      running(application) {
        val request =
          FakeRequest(POST, verificationHistorySelectTaxYearRoute)
            .withFormUrlEncodedBody("value" -> "invalid")

        val boundForm = form.bind(Map("value" -> "invalid"))

        val view =
          application.injector.instanceOf[VerificationHistorySelectTaxYearView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, mode, taxYears)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET when no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, verificationHistorySelectTaxYearRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, verificationHistorySelectTaxYearRoute)
            .withFormUrlEncodedBody("value" -> "all")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
  }
}
