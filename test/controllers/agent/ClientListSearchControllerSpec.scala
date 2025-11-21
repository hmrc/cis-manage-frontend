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
import models.{CisTaxpayerSearchResult, UserAnswers}
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
import play.api.Application
import services.ManageService
import viewmodels.agent.{ClientListViewModel, SearchByList}

import scala.concurrent.Future

class ClientListSearchControllerSpec extends SpecBase with MockitoSugar {

  val formProvider                   = new ClientListSearchFormProvider()
  val form: Form[ClientListFormData] = formProvider()

  lazy val clientListSearchRoute: String   = controllers.agent.routes.ClientListSearchController.onPageLoad().url
  lazy val clientListDownloadRoute: String =
    controllers.agent.routes.ClientListSearchController.downloadClientList().url
  val clients                              = List(
    CisTaxpayerSearchResult(
      uniqueId = "123",
      taxOfficeNumber = "123",
      taxOfficeRef = "AB456",
      agentOwnRef = Some("someRef"),
      schemeName = Some("Scheme 123")
    )
  )

  def buildApp(userAnswers: UserAnswers = emptyUserAnswers): (ManageService, SessionRepository, Application) = buildApp(
    Some(userAnswers)
  )
  def buildApp(userAnswers: Option[UserAnswers]): (ManageService, SessionRepository, Application)            = {
    val manageService     = mock[ManageService]
    val sessionRepository = mock[SessionRepository]
    val app               = applicationBuilder(userAnswers = userAnswers)
      .overrides(
        bind[ManageService].toInstance(manageService),
        bind[SessionRepository].toInstance(sessionRepository)
      )
      .build()

    (manageService, sessionRepository, app)
  }

  "ClientListSearch Controller" - {

    val filteredClients = ClientListViewModel.filterByField("", "")

    "must return OK and the correct view for a GET" in {

      val (manageService, mockSessionRepository, application) = buildApp(emptyUserAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(manageService.resolveAndStoreAgentClients(any[UserAnswers])(using any))
        .thenReturn(Future.successful((clients, emptyUserAnswers)))

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

      val (manageService, mockSessionRepository, application) = buildApp(userAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(manageService.resolveAndStoreAgentClients(any[UserAnswers])(using any))
        .thenReturn(Future.successful((clients, userAnswers)))

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

      val (manageService, mockSessionRepository, application) = buildApp(emptyUserAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(manageService.resolveAndStoreAgentClients(any[UserAnswers])(using any))
        .thenReturn(Future.successful((clients, emptyUserAnswers)))

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

      val (manageService, _, application) = buildApp(emptyUserAnswers)

      when(manageService.resolveAndStoreAgentClients(any[UserAnswers])(using any))
        .thenReturn(Future.successful((clients, emptyUserAnswers)))

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

      val (_, _, application) = buildApp(None)

      running(application) {
        val request = FakeRequest(GET, clientListSearchRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val (_, _, application) = buildApp(None)

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

        val (_, mockSessionRepository, application) = buildApp(emptyUserAnswers)

        val filteredClients = ClientListViewModel.allAgentClients

        lazy val clearFilterRoute: String = controllers.agent.routes.ClientListSearchController.clearFilter().url

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

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

    ".downloadClientList" - {

      "must return a CSV file with all mock clients" in {

        val (_, _, application) = buildApp(emptyUserAnswers)

        running(application) {
          val request = FakeRequest(GET, clientListDownloadRoute)

          val result = route(application, request).value

          status(result) mustEqual OK

          contentType(result) mustBe Some("text/csv")
          header("Content-Disposition", result) mustBe Some("attachment; filename=CISAgentClientList.csv")

          val body = contentAsString(result)

          val expectedLines = Seq(
            "Client name,Employers reference,Client reference",
            "\"ABC Construction Ltd\",\"123/AB45678\",\"ABC-001\"",
            "\"ABC Property Services\",\"789/EF23456\",\"ABC-002\"",
            "\"Capital Construction Group\",\"345/IJ67890\",\"CAP-001\""
          )

          body mustEqual expectedLines.mkString("\n")
        }
      }
    }

  }
}
