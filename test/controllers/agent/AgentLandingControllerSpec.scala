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
import models.{CisTaxpayerSearchResult, Scheme, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.mockito.MockitoSugar
import pages.{AgentClientsPage, CisIdPage}
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{ManageService, PrepopService}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import viewmodels.agent.AgentLandingViewModel

import scala.concurrent.Future

class AgentLandingControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterAll with BeforeAndAfterEach {

  private val userId   = "id"
  private val uniqueId = "some-unique-id"

  private val mockManageService     = mock[ManageService]
  private val mockSessionRepository = mock[SessionRepository]
  private val mockPrepopService     = mock[PrepopService]

  private val landingViewModel = AgentLandingViewModel(
    schemeName = "Test scheme name",
    employerRef = "123/AB456"
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

  private val userAnswersWithoutAgentClient: UserAnswers =
    emptyUserAnswers
      .set(AgentClientsPage, List.empty[CisTaxpayerSearchResult])
      .success
      .value

  private val commonBindings = Seq(
    bind[ManageService].toInstance(mockManageService),
    bind[SessionRepository].toInstance(mockSessionRepository),
    bind[PrepopService].toInstance(mockPrepopService)
  )

  private def withApplication[T](application: Application)(block: => T): T =
    try block
    finally application.stop().futureValue

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
  }

  override def afterEach(): Unit = {
    reset(mockManageService, mockSessionRepository, mockPrepopService)
    super.afterEach()
  }

  "AgentLandingController.onPageLoad" - {

    "must return OK and render the page when the service succeeds" in {

      when(
        mockManageService.getAgentLandingData(
          eqTo(uniqueId),
          any[UserAnswers],
          eqTo(userId)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(landingViewModel))

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithAgentClient),
          additionalBindings = commonBindings,
          isAgent = true
        ).build()

      withApplication(application) {
        val request = FakeRequest(GET, controllers.agent.routes.AgentLandingController.onPageLoad(uniqueId).url)
        val result  = route(application, request).value

        status(result) mustBe OK
        val body = contentAsString(result)

        body must include("No name")
        body must include("Test scheme name")
        body must include("123/AB456")

        val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(CisIdPage) mustBe Some(uniqueId)

        verify(mockManageService)
          .getAgentLandingData(eqTo(uniqueId), any[UserAnswers], eqTo(userId))(using any[HeaderCarrier])
      }
    }

    "must return OK and render the page when the service succeeds with ITMP name" in {

      when(
        mockManageService.getAgentLandingData(
          eqTo(uniqueId),
          any[UserAnswers],
          eqTo(userId)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(landingViewModel))

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithAgentClient),
          additionalBindings = commonBindings,
          isAgent = true,
          itmpName = Some("Test name")
        ).build()

      withApplication(application) {
        val request = FakeRequest(GET, controllers.agent.routes.AgentLandingController.onPageLoad(uniqueId).url)
        val result  = route(application, request).value

        status(result) mustBe OK
        val body = contentAsString(result)

        body must include("Test name")
        body must include("Test scheme name")
        body must include("123/AB456")

        val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(CisIdPage) mustBe Some(uniqueId)

        verify(mockManageService)
          .getAgentLandingData(eqTo(uniqueId), any[UserAnswers], eqTo(userId))(using any[HeaderCarrier])
      }
    }

    "must redirect to JourneyRecoveryController when client employee tax office number & tax office reference not available" in {

      when(
        mockManageService.getAgentLandingData(
          eqTo(uniqueId),
          any[UserAnswers],
          eqTo(userId)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(landingViewModel))

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithCisId),
          additionalBindings = commonBindings,
          isAgent = true,
          itmpName = Some("Test name")
        ).build()

      withApplication(application) {
        val request = FakeRequest(GET, controllers.agent.routes.AgentLandingController.onPageLoad(uniqueId).url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url

      }
    }

    "must redirect to JourneyRecoveryController when the service fails" in {

      when(
        mockManageService.getAgentLandingData(
          eqTo(uniqueId),
          any[UserAnswers],
          eqTo(userId)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithCisId),
          additionalBindings = commonBindings,
          isAgent = true
        ).build()

      withApplication(application) {
        val request =
          FakeRequest(GET, controllers.agent.routes.AgentLandingController.onPageLoad(uniqueId).url)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "AgentLandingController.onTargetClick" - {

    val returnsTargetKey = "returnDue"

    "must call prepopulate + getScheme and redirect using determineLandingDestination when scheme is found" in {

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
          additionalBindings = commonBindings,
          isAgent = true
        ).build()

      withApplication(application) {
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
      }
    }

    "must redirect to SystemErrorController when prepopulateContractorKnownFacts fails with UpstreamErrorResponse" in {

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
          additionalBindings = commonBindings,
          isAgent = true
        ).build()

      withApplication(application) {
        val request =
          FakeRequest(
            GET,
            controllers.agent.routes.AgentLandingController.onTargetClick(uniqueId, returnsTargetKey).url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemErrorController when prepopulateContractorKnownFacts fails with an unexpected error" in {

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
          additionalBindings = commonBindings,
          isAgent = true
        ).build()

      withApplication(application) {
        val request =
          FakeRequest(
            GET,
            controllers.agent.routes.AgentLandingController.onTargetClick(uniqueId, returnsTargetKey).url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemErrorController when client is missing from AgentClientsPage" in {

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithoutAgentClient),
          additionalBindings = commonBindings,
          isAgent = true
        ).build()

      withApplication(application) {
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
      }
    }

    "must return NotFound when targetKey is unknown" in {

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithAgentClient),
          additionalBindings = commonBindings,
          isAgent = true
        ).build()

      withApplication(application) {
        val request =
          FakeRequest(
            GET,
            controllers.agent.routes.AgentLandingController.onTargetClick(uniqueId, "unknownTargetKey").url
          )

        val result = route(application, request).value

        status(result) mustBe NOT_FOUND
        verifyNoInteractions(mockPrepopService)
      }
    }

    "must redirect to SystemErrorController when getScheme returns None" in {

      when(
        mockPrepopService.prepopulateContractorKnownFacts(any[String], any[String], any[String])(any[HeaderCarrier])
      ).thenReturn(Future.unit)

      when(
        mockPrepopService.getScheme(any[String])(any[HeaderCarrier])
      ).thenReturn(Future.successful(None))

      val application: Application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithAgentClient),
          additionalBindings = commonBindings,
          isAgent = true
        ).build()

      withApplication(application) {
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
      }
    }
  }
}
