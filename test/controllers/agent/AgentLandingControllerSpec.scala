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
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import models.UserAnswers
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import services.ManageService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.agent.AgentLandingViewModel

import scala.concurrent.Future

class AgentLandingControllerSpec extends SpecBase with MockitoSugar {

  private val uniqueId = "some-unique-id"

  private val landingViewModel = AgentLandingViewModel(
    clientName = "Test Client",
    employerRef = "123/AB456",
    utr = Some("1234567890")
  )

  "AgentLandingController.onPageLoad" - {

    "must return OK and render the page when the service succeeds" in {
      val mockManageService = mock[ManageService]

      when(
        mockManageService.getAgentLandingData(
          eqTo(uniqueId),
          any[UserAnswers]
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(landingViewModel))

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithCisId),
          additionalBindings = Seq(
            bind[ManageService].toInstance(mockManageService)
          ),
          isAgent = true
        ).build()

      try {
        val request = FakeRequest(GET, controllers.agent.routes.AgentLandingController.onPageLoad(uniqueId).url)
        val result  = route(application, request).value

        status(result) mustBe OK
        val body = contentAsString(result)

        body must include("Test Client")
        body must include("123/AB456")
        body must include("1234567890")

        verify(mockManageService)
          .getAgentLandingData(eqTo(uniqueId), any[UserAnswers])(using any[HeaderCarrier])
      } finally application.stop()
    }

    "must redirect to JourneyRecoveryController when the service fails" in {
      val mockManageService = mock[ManageService]

      when(
        mockManageService.getAgentLandingData(
          eqTo(uniqueId),
          any[UserAnswers]
        )(using any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithCisId),
          additionalBindings = Seq(
            bind[ManageService].toInstance(mockManageService)
          ),
          isAgent = true
        ).build()

      try {
        val request =
          FakeRequest(GET, controllers.agent.routes.AgentLandingController.onPageLoad(uniqueId).url)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url

      } finally application.stop()
    }
  }
}
