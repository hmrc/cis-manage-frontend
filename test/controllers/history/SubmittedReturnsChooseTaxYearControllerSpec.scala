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
import controllers.routes
import forms.history.SubmittedReturnsChooseTaxYearFormProvider
import models.history.TaxYearSelection.TaxYear
import models.history.TaxYearSelection
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.history.SubmittedReturnsChooseTaxYearPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.ManageService
import views.html.history.SubmittedReturnsChooseTaxYearView

import scala.concurrent.Future

class SubmittedReturnsChooseTaxYearControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val submittedReturnsChooseTaxYearRoute: String =
    controllers.history.routes.SubmittedReturnsChooseTaxYearController.onPageLoad().url
  val taxYears: Seq[String]                           =
    Seq("2021 to 2022", "2022 to 2023", "2023 to 2024", "2024 to 2025")
  val taxYearTuples: Seq[(Int, Int)]                  =
    Seq((2021, 2022), (2022, 2023), (2023, 2024), (2024, 2025))
  val taxYearSelections: Seq[TaxYearSelection]        =
    Seq(TaxYear(2021, 2022), TaxYear(2022, 2023), TaxYear(2023, 2024), TaxYear(2024, 2025))

  val formProvider       = new SubmittedReturnsChooseTaxYearFormProvider()
  val form: Form[String] = formProvider(taxYears)

  val mockManageService = mock[ManageService]

  "SubmittedReturnsChooseTaxYear Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockManageService.getSubmittedTaxYears(any())(any())) thenReturn Future.successful(taxYearTuples)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCisId))
        .overrides(
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, submittedReturnsChooseTaxYearRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmittedReturnsChooseTaxYearView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYears)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockManageService.getSubmittedTaxYears(any())(any())) thenReturn Future.successful(taxYearTuples)

      val userAnswers = userAnswersWithCisId
        .set(SubmittedReturnsChooseTaxYearPage, taxYearSelections.head)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, submittedReturnsChooseTaxYearRoute)

        val view = application.injector.instanceOf[SubmittedReturnsChooseTaxYearView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(taxYears.head), taxYears)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to NoReturnsSubmittedController for a GET when no tax years are returned" in {

      when(mockManageService.getSubmittedTaxYears(any())(any())) thenReturn Future.successful(Seq.empty)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCisId))
        .overrides(
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, submittedReturnsChooseTaxYearRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.amend.routes.NoReturnsSubmittedController.onPageLoad().url
      }
    }

    "must redirect to the single tax year page for a GET when only one tax year is returned" in {

      val singleTaxYearTuple = Seq((2021, 2022))

      when(mockManageService.getSubmittedTaxYears(any())(any())) thenReturn Future.successful(singleTaxYearTuple)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCisId))
        .overrides(
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, submittedReturnsChooseTaxYearRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.history.routes.SubmittedReturnsController.onPageLoadSingleYear("2021").url
      }
    }

    "must redirect to System Error for a GET when the service fails" in {

      when(mockManageService.getSubmittedTaxYears(any())(any())) thenReturn Future.failed(
        new Exception("service failed")
      )

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCisId))
        .overrides(
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, submittedReturnsChooseTaxYearRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to the all tax years submitted returns page when 'all' is submitted" in {

      when(mockManageService.getSubmittedTaxYears(any())(any())) thenReturn Future.successful(taxYearTuples)

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageService].toInstance(mockManageService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submittedReturnsChooseTaxYearRoute)
            .withFormUrlEncodedBody(("value", "all"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.history.routes.SubmittedReturnsController.onPageLoadAllYears().url
      }
    }

    "must redirect to the selected single tax year page when valid data is submitted" in {

      when(mockManageService.getSubmittedTaxYears(any())(any())) thenReturn Future.successful(taxYearTuples)

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageService].toInstance(mockManageService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submittedReturnsChooseTaxYearRoute)
            .withFormUrlEncodedBody(("value", taxYears.head))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.history.routes.SubmittedReturnsController.onPageLoadSingleYear("2021").url
      }
    }

    "must return an internal server error when a different invalid tax year format is submitted that passes form validation" in {

      val weirdTaxYearTuples = Seq((2021, 22))
      when(mockManageService.getSubmittedTaxYears(any())(any())) thenReturn Future.successful(weirdTaxYearTuples)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[ManageService].toInstance(mockManageService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submittedReturnsChooseTaxYearRoute)
            .withFormUrlEncodedBody(("value", "2021 to 22"))

        val result = route(application, request).value

        intercept[Exception] {
          await(result)
        }.getMessage must include("unable to parse tax year selection")
      }
    }

    "must return an internal server error when an invalid tax year format is submitted that passes form validation" in {

      val weirdTaxYearTuples = Seq((1, 2))
      when(mockManageService.getSubmittedTaxYears(any())(any())) thenReturn Future.successful(weirdTaxYearTuples)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[ManageService].toInstance(mockManageService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submittedReturnsChooseTaxYearRoute)
            .withFormUrlEncodedBody(("value", "1 to 2"))

        val result = route(application, request).value

        intercept[Exception] {
          await(result)
        }.getMessage must include("unable to parse tax year selection")
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockManageService.getSubmittedTaxYears(any())(any())) thenReturn Future.successful(taxYearTuples)

      val userAnswers = userAnswersWithCisId
        .set(SubmittedReturnsChooseTaxYearPage, taxYearSelections.head)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, submittedReturnsChooseTaxYearRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SubmittedReturnsChooseTaxYearView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYears)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, submittedReturnsChooseTaxYearRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, submittedReturnsChooseTaxYearRoute)
            .withFormUrlEncodedBody(("value", taxYears.head))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
