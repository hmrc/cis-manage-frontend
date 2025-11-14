/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.agent

import base.SpecBase
import controllers.routes
import forms.ClientListSearchFormProvider
import models.agent.ClientListFormData
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.agent.ClientListSearchView
import pages.ClientListSearchPage
import viewmodels.agent.{ClientListViewModel, SearchByList}

import scala.concurrent.Future

class ClientListSearchControllerSpec extends SpecBase with MockitoSugar {

  val formProvider                   = new ClientListSearchFormProvider()
  val form: Form[ClientListFormData] = formProvider()

  lazy val clientListSearchRoute: String = controllers.agent.routes.ClientListSearchController.onPageLoad().url

  "ClientListSearch Controller" - {

    val filteredClients = ClientListViewModel.filterByField("", "")

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, clientListSearchRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClientListSearchView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, SearchByList.searchByOptions, filteredClients)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val filteredClients = ClientListViewModel.filterByField("CN", "ABC")

      val userAnswers =
        UserAnswers(userAnswersId).set(ClientListSearchPage, ClientListFormData("CN", "ABC")).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, clientListSearchRoute)

        val view = application.injector.instanceOf[ClientListSearchView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(ClientListFormData("CN", "ABC")), SearchByList.searchByOptions, filteredClients)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to the client list search page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, clientListSearchRoute)
            .withFormUrlEncodedBody(("searchBy", "CN"), ("searchFilter", "ABC"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.agent.routes.ClientListSearchController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, clientListSearchRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ClientListSearchView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, SearchByList.searchByOptions, filteredClients)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, clientListSearchRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, clientListSearchRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    ".clearFilter" - {
      "must remove form data from user answers and display the client list search page" in {

        val filteredClients = ClientListViewModel.allAgentClients

        lazy val clearFilterRoute: String = controllers.agent.routes.ClientListSearchController.clearFilter().url

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val view = application.injector.instanceOf[ClientListSearchView]

          val request =
            FakeRequest(GET, clearFilterRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, SearchByList.searchByOptions, filteredClients)(request, messages(application)).toString
        }
      }
    }

  }
}
