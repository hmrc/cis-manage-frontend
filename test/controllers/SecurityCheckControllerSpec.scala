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

package controllers

import base.SpecBase
import models.agent.{ClientListCheckReturnTarget, ClientListStatus}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ConstructionIndustrySchemeService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.SecurityCheckView
import scala.concurrent.Future

class SecurityCheckControllerSpec extends SpecBase with MockitoSugar {

  "SecurityCheck Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.SecurityCheckController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecurityCheckView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "onClientListCheck" - {

      "must return OK with a Refresh header for a valid return target" in {

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          isAgent = true
        ).build()

        running(application) {
          val request = FakeRequest(
            GET,
            routes.SecurityCheckController
              .onClientListCheck(
                ClientListCheckReturnTarget.FileMonthlyReturns.key,
                None,
                None
              )
              .url
          )

          val result = route(application, request).value

          val expectedUrl = routes.SecurityCheckController
            .pollClientListCheck(
              ClientListCheckReturnTarget.FileMonthlyReturns.key,
              None,
              None,
              retryCount = 0
            )
            .url

          status(result) mustEqual OK
          headers(result).get("Refresh") mustEqual Some(s"15; url=$expectedUrl")
        }
      }

      "must redirect to system error for an invalid return target" in {

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          isAgent = true
        ).build()

        running(application) {
          val request = FakeRequest(
            GET,
            routes.SecurityCheckController
              .onClientListCheck(
                "invalid",
                None,
                None
              )
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to system error when agent dashboard has no instanceId" in {

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          isAgent = true
        ).build()

        running(application) {
          val request = FakeRequest(
            GET,
            routes.SecurityCheckController
              .onClientListCheck(
                ClientListCheckReturnTarget.AgentDashboard.key,
                None,
                None
              )
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }

    "pollClientListCheck" - {

      "must redirect to the return target when client list status is succeeded" in {

        val mockCisService = mock[ConstructionIndustrySchemeService]

        when(mockCisService.getClientListStatus(using any[HeaderCarrier]))
          .thenReturn(Future.successful(ClientListStatus.Succeeded))

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          isAgent = true,
          additionalBindings = Seq(
            bind[ConstructionIndustrySchemeService].toInstance(mockCisService)
          )
        ).build()

        running(application) {
          val request = FakeRequest(
            GET,
            routes.SecurityCheckController
              .pollClientListCheck(
                ClientListCheckReturnTarget.FileMonthlyReturns.key,
                None,
                None
              )
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.agent.routes.ClientListSearchController.onPageLoad().url
        }
      }

      "must redirect to agent dashboard when client list status is succeeded" in {

        val mockCisService = mock[ConstructionIndustrySchemeService]

        when(mockCisService.getClientListStatus(using any[HeaderCarrier]))
          .thenReturn(Future.successful(ClientListStatus.Succeeded))

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          isAgent = true,
          additionalBindings = Seq(
            bind[ConstructionIndustrySchemeService].toInstance(mockCisService)
          )
        ).build()

        running(application) {
          val instanceId = "123456"

          val request = FakeRequest(
            GET,
            routes.SecurityCheckController
              .pollClientListCheck(
                ClientListCheckReturnTarget.AgentDashboard.key,
                Some(instanceId),
                None
              )
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.agent.routes.AgentLandingController.onPageLoad(instanceId).url
        }
      }

      "must return OK with a Refresh header when client list status is in progress" in {

        val mockCisService = mock[ConstructionIndustrySchemeService]

        when(mockCisService.getClientListStatus(using any[HeaderCarrier]))
          .thenReturn(Future.successful(ClientListStatus.InProgress))

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          isAgent = true,
          additionalBindings = Seq(
            bind[ConstructionIndustrySchemeService].toInstance(mockCisService)
          )
        ).build()

        running(application) {
          val request = FakeRequest(
            GET,
            routes.SecurityCheckController
              .pollClientListCheck(
                ClientListCheckReturnTarget.FileMonthlyReturns.key,
                None,
                None
              )
              .url
          )

          val result = route(application, request).value

          val expectedUrl = routes.SecurityCheckController
            .pollClientListCheck(
              ClientListCheckReturnTarget.FileMonthlyReturns.key,
              None,
              None,
              retryCount = 1
            )
            .url

          status(result) mustEqual OK
          headers(result).get("Refresh") mustEqual Some(s"15; url=$expectedUrl")
        }
      }

      "must redirect to system error when client list status is failed" in {

        val mockCisService = mock[ConstructionIndustrySchemeService]

        when(mockCisService.getClientListStatus(using any[HeaderCarrier]))
          .thenReturn(Future.successful(ClientListStatus.Failed))

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          isAgent = true,
          additionalBindings = Seq(
            bind[ConstructionIndustrySchemeService].toInstance(mockCisService)
          )
        ).build()

        running(application) {
          val request = FakeRequest(
            GET,
            routes.SecurityCheckController
              .pollClientListCheck(
                ClientListCheckReturnTarget.FileMonthlyReturns.key,
                None,
                None
              )
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to system error when client list status is initiate download" in {

        val mockCisService = mock[ConstructionIndustrySchemeService]

        when(mockCisService.getClientListStatus(using any[HeaderCarrier]))
          .thenReturn(Future.successful(ClientListStatus.InitiateDownload))

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          isAgent = true,
          additionalBindings = Seq(
            bind[ConstructionIndustrySchemeService].toInstance(mockCisService)
          )
        ).build()

        running(application) {
          val request = FakeRequest(
            GET,
            routes.SecurityCheckController
              .pollClientListCheck(
                ClientListCheckReturnTarget.FileMonthlyReturns.key,
                None,
                None
              )
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to system error when max retries are exceeded" in {

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          isAgent = true
        ).build()

        running(application) {
          val request = FakeRequest(
            GET,
            routes.SecurityCheckController
              .pollClientListCheck(
                ClientListCheckReturnTarget.FileMonthlyReturns.key,
                None,
                None,
                retryCount = 2
              )
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to system error for an invalid return target" in {

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          isAgent = true
        ).build()

        running(application) {
          val request = FakeRequest(
            GET,
            routes.SecurityCheckController
              .pollClientListCheck(
                "invalid",
                None,
                None
              )
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to system error when client list service fails" in {

        val mockCisService = mock[ConstructionIndustrySchemeService]

        when(mockCisService.getClientListStatus(using any[HeaderCarrier]))
          .thenReturn(Future.failed(new RuntimeException("boom")))

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          isAgent = true,
          additionalBindings = Seq(
            bind[ConstructionIndustrySchemeService].toInstance(mockCisService)
          )
        ).build()

        running(application) {
          val request = FakeRequest(
            GET,
            routes.SecurityCheckController
              .pollClientListCheck(
                ClientListCheckReturnTarget.FileMonthlyReturns.key,
                None,
                None
              )
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }
}
