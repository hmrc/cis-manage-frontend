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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ConstructionIndustrySchemeService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.agent.RetrievingClientView

import scala.concurrent.Future

class RetrievingClientControllerSpec extends SpecBase with MockitoSugar {

  lazy val view: RetrievingClientView = app.injector.instanceOf[RetrievingClientView]

  private def buildAppWithStatus(statusF: Future[String]) = {
    val mockCisService = mock[ConstructionIndustrySchemeService]
    when(mockCisService.getClientListStatus(using any[HeaderCarrier]))
      .thenReturn(statusF)

    val application =
      applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = Seq(bind[ConstructionIndustrySchemeService].toInstance(mockCisService)),
        isAgent = true
      ).build()

    (application, mockCisService)
  }

  private def buildAppWithStartStatus(startStatusF: Future[String]) = {
    val mockCisService = mock[ConstructionIndustrySchemeService]
    when(mockCisService.startClientListRetrieval(using any[HeaderCarrier]))
      .thenReturn(startStatusF)

    when(mockCisService.getClientListStatus(using any[HeaderCarrier]))
      .thenReturn(Future.successful("in-progress"))

    val application =
      applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = Seq(bind[ConstructionIndustrySchemeService].toInstance(mockCisService)),
        isAgent = true
      ).build()

    (application, mockCisService)
  }

  "RetrievingClientController.onPageLoad" - {

    "must redirect to client list search when status is succeeded" in {
      val (application, _) = buildAppWithStatus(Future.successful("succeeded"))

      running(application) {
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe
          routes.ClientListSearchController.onPageLoad().url
      }
    }

    "must redirect to failed-to-retrieve when status is failed" in {
      val (application, _) = buildAppWithStatus(Future.successful("failed"))

      running(application) {
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe
          routes.FailedToRetrieveClientController.onPageLoad().url
      }
    }

    "must return OK with Refresh header when status is in-progress (first retry)" in {
      val (application, _) = buildAppWithStatus(Future.successful("in-progress"))

      running(application) {
        val baseUrl     = controllers.agent.routes.RetrievingClientController.onPageLoad().url
        val expectedUrl = controllers.agent.routes.RetrievingClientController.onPageLoad(retryCount = 1).url
        val request     = FakeRequest(GET, baseUrl)
        val result      = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustBe view()(request, applicationConfig, messages(application)).toString

        headers(result).get("Refresh") mustBe Some(s"15; url=$expectedUrl")
      }
    }

    "must increment RetryCount and refresh again when still in-progress" in {
      val (application, _) = buildAppWithStatus(Future.successful("in-progress"))

      running(application) {
        val request            = FakeRequest(
          GET,
          controllers.agent.routes.RetrievingClientController.onPageLoad(retryCount = 3).url
        )
        val result             = route(application, request).value
        val expectedRefreshUrl =
          controllers.agent.routes.RetrievingClientController.onPageLoad(retryCount = 4).url

        status(result) mustEqual OK
        headers(result).get("Refresh") mustBe Some(s"15; url=$expectedRefreshUrl")
      }
    }

    "must redirect to failed-to-retrieve when max retries exceeded" in {
      val (application, mockCisService) =
        buildAppWithStatus(Future.successful("in-progress"))

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.agent.routes.RetrievingClientController.onPageLoad(retryCount = 8).url
        )
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe
          routes.FailedToRetrieveClientController.onPageLoad().url

        verifyNoInteractions(mockCisService)
      }
    }

    "must redirect to system error for any other/terminal status" in {
      val terminalStatuses = Seq("system-error", "initiate-download", "weird-status")
      terminalStatuses.foreach { s =>
        val (application, _) = buildAppWithStatus(Future.successful(s))

        running(application) {
          val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe
            controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }

    "must redirect to system error if the service call fails" in {
      val (application, _) =
        buildAppWithStatus(Future.failed(new RuntimeException("boom")))

      running(application) {
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }

  "RetrievingClientController.start" - {

    "must redirect to client list search when start status is succeeded" in {
      val (application, _) = buildAppWithStartStatus(Future.successful("succeeded"))

      running(application) {
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.start().url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.agent.routes.ClientListSearchController.onPageLoad().url
      }
    }

    "must redirect to failed-to-retrieve when start status is failed" in {
      val (application, _) = buildAppWithStartStatus(Future.successful("failed"))

      running(application) {
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.start().url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.agent.routes.FailedToRetrieveClientController.onPageLoad().url
      }
    }

    "must redirect to polling page with RetryCount=1 when start status is in-progress" in {
      val (application, _) = buildAppWithStartStatus(Future.successful("in-progress"))

      running(application) {
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.start().url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.agent.routes.RetrievingClientController.onPageLoad(retryCount = 1).url
      }
    }

    "must redirect to system error for any other start status" in {
      val (application, _) = buildAppWithStartStatus(Future.successful("weird-status"))

      running(application) {
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.start().url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to system error if the start call fails" in {
      val (application, _) = buildAppWithStartStatus(Future.failed(new RuntimeException("boom")))

      running(application) {
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.start().url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
