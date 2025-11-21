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
import org.mockito.Mockito.when
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
  "RetrievingClient Controller" - {

    "must redirect to the correct page for a GET" in {

      val mockCisService: ConstructionIndustrySchemeService = mock[ConstructionIndustrySchemeService]
      val application                                       = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = Seq(
          bind[ConstructionIndustrySchemeService].to(mockCisService)
        ),
        isAgent = true
      ).build()

      running(application) {

        when(mockCisService.getClientListStatus(using any[HeaderCarrier])).thenReturn(Future.successful("succeeded"))
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.onPageLoad().url)

        val result = route(application, request).value

        application.injector.instanceOf[RetrievingClientView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe routes.ClientListSearchController.onPageLoad().url
      }
    }

    "must redirect to the correct page for a GET when 'failed' is returned" in {

      val mockCisService: ConstructionIndustrySchemeService = mock[ConstructionIndustrySchemeService]
      val application                                       = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = Seq(
          bind[ConstructionIndustrySchemeService].to(mockCisService)
        )
      ).build()

      running(application) {

        when(mockCisService.getClientListStatus(using any[HeaderCarrier])).thenReturn(Future.successful("failed"))
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.onPageLoad().url)

        val result = route(application, request).value

        application.injector.instanceOf[RetrievingClientView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe routes.FailedToRetrieveClientController.onPageLoad().url
      }
    }

    "must redirect to the correct page for a GET when in-progress is returned" in {

      val mockCisService: ConstructionIndustrySchemeService = mock[ConstructionIndustrySchemeService]
      val application                                       = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = Seq(
          bind[ConstructionIndustrySchemeService].to(mockCisService)
        )
      ).build()

      running(application) {

        when(mockCisService.getClientListStatus(using any[HeaderCarrier])).thenReturn(Future.successful("in-progress"))
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.onPageLoad().url)

        val result = route(application, request).value

        application.injector.instanceOf[RetrievingClientView]

        status(result) mustEqual OK
        contentAsString(result) mustBe view()(request, applicationConfig, messages(application)).toString
      }
    }

    "must redirect to the correct page for a GET when system-error is returned" in {

      val mockCisService: ConstructionIndustrySchemeService = mock[ConstructionIndustrySchemeService]
      val application                                       = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = Seq(
          bind[ConstructionIndustrySchemeService].to(mockCisService)
        )
      ).build()

      running(application) {

        when(mockCisService.getClientListStatus(using any[HeaderCarrier]))
          .thenReturn(Future.successful("system-error"))
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.onPageLoad().url)

        val result = route(application, request).value

        application.injector.instanceOf[RetrievingClientView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to the correct page for a GET when initiate-download is returned" in {

      val mockCisService: ConstructionIndustrySchemeService = mock[ConstructionIndustrySchemeService]
      val application                                       = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = Seq(
          bind[ConstructionIndustrySchemeService].to(mockCisService)
        )
      ).build()

      running(application) {

        when(mockCisService.getClientListStatus(using any[HeaderCarrier]))
          .thenReturn(Future.successful("initiate-download"))
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.onPageLoad().url)

        val result = route(application, request).value

        application.injector.instanceOf[RetrievingClientView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

  }
}
