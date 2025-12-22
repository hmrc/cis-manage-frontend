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
import services.{ManageService, PaginationService}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.agent.{ClientListViewModel, SearchByList}
import viewmodels.govuk.PaginationFluency._

import scala.concurrent.Future

class ClientListSearchControllerSpec extends SpecBase with MockitoSugar {
  implicit val hc: HeaderCarrier     = HeaderCarrier()
  val formProvider                   = new ClientListSearchFormProvider()
  val form: Form[ClientListFormData] = formProvider()

  private val onPageLoadRoute  = controllers.agent.routes.ClientListSearchController.onPageLoad().url
  private val clearFilterRoute = controllers.agent.routes.ClientListSearchController.clearFilter().url
  private val downloadRoute    = controllers.agent.routes.ClientListSearchController.downloadClientList().url

  private val cisClients: List[CisTaxpayerSearchResult] = List(
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

  private def allVm: Seq[ClientListViewModel] =
    ClientListViewModel.fromCisClients(cisClients)

  private def appWith(
    ua: Option[UserAnswers] = Some(emptyUserAnswers),
    returnedUa: UserAnswers = emptyUserAnswers
  ) = {
    val manageService = mock[ManageService]
    val sessionRepo   = mock[SessionRepository]

    when(sessionRepo.set(any())) thenReturn Future.successful(true)
    when(manageService.resolveAndStoreAgentClients(any[UserAnswers])(using any[HeaderCarrier]))
      .thenReturn(Future.successful((cisClients, returnedUa)))

    applicationBuilder(userAnswers = ua, isAgent = true)
      .overrides(
        bind[ManageService].toInstance(manageService),
        bind[SessionRepository].toInstance(sessionRepo)
      )
      .build()
  }

  "ClientListSearch Controller" - {

    "must return OK and the correct view for a GET" in {
      val app = appWith()
      running(app) {
        val req               = FakeRequest(GET, "/agent/file-monthly-cis-returns")
        val result            = route(app, req).value
        val view              = app.injector.instanceOf[ClientListSearchView]
        val paginationService = app.injector.instanceOf[PaginationService]

        val filtered         = ClientListViewModel.filterByField("", "", allVm)
        val paginationResult = paginationService.paginateClientList(
          filtered,
          1,
          onPageLoadRoute
        )

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(
            form,
            SearchByList.searchByOptions,
            paginationResult.paginatedData,
            paginationResult.paginationViewModel
          )(req, messages(app)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val uaWithSearch =
        emptyUserAnswers.set(ClientListSearchPage, ClientListFormData("CN", "ABC")).success.value

      val app = appWith(returnedUa = uaWithSearch)
      running(app) {
        val req               = FakeRequest(GET, onPageLoadRoute)
        val result            = route(app, req).value
        val view              = app.injector.instanceOf[ClientListSearchView]
        val paginationService = app.injector.instanceOf[PaginationService]

        val prepared         = form.fill(ClientListFormData("CN", "ABC"))
        val filtered         = ClientListViewModel.filterByField("CN", "ABC", allVm)
        val paginationResult = paginationService.paginateClientList(
          filtered,
          1,
          onPageLoadRoute
        )

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(
            prepared,
            SearchByList.searchByOptions,
            paginationResult.paginatedData,
            paginationResult.paginationViewModel
          )(req, messages(app)).toString
      }
    }

    "must redirect to the client list search page when valid data is submitted" in {
      val app = appWith()
      running(app) {
        val req =
          FakeRequest(POST, onPageLoadRoute)
            .withFormUrlEncodedBody("searchBy" -> "CN", "searchFilter" -> "ABC")

        val result = route(app, req).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe onPageLoadRoute
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val app = appWith()
      running(app) {
        val req =
          FakeRequest(POST, onPageLoadRoute)
            .withFormUrlEncodedBody("value" -> "")

        val result            = route(app, req).value
        val view              = app.injector.instanceOf[ClientListSearchView]
        val paginationService = app.injector.instanceOf[PaginationService]
        val boundForm         = form.bind(Map("value" -> ""))

        val filtered         = ClientListViewModel.filterByField("", "", allVm)
        val paginationResult = paginationService.paginateClientList(
          filtered,
          1,
          onPageLoadRoute
        )

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe
          view(
            boundForm,
            SearchByList.searchByOptions,
            paginationResult.paginatedData,
            paginationResult.paginationViewModel
          )(req, messages(app)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoadRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, onPageLoadRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    ".clearFilter" - {
      "must remove form data from user answers and display the client list search page" in {
        val app = appWith()
        running(app) {
          val req               = FakeRequest(GET, clearFilterRoute)
          val result            = route(app, req).value
          val view              = app.injector.instanceOf[ClientListSearchView]
          val paginationService = app.injector.instanceOf[PaginationService]

          val paginationResult = paginationService.paginateClientList(
            allVm,
            1,
            onPageLoadRoute
          )

          status(result) mustBe OK
          contentAsString(result) mustBe
            view(
              form,
              SearchByList.searchByOptions,
              paginationResult.paginatedData,
              paginationResult.paginationViewModel
            )(req, messages(app)).toString
        }
      }
    }

    "pagination" - {
      "must handle page query parameter correctly" in {
        val app = appWith()
        running(app) {
          val req    = FakeRequest(GET, s"$onPageLoadRoute?page=1")
          val result = route(app, req).value

          status(result) mustBe OK
        }
      }

      "must handle invalid page parameter by defaulting to page 1" in {
        val app = appWith()
        running(app) {
          val req    = FakeRequest(GET, s"$onPageLoadRoute?page=invalid")
          val result = route(app, req).value

          status(result) mustBe OK
        }
      }

      "must handle negative page parameter by defaulting to page 1" in {
        val app = appWith()
        running(app) {
          val req    = FakeRequest(GET, s"$onPageLoadRoute?page=-1")
          val result = route(app, req).value

          status(result) mustBe OK
        }
      }
    }

    ".downloadClientList" - {

      "must return a CSV file with all mock clients" in {
        val app = appWith()
        running(app) {
          val req    = FakeRequest(GET, downloadRoute)
          val result = route(app, req).value

          status(result) mustBe OK
          contentType(result) mustBe Some("text/csv")
          header("Content-Disposition", result).value mustBe "attachment; filename=CISAgentClientList.csv"

          val msgs           = messages(app)
          val expectedHeader = Seq(
            msgs("agent.clientListSearch.th.clientName"),
            msgs("agent.clientListSearch.th.employerReference"),
            msgs("agent.clientListSearch.th.clientReference")
          ).mkString(",")

          val expectedBody = Seq(
            expectedHeader,
            "\"ABC Construction Ltd\",\"123/AB45678\",\"ABC-001\"",
            "\"ABC Property Services\",\"789/EF23456\",\"ABC-002\""
          ).mkString("\n")

          contentAsString(result) mustBe expectedBody
        }
      }

      "journey recovery when no userAnswers" in {
        val app = appWith(ua = None)
        running(app) {
          val req    = FakeRequest(GET, onPageLoadRoute)
          val result = route(app, req).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must neutralise CSV injection for dangerous leading characters" in {
        val dangerousClients = List(
          CisTaxpayerSearchResult(
            uniqueId = "UID-003",
            taxOfficeNumber = "123",
            taxOfficeRef = "AB45678",
            agentOwnRef = Some("=ABC"),
            schemeName = Some("""=Company("x")"""),
            utr = Some("1234567890")
          ),
          CisTaxpayerSearchResult(
            uniqueId = "UID-004",
            taxOfficeNumber = "124",
            taxOfficeRef = "AB45679",
            agentOwnRef = Some("\t=ABC"),
            schemeName = Some("\t=Company(\"x\")"),
            utr = Some("1234567890")
          ),
          CisTaxpayerSearchResult(
            uniqueId = "UID-005",
            taxOfficeNumber = "125",
            taxOfficeRef = "AB45680",
            agentOwnRef = Some("\r=ABC"),
            schemeName = Some("\r=Company(\"x\")"),
            utr = Some("1234567890")
          ),
          CisTaxpayerSearchResult(
            uniqueId = "UID-006",
            taxOfficeNumber = "126",
            taxOfficeRef = "AB45681",
            agentOwnRef = Some("\n=ABC"),
            schemeName = Some("\n=Company(\"x\")"),
            utr = Some("1234567890")
          )
        )

        val returnedUa = emptyUserAnswers
        val app        = {
          val manageService = mock[ManageService]
          val sessionRepo   = mock[SessionRepository]

          when(sessionRepo.set(any())) thenReturn Future.successful(true)
          when(manageService.resolveAndStoreAgentClients(any[UserAnswers])(using any[HeaderCarrier]))
            .thenReturn(Future.successful((dangerousClients, returnedUa)))

          applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
            .overrides(
              bind[ManageService].toInstance(manageService),
              bind[SessionRepository].toInstance(sessionRepo)
            )
            .build()
        }

        running(app) {
          val request = FakeRequest(GET, downloadRoute)
          val result  = route(app, request).value

          status(result) mustBe OK

          val msgs           = messages(app)
          val expectedHeader = Seq(
            msgs("agent.clientListSearch.th.clientName"),
            msgs("agent.clientListSearch.th.employerReference"),
            msgs("agent.clientListSearch.th.clientReference")
          ).mkString(",")

          val expectedBody = Seq(
            expectedHeader,
            "\"'=Company(\"\"x\"\")\",\"123/AB45678\",\"'=ABC\"",
            "\"'\t=Company(\"\"x\"\")\",\"124/AB45679\",\"'\t=ABC\"",
            "\"'\r=Company(\"\"x\"\")\",\"125/AB45680\",\"'\r=ABC\"",
            "\"'\n=Company(\"\"x\"\")\",\"126/AB45681\",\"'\n=ABC\""
          ).mkString("\n")

          contentAsString(result) mustBe expectedBody
        }
      }
    }
  }
}
