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

package controllers.delete

import base.SpecBase
import forms.delete.DeleteNilMonthlyReturnFormProvider
import models.{Deletable, NormalMode, NotDeletable, UnsubmittedMonthlyReturnsRow, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.delete.DeleteNilMonthlyReturnPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.delete.UnsubmittedMonthlyReturnToDeleteQuery
import repositories.SessionRepository
import services.ManageService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.delete.DeleteNilMonthlyReturnView

import scala.concurrent.Future

class DeleteNilMonthlyReturnControllerSpec extends SpecBase with MockitoSugar {

  val formProvider        = new DeleteNilMonthlyReturnFormProvider()
  val form: Form[Boolean] = formProvider()

  private val monthYear: String = "April 2026"

  val deletableRow        =
    UnsubmittedMonthlyReturnsRow(2026, 4, "Nil", "In Progress", 3000L, Seq("Continue", "Delete"), None, Some("N"), true)
  val baseUa: UserAnswers = userAnswersWithCisId
    .set(UnsubmittedMonthlyReturnToDeleteQuery, deletableRow)
    .success
    .value

  lazy val deleteNilMonthlyReturnRoute: String =
    controllers.delete.routes.DeleteNilMonthlyReturnController.onPageLoad().url

  "DeleteNilMonthlyReturn Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseUa)).build()

      running(application) {
        val request = FakeRequest(GET, deleteNilMonthlyReturnRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeleteNilMonthlyReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, monthYear, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseUa.set(DeleteNilMonthlyReturnPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, deleteNilMonthlyReturnRoute)

        val view = application.injector.instanceOf[DeleteNilMonthlyReturnView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), monthYear, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the ReturnsLandingController after calling api when user answered yes" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockManageService = mock[ManageService]
      when(
        mockManageService.checkUnsubmittedMonthlyReturnDeletion(any[UserAnswers], any[Long])(any[HeaderCarrier])
      ).thenReturn(Future.successful(Deletable(deletableRow)))
      when(
        mockManageService
          .deleteUnsubmittedMonthlyReturn(any[UserAnswers], any[UnsubmittedMonthlyReturnsRow])(any())
      ).thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(baseUa))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageService].toInstance(mockManageService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteNilMonthlyReturnRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnsLandingController.onPageLoad("1").url
      }
    }

    "must redirect to the ReturnsLandingController when user answered no" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockManageService = mock[ManageService]

      val application =
        applicationBuilder(userAnswers = Some(baseUa))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageService].toInstance(mockManageService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteNilMonthlyReturnRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnsLandingController.onPageLoad("1").url

        verifyNoInteractions(mockManageService)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseUa)).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteNilMonthlyReturnRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeleteNilMonthlyReturnView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, monthYear, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, deleteNilMonthlyReturnRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteNilMonthlyReturnRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if CisId missing in user answers" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers.set(UnsubmittedMonthlyReturnToDeleteQuery, deletableRow).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteNilMonthlyReturnRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if record is not deletable" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockManageService     = mock[ManageService]

      when(mockManageService.checkUnsubmittedMonthlyReturnDeletion(any[UserAnswers], any[Long])(any()))
        .thenReturn(Future.successful(NotDeletable))

      val application =
        applicationBuilder(userAnswers = Some(baseUa))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageService].toInstance(mockManageService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteNilMonthlyReturnRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      verifyNoInteractions(mockSessionRepository)
      verify(mockManageService, never()).deleteUnsubmittedMonthlyReturn(any(), any())(any())
    }

    "must redirect to Journey Recovery for a GET if UnsubmittedReturnToDeleteQuery is missing" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCisId)).build()

      running(application) {
        val request = FakeRequest(GET, deleteNilMonthlyReturnRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
