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
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ConstructionIndustrySchemeService
import views.html.agent.RetrievingClientView
import play.api.inject.bind
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when

import scala.concurrent.Future

class RetrievingClientControllerSpec extends SpecBase with MockitoSugar {

  "RetrievingClient Controller" - {

    "must redirect to the correct page for a GET" in {

      val mockCisService: ConstructionIndustrySchemeService = mock[ConstructionIndustrySchemeService]
      val application                                       = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = Seq(
          bind[ConstructionIndustrySchemeService].to(mockCisService)
        )
      ).build()

      running(application) {

        when(mockCisService.getClientListStatus(using any[HeaderCarrier])).thenReturn(Future.successful("Success"))
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RetrievingClientView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe "/success"
      }
    }
  }
}
