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
import controllers.routes
import forms.clientdetails.RemoveClientYesNoFormProvider
import models.agent.ClientListFormData
import models.{CisTaxpayerSearchResult, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ClientListSearchPage
import pages.clientdetails.RemoveClientYesNoPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.ManageService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.clientdetails.RemoveClientYesNoView

import scala.concurrent.Future

class RemoveClientYesNoControllerSpec extends SpecBase with MockitoSugar {
  val clientName  = "clientName"
  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RemoveClientYesNoFormProvider()
  val form         = formProvider()

  lazy val removeClientRoute = controllers.clientdetails.routes.RemoveClientYesNoController.onPageLoad(NormalMode).url

  "RemoveClient Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeClientRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveClientYesNoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(clientName, form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(RemoveClientYesNoPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeClientRoute)

        val view = application.injector.instanceOf[RemoveClientYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(clientName, form.fill(true), NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted and user answered YES" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockManageService     = mock[ManageService]

      val cisClients: List[CisTaxpayerSearchResult] = List(
        CisTaxpayerSearchResult(
          uniqueId = "UID-001",
          taxOfficeNumber = "123",
          taxOfficeRef = "AB45678",
          agentOwnRef = Some("ABC-001"),
          schemeName = Some("ABC Construction Ltd"),
          utr = Some("1234567890")
        ),
        CisTaxpayerSearchResult(
          uniqueId = "UID-002",
          taxOfficeNumber = "789",
          taxOfficeRef = "EF23456",
          agentOwnRef = Some("ABC-002"),
          schemeName = Some("ABC Property Services"),
          utr = Some("1234567890")
        )
      )

      val uaWithClients =
        emptyUserAnswers.set(ClientListSearchPage, ClientListFormData("CN", "ABC")).success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockManageService.removeClient(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))
      when(mockManageService.resolveAndStoreAgentClients(any[UserAnswers])(using any[HeaderCarrier]))
        .thenReturn(Future.successful((cisClients, uaWithClients)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageService].toInstance(mockManageService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeClientRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data is submitted and user answered NO" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeClientRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removeClientRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveClientYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(clientName, boundForm, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removeClientRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, removeClientRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if be failed" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockManageService     = mock[ManageService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockManageService.removeClient(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageService].toInstance(mockManageService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeClientRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
