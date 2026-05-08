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
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.VerificationHistorySelectTaxYearPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear}
import play.api.test.Helpers.*
import repositories.SessionRepository
import models.verify.VerificationTaxYearSelection
import views.html.verify.VerificationHistorySelectTaxYearView

import scala.concurrent.Future

class VerificationHistorySelectTaxYearControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val verificationHistorySelectTaxYearRoute =
    controllers.verify.routes.VerificationHistorySelectTaxYearController.onPageLoad(NormalMode).url

  val taxYears: Seq[String] =
    Seq(
      "2026 to 2027 (current tax year)",
      "2025 to 2026",
      "2024 to 2025",
      "2023 to 2024"
    )

  val formProvider = new VerificationHistorySelectTaxYearFormProvider()
  val form         = formProvider(taxYears)

  val mode = NormalMode

  "VerificationHistorySelectTaxYear Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId)).build()

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

      val userAnswers = userAnswersWithCisId
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

      val selectedYear = "2026 to 2027 (current tax year)"

      val userAnswers = userAnswersWithCisId
        .set(
          VerificationHistorySelectTaxYearPage,
          TaxYear(selectedYear)
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
          view(form.fill(selectedYear), mode, taxYears)(request, messages(application)).toString
      }
    }

    "must redirect to onPageLoad when no user answers exist and form submitted" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, verificationHistorySelectTaxYearRoute)
            .withFormUrlEncodedBody("value" -> "all")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual verificationHistorySelectTaxYearRoute
      }
    }

    "must treat 'all' as AllTaxYears and redirect to next page" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, verificationHistorySelectTaxYearRoute)
            .withFormUrlEncodedBody("value" -> "all")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, verificationHistorySelectTaxYearRoute)
            .withFormUrlEncodedBody(
              "value" -> "2026 to 2027 (current tax year)"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return BAD_REQUEST when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId)).build()

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

    "must return OK and the correct view for a GET when no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None).build()

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
