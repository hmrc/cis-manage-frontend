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
import models.{CisTaxpayerSearchResult, Scheme, UserAnswers}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.AgentClientsPage
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import services.{ManageService, PrepopService}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import viewmodels.agent.AgentLandingViewModel

import scala.concurrent.Future

class AgentLandingControllerSpec extends SpecBase with MockitoSugar {

  private val uniqueId = "some-unique-id"

  private val landingViewModel = AgentLandingViewModel(
    clientName = "Test Client",
    employerRef = "123/AB456",
    utr = Some("1234567890")
  )

  private val client = CisTaxpayerSearchResult(
    uniqueId = uniqueId,
    taxOfficeNumber = "163",
    taxOfficeRef = "AB0063",
    agentOwnRef = Some("ref123"),
    schemeName = Some("Test Client"),
    utr = Some("1234567890")
  )

  private val userAnswersWithAgentClient: UserAnswers =
    emptyUserAnswers
      .set(AgentClientsPage, List(client))
      .success
      .value

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

  "AgentLandingController.onTargetClick" - {

    val returnsTargetKey = "returnDue"

    "must call prepopulate + getScheme and redirect using determineLandingDestination when scheme is found" in {
      val mockPrepopService = mock[PrepopService]

      val scheme = Scheme(
        schemeId = 123,
        instanceId = uniqueId,
        utr = Some("1234567890"),
        name = Some("Test Client"),
        prePopSuccessful = Some("Y"),
        subcontractorCounter = Some(0)
      )

      when(
        mockPrepopService.prepopulateContractorKnownFacts(
          any[String],
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(Future.unit)

      when(
        mockPrepopService.getScheme(any[String])(any[HeaderCarrier])
      ).thenReturn(Future.successful(Some(scheme)))

      when(
        mockPrepopService.determineLandingDestination(
          any[Call],
          any[String],
          any[Scheme],
          any[Call],
          any[Call]
        )
      ).thenReturn(controllers.routes.ReturnsLandingController.onPageLoad(uniqueId))

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithAgentClient),
          additionalBindings = Seq(
            bind[PrepopService].toInstance(mockPrepopService)
          ),
          isAgent = true
        ).build()

      try {
        val request =
          FakeRequest(
            GET,
            controllers.agent.routes.AgentLandingController.onTargetClick(uniqueId, returnsTargetKey).url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must endWith(
          controllers.routes.ReturnsLandingController.onPageLoad(uniqueId).url
        )

        verify(mockPrepopService)
          .prepopulateContractorKnownFacts(eqTo(uniqueId), eqTo("163"), eqTo("AB0063"))(any[HeaderCarrier])

        verify(mockPrepopService)
          .getScheme(eqTo(uniqueId))(any[HeaderCarrier])

        verify(mockPrepopService)
          .determineLandingDestination(
            any[Call],
            eqTo(uniqueId),
            eqTo(scheme),
            any[Call],
            any[Call]
          )
      } finally application.stop()
    }

    "must redirect to SystemErrorController when prepopulateContractorKnownFacts fails with UpstreamErrorResponse" in {
      val mockPrepopService = mock[PrepopService]

      when(
        mockPrepopService.prepopulateContractorKnownFacts(
          any[String],
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.failed(UpstreamErrorResponse("boom", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR))
      )

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithAgentClient),
          additionalBindings = Seq(
            bind[PrepopService].toInstance(mockPrepopService)
          ),
          isAgent = true
        ).build()

      try {
        val request =
          FakeRequest(
            GET,
            controllers.agent.routes.AgentLandingController.onTargetClick(uniqueId, returnsTargetKey).url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url

      } finally application.stop()
    }

    "must redirect to SystemErrorController when prepopulateContractorKnownFacts fails with an unexpected error" in {
      val mockPrepopService = mock[PrepopService]

      when(
        mockPrepopService.prepopulateContractorKnownFacts(
          any[String],
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.failed(new RuntimeException("boom"))
      )

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithAgentClient),
          additionalBindings = Seq(
            bind[PrepopService].toInstance(mockPrepopService)
          ),
          isAgent = true
        ).build()

      try {
        val request =
          FakeRequest(
            GET,
            controllers.agent.routes.AgentLandingController.onTargetClick(uniqueId, returnsTargetKey).url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url

      } finally application.stop()
    }

    "must redirect to SystemErrorController when client is missing from AgentClientsPage" in {
      val mockPrepopService = mock[PrepopService]

      val uaWithoutClient =
        emptyUserAnswers
          .set(AgentClientsPage, List.empty[CisTaxpayerSearchResult])
          .success
          .value

      val application: Application =
        applicationBuilder(
          userAnswers = Some(uaWithoutClient),
          additionalBindings = Seq(
            bind[PrepopService].toInstance(mockPrepopService)
          ),
          isAgent = true
        ).build()

      try {
        val request =
          FakeRequest(
            GET,
            controllers.agent.routes.AgentLandingController.onTargetClick(uniqueId, returnsTargetKey).url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url

        verifyNoInteractions(mockPrepopService)

      } finally application.stop()
    }

    "must return NotFound when targetKey is unknown" in {
      val mockPrepopService = mock[PrepopService]

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithAgentClient),
          additionalBindings = Seq(
            bind[PrepopService].toInstance(mockPrepopService)
          ),
          isAgent = true
        ).build()

      try {
        val request =
          FakeRequest(
            GET,
            controllers.agent.routes.AgentLandingController.onTargetClick(uniqueId, "unknownTargetKey").url
          )

        val result = route(application, request).value

        status(result) mustBe NOT_FOUND
        verifyNoInteractions(mockPrepopService)

      } finally application.stop()
    }

    "must redirect to SystemErrorController when getScheme returns None" in {
      val mockPrepopService = mock[PrepopService]

      when(
        mockPrepopService.prepopulateContractorKnownFacts(any[String], any[String], any[String])(any[HeaderCarrier])
      ).thenReturn(Future.unit)

      when(
        mockPrepopService.getScheme(any[String])(any[HeaderCarrier])
      ).thenReturn(Future.successful(None))

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithAgentClient),
          additionalBindings = Seq(bind[PrepopService].toInstance(mockPrepopService)),
          isAgent = true
        ).build()

      try {
        val request =
          FakeRequest(
            GET,
            controllers.agent.routes.AgentLandingController.onTargetClick(uniqueId, returnsTargetKey).url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url

        verify(mockPrepopService)
          .prepopulateContractorKnownFacts(eqTo(uniqueId), eqTo("163"), eqTo("AB0063"))(any[HeaderCarrier])
        verify(mockPrepopService)
          .getScheme(eqTo(uniqueId))(any[HeaderCarrier])
      } finally application.stop()
    }
  }
}
