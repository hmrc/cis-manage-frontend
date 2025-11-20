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

package controllers

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.inject.bind
import repositories.SessionRepository
import services.ConstructionIndustrySchemeService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.IntroductionView

import scala.concurrent.Future

class IntroductionControllerSpec extends SpecBase {

  private val routeRouting = routes.IntroductionController.affinityGroupRouting().url

  "IntroductionController.onPageLoad" - {
    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.IntroductionController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IntroductionView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(
          request,
          applicationConfig,
          messages(application)
        ).toString

      }
    }
  }

  "IntroductionController.affinityGroupRouting" - {

    "for a contractor" - {

      "must redirect to the contractor landing page" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.IntroductionController.affinityGroupRouting().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.contractor.routes.ContractorLandingController
            .onPageLoad()
            .url
        }
      }

    }

    "for an agent" - {

      "must redirect to client list search page when status is succeeded" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val mockCisService = mock[ConstructionIndustrySchemeService]
        when(mockCisService.startClientListRetrieval(using any[HeaderCarrier]))
          .thenReturn(Future.successful("succeeded"))

        val application = applicationBuilder(userAnswers = None, isAgent = true)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ConstructionIndustrySchemeService].toInstance(mockCisService)
          )
          .build()

        running(application) {
          val controller = application.injector.instanceOf[IntroductionController]
          val request    = FakeRequest(POST, routeRouting)
          val result     = controller.affinityGroupRouting()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.agent.routes.ClientListSearchController.onPageLoad().url
        }
      }

      "must redirect to failed-to-retrieve page when status is failed" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val mockCisService = mock[ConstructionIndustrySchemeService]
        when(mockCisService.startClientListRetrieval(using any[HeaderCarrier]))
          .thenReturn(Future.successful("failed"))

        val application = applicationBuilder(userAnswers = None, isAgent = true)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ConstructionIndustrySchemeService].toInstance(mockCisService)
          )
          .build()

        running(application) {
          val controller = application.injector.instanceOf[IntroductionController]
          val request    = FakeRequest(POST, routeRouting)
          val result     = controller.affinityGroupRouting()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.agent.routes.FailedToRetrieveClientController.onPageLoad().url
        }
      }

      "must redirect to retrieving page with RetryCount=1 when status is in-progress" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val mockCisService = mock[ConstructionIndustrySchemeService]
        when(mockCisService.startClientListRetrieval(using any[HeaderCarrier]))
          .thenReturn(Future.successful("in-progress"))

        val application = applicationBuilder(userAnswers = None, isAgent = true)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ConstructionIndustrySchemeService].toInstance(mockCisService)
          )
          .build()

        running(application) {
          val controller = application.injector.instanceOf[IntroductionController]
          val request    = FakeRequest(POST, routeRouting)
          val result     = controller.affinityGroupRouting()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.agent.routes.RetrievingClientController.onPageLoad().url + "?RetryCount=1"
        }
      }

      "must redirect to system error page when status is initiate-download" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val mockCisService = mock[ConstructionIndustrySchemeService]
        when(mockCisService.startClientListRetrieval(using any[HeaderCarrier]))
          .thenReturn(Future.successful("initiate-download"))

        val application = applicationBuilder(userAnswers = None, isAgent = true)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ConstructionIndustrySchemeService].toInstance(mockCisService)
          )
          .build()

        running(application) {
          val controller = application.injector.instanceOf[IntroductionController]
          val request    = FakeRequest(POST, routeRouting)
          val result     = controller.affinityGroupRouting()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to system error page when service call fails" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val mockCisService = mock[ConstructionIndustrySchemeService]
        when(mockCisService.startClientListRetrieval(using any[HeaderCarrier]))
          .thenReturn(Future.failed(new RuntimeException("boom")))

        val application = applicationBuilder(userAnswers = None, isAgent = true)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ConstructionIndustrySchemeService].toInstance(mockCisService)
          )
          .build()

        running(application) {
          val controller = application.injector.instanceOf[IntroductionController]
          val request    = FakeRequest(POST, routeRouting)
          val result     = controller.affinityGroupRouting()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }
}
