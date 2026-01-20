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

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import base.SpecBase
import models.UserAnswers
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ManageService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.{ReturnLandingViewModel, ReturnsLandingContext}
import scala.concurrent.Future

class ReturnsLandingControllerSpec extends SpecBase with MockitoSugar {

  private val instanceId = "CIS-123"

  private val context = ReturnsLandingContext(
    contractorName = "ABC Construction Ltd",
    standardReturnLink = "/standard-link",
    nilReturnLink = "/nil-link",
    returnsList = Seq(
      ReturnLandingViewModel("August 2025", "Standard", "19 September 2025", "In progress"),
      ReturnLandingViewModel("July 2025", "Nil", "19 August 2025", "In progress")
    )
  )

  "ReturnsLandingController.onPageLoad" - {

    "must return OK when ManageService returns context (org)" in {
      val mockManageService = mock[ManageService]

      when(
        mockManageService.buildReturnsLandingContext(
          eqTo(instanceId),
          any[UserAnswers],
          eqTo(false)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Some(context)))

      val app =
        applicationBuilder(
          userAnswers = Some(userAnswersWithCisId),
          isAgent = false,
          additionalBindings = Seq(bind[ManageService].toInstance(mockManageService))
        ).build()

      running(app) {
        val req = FakeRequest(GET, controllers.routes.ReturnsLandingController.onPageLoad(instanceId).url)
        val res = route(app, req).value

        status(res) mustBe OK
      }
    }

    "must return OK when ManageService returns context (agent)" in {
      val mockManageService = mock[ManageService]

      when(
        mockManageService.buildReturnsLandingContext(
          eqTo(instanceId),
          any[UserAnswers],
          eqTo(true)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Some(context.copy(contractorName = "Client Ltd"))))

      val app =
        applicationBuilder(
          userAnswers = Some(userAnswersWithCisId),
          isAgent = true,
          additionalBindings = Seq(bind[ManageService].toInstance(mockManageService))
        ).build()

      running(app) {
        val req = FakeRequest(GET, controllers.routes.ReturnsLandingController.onPageLoad(instanceId).url)
        val res = route(app, req).value

        status(res) mustBe OK

        verify(mockManageService).buildReturnsLandingContext(
          eqTo(instanceId),
          any[UserAnswers],
          eqTo(true)
        )(using any[HeaderCarrier])
      }
    }

    "must redirect to SystemErrorController when ManageService returns None OR fails" in {
      val mockManageService = mock[ManageService]

      when(
        mockManageService.buildReturnsLandingContext(
          eqTo(instanceId),
          any[UserAnswers],
          any[Boolean]
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(None))

      val app =
        applicationBuilder(
          userAnswers = Some(userAnswersWithCisId),
          additionalBindings = Seq(bind[ManageService].toInstance(mockManageService))
        ).build()

      running(app) {
        val req = FakeRequest(GET, controllers.routes.ReturnsLandingController.onPageLoad(instanceId).url)
        val res = route(app, req).value

        status(res) mustBe SEE_OTHER
        redirectLocation(res).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
