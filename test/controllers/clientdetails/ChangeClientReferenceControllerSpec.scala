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

package controllers.clientdetails

import base.SpecBase
import controllers.actions.{ClientListStatusGuard, HasClientGuard}
import controllers.routes
import forms.clientdetails.ChangeClientReferenceFormProvider
import models.{NormalMode, UserAnswers}
import models.requests.{DataRequest, IdentifierRequest}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.clientdetails.ChangeClientReferencePage
import play.api.inject.bind
import play.api.mvc.{ActionFilter, Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.clientdetails.ChangeClientReferenceView

import scala.concurrent.{ExecutionContext, Future}

class ChangeClientReferenceControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ChangeClientReferenceFormProvider()
  val form         = formProvider()

  lazy val changeClientReferenceRoute: String =
    controllers.clientdetails.routes.ChangeClientReferenceController.onPageLoad(NormalMode).url

  private val mockClientListStatusGuard = mock[ClientListStatusGuard]
  private val mockHasClientGuard        = mock[HasClientGuard]

  private val passThroughClientListStatusGuard =
    new ActionFilter[IdentifierRequest] {
      override protected def executionContext: ExecutionContext                               = ExecutionContext.global
      override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = Future.successful(None)
    }

  private val passThroughHasClientGuard =
    new ActionFilter[DataRequest] {
      override protected def executionContext: ExecutionContext                         = ExecutionContext.global
      override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] = Future.successful(None)
    }

  when(mockClientListStatusGuard.groupB(any[Call])).thenReturn(passThroughClientListStatusGuard)
  when(mockHasClientGuard.currentClient).thenReturn(passThroughHasClientGuard)

  private val guardBindings = Seq(
    bind[ClientListStatusGuard].toInstance(mockClientListStatusGuard),
    bind[HasClientGuard].toInstance(mockHasClientGuard)
  )

  "ChangeClientReference Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = guardBindings
      ).build()

      running(application) {
        val request = FakeRequest(GET, changeClientReferenceRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChangeClientReferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ChangeClientReferencePage, "answer").success.value

      val application = applicationBuilder(
        userAnswers = Some(userAnswers),
        additionalBindings = guardBindings
      ).build()

      running(application) {
        val request = FakeRequest(GET, changeClientReferenceRoute)

        val view = application.injector.instanceOf[ChangeClientReferenceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          additionalBindings = guardBindings ++ Seq(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
        ).build()

      running(application) {
        val request =
          FakeRequest(POST, changeClientReferenceRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = guardBindings
      ).build()

      running(application) {
        val request =
          FakeRequest(POST, changeClientReferenceRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ChangeClientReferenceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(
        userAnswers = None,
        additionalBindings = guardBindings
      ).build()

      running(application) {
        val request = FakeRequest(GET, changeClientReferenceRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(
        userAnswers = None,
        additionalBindings = guardBindings
      ).build()

      running(application) {
        val request =
          FakeRequest(POST, changeClientReferenceRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
