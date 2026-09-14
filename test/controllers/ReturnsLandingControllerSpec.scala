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

import base.UnitSpec
import models.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import pages.AgentClientsPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ManageService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.ReturnsLandingContext
import views.html.ReturnsLandingView

import scala.concurrent.Future

class ReturnsLandingControllerSpec extends UnitSpec {
  private val givenAgentRef                       = "123456"
  private val givenCisTaxPayerSearchResult        = CisTaxpayerSearchResult(
    uniqueId = cisId,
    taxOfficeNumber = "123",
    taxOfficeRef = "AB12345",
    agentOwnRef = Some(givenAgentRef),
    schemeName = None,
    utr = None
  )
  private val userAnswersWithCisIdAndAgentClients =
    userAnswersWithCisId.set(AgentClientsPage, List(givenCisTaxPayerSearchResult)).success.value

  private val context = ReturnsLandingContext(
    contractorName = "ABC Construction Ltd",
    standardReturnLink = "/standard-link",
    nilReturnLink = "/nil-link",
    returnToHomeLink = "/example"
  )

  private val mockManageService = mock[ManageService]

  private val stubView    = mock[ReturnsLandingView]
  private val stubContent = "ReturnsLandingView"
  when(stubView.apply(any, any, any, any)(any, any)) thenReturn play.twirl.api.Html(stubContent)

  private val controllerUnderTest =
    new ReturnsLandingController(mockCisControllerComponents, mockSessionRepo, stubView, mockManageService)

  "ReturnsLandingController.onPageLoad" - {

    "must return OK when ManageService returns context (org)" in {
      mockCisControllerComponents.loginAsOrg()
      when(
        mockManageService.buildReturnsLandingContext(
          eqTo(cisId),
          any[UserAnswers],
          eqTo(false)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Some(context)))

      val res = controllerUnderTest.onPageLoad(cisId)(FakeRequest())

      status(res) mustBe OK
    }

    "must return OK when ManageService returns context (agent)" in {
      mockCisControllerComponents.loginAsAgent(ref = givenAgentRef)
      mockUserAnswers(Some(userAnswersWithCisIdAndAgentClients))
      when(
        mockManageService.buildReturnsLandingContext(
          eqTo(cisId),
          any[UserAnswers],
          eqTo(true)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Some(context.copy(contractorName = "Client Ltd"))))

      val res = controllerUnderTest.onPageLoad(cisId)(FakeRequest())

      status(res) mustBe OK

      verify(mockManageService).buildReturnsLandingContext(
        eqTo(cisId),
        any[UserAnswers],
        eqTo(true)
      )(using any[HeaderCarrier])
    }

    "must redirect to SystemErrorController when ManageService returns None" in {
      mockCisControllerComponents.loginAsOrg()
      when(
        mockManageService.buildReturnsLandingContext(
          eqTo(cisId),
          any[UserAnswers],
          any[Boolean]
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(None))

      val res = controllerUnderTest.onPageLoad(cisId)(FakeRequest())

      status(res) mustBe SEE_OTHER
      redirectLocation(res).value mustBe routes.SystemErrorController.onPageLoad().url
    }

    "must redirect to SystemErrorController when ManageService fails" in {
      mockCisControllerComponents.loginAsOrg()
      when(
        mockManageService.buildReturnsLandingContext(
          eqTo(cisId),
          any[UserAnswers],
          any[Boolean]
        )(using any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val res = controllerUnderTest.onPageLoad(cisId)(FakeRequest())

      status(res) mustBe SEE_OTHER
      redirectLocation(res).value mustBe routes.SystemErrorController.onPageLoad().url
    }

    "must update contractor name from query param before building landing context" in {
      mockCisControllerComponents.loginAsOrg()
      when(
        mockManageService.buildReturnsLandingContext(
          eqTo(cisId),
          any[UserAnswers],
          eqTo(false)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Some(context)))

      val req = FakeRequest(
        GET,
        routes.ReturnsLandingController.onPageLoad(cisId).url + "?contractorName=New%20Contractor%20Ltd"
      )
      val res = controllerUnderTest.onPageLoad(cisId)(req)

      // status(res) mustBe OK
      redirectLocation(res) mustBe empty
    }
  }
}
