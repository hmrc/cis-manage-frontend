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
import models.{UnsubmittedMonthlyReturn, UserAnswers}
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.{SessionRepository, UnsubmittedMonthlyReturnRepository}
import services.ManageService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.{ReturnLandingViewModel, ReturnsLandingContext}

import java.time.Instant
import scala.concurrent.Future

class ReturnsLandingControllerSpec extends SpecBase with MockitoSugar {

  private val instanceId = "CIS-123"

  private val context = ReturnsLandingContext(
    contractorName = "ABC Construction Ltd",
    standardReturnLink = "/standard-link",
    nilReturnLink = "/nil-link",
    returnsList = Seq(
      ReturnLandingViewModel(3000L, "August 2025", "Standard", "19 September 2025", "In progress", Some("Y")),
      ReturnLandingViewModel(3001L, "July 2025", "Nil", "19 August 2025", "In progress", Some("Y"))
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

    "must redirect to SystemErrorController when ManageService returns None" in {
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

    "must redirect to SystemErrorController when ManageService fails" in {
      val mockManageService = mock[ManageService]

      when(
        mockManageService.buildReturnsLandingContext(
          eqTo(instanceId),
          any[UserAnswers],
          any[Boolean]
        )(using any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("boom")))

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

  "ReturnsLandingController.onDeleteRedirect" - {

    val monthlyReturnId = 3000L
    val now: Instant    = Instant.parse("2026-04-09T12:34:56.789Z")

    lazy val onDeleteRedirectRoute: String =
      controllers.routes.ReturnsLandingController.onDeleteRedirect(monthlyReturnId).url

    "must redirect to DeleteAmendedNilMonthlyReturnController" in {
      val mockReturnsRepo       = mock[UnsubmittedMonthlyReturnRepository]
      val mockSessionRepository = mock[SessionRepository]

      val mockDbRecordOpt = Some(
        UnsubmittedMonthlyReturn(
          instanceId = "1",
          monthlyReturnId = monthlyReturnId,
          taxYear = 2026,
          taxMonth = 4,
          returnType = "Nil",
          status = "In Progress",
          amendment = Some("Y"),
          deletable = true,
          lastUpdated = now
        )
      )
      when(mockReturnsRepo.get(eqTo(monthlyReturnId))).thenReturn(Future.successful(mockDbRecordOpt))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UnsubmittedMonthlyReturnRepository].toInstance(mockReturnsRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, onDeleteRedirectRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.delete.routes.DeleteAmendedNilMonthlyReturnController
          .onPageLoad()
          .url
      }
    }

    "must redirect to DeleteNilMonthlyReturnController" in {
      val mockReturnsRepo       = mock[UnsubmittedMonthlyReturnRepository]
      val mockSessionRepository = mock[SessionRepository]

      val mockDbRecordOpt = Some(
        UnsubmittedMonthlyReturn(
          instanceId = "1",
          monthlyReturnId = monthlyReturnId,
          taxYear = 2026,
          taxMonth = 4,
          returnType = "Nil",
          status = "In Progress",
          amendment = Some("N"),
          deletable = true,
          lastUpdated = now
        )
      )
      when(mockReturnsRepo.get(eqTo(monthlyReturnId))).thenReturn(Future.successful(mockDbRecordOpt))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UnsubmittedMonthlyReturnRepository].toInstance(mockReturnsRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, onDeleteRedirectRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.delete.routes.DeleteNilMonthlyReturnController
          .onPageLoad()
          .url
      }
    }

    "must redirect to DeleteAmendedMonthlyReturnController" in {
      val mockReturnsRepo       = mock[UnsubmittedMonthlyReturnRepository]
      val mockSessionRepository = mock[SessionRepository]

      val mockDbRecordOpt = Some(
        UnsubmittedMonthlyReturn(
          instanceId = "1",
          monthlyReturnId = monthlyReturnId,
          taxYear = 2026,
          taxMonth = 4,
          returnType = "Standard",
          status = "In Progress",
          amendment = Some("Y"),
          deletable = true,
          lastUpdated = now
        )
      )
      when(mockReturnsRepo.get(eqTo(monthlyReturnId))).thenReturn(Future.successful(mockDbRecordOpt))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UnsubmittedMonthlyReturnRepository].toInstance(mockReturnsRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, onDeleteRedirectRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.delete.routes.DeleteAmendedMonthlyReturnController
          .onPageLoad()
          .url
      }
    }

    "must redirect to DeleteMonthlyReturnController" in {
      val mockReturnsRepo       = mock[UnsubmittedMonthlyReturnRepository]
      val mockSessionRepository = mock[SessionRepository]

      val mockDbRecordOpt = Some(
        UnsubmittedMonthlyReturn(
          instanceId = "1",
          monthlyReturnId = monthlyReturnId,
          taxYear = 2026,
          taxMonth = 4,
          returnType = "Standard",
          status = "In Progress",
          amendment = Some("N"),
          deletable = true,
          lastUpdated = now
        )
      )
      when(mockReturnsRepo.get(eqTo(monthlyReturnId))).thenReturn(Future.successful(mockDbRecordOpt))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UnsubmittedMonthlyReturnRepository].toInstance(mockReturnsRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, onDeleteRedirectRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.delete.routes.DeleteMonthlyReturnController
          .onPageLoad()
          .url
      }
    }

    "must redirect to journey recovery when an unsubmitted return is not deletable" in {
      val mockReturnsRepo       = mock[UnsubmittedMonthlyReturnRepository]
      val mockSessionRepository = mock[SessionRepository]

      val mockDbRecordOpt = Some(
        UnsubmittedMonthlyReturn(
          instanceId = "1",
          monthlyReturnId = monthlyReturnId,
          taxYear = 2026,
          taxMonth = 4,
          returnType = "Standard",
          status = "Failed",
          amendment = Some("N"),
          deletable = false,
          lastUpdated = now
        )
      )
      when(mockReturnsRepo.get(eqTo(monthlyReturnId))).thenReturn(Future.successful(mockDbRecordOpt))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UnsubmittedMonthlyReturnRepository].toInstance(mockReturnsRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, onDeleteRedirectRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }

      verifyNoInteractions(mockSessionRepository)
    }

    "must redirect to journey recovery when an unsubmitted return not found in mongo db" in {
      val mockReturnsRepo       = mock[UnsubmittedMonthlyReturnRepository]
      val mockSessionRepository = mock[SessionRepository]

      when(mockReturnsRepo.get(eqTo(monthlyReturnId))).thenReturn(Future.successful(None))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UnsubmittedMonthlyReturnRepository].toInstance(mockReturnsRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, onDeleteRedirectRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }

      verifyNoInteractions(mockSessionRepository)
    }

    "must redirect to journey recovery when an unable to resolve the route" in {
      val mockReturnsRepo       = mock[UnsubmittedMonthlyReturnRepository]
      val mockSessionRepository = mock[SessionRepository]

      val mockDbRecordOpt = Some(
        UnsubmittedMonthlyReturn(
          instanceId = "1",
          monthlyReturnId = monthlyReturnId,
          taxYear = 2026,
          taxMonth = 4,
          returnType = "Invalid Status",
          status = "Failed",
          amendment = None,
          deletable = true,
          lastUpdated = now
        )
      )
      when(mockReturnsRepo.get(eqTo(monthlyReturnId))).thenReturn(Future.successful(mockDbRecordOpt))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UnsubmittedMonthlyReturnRepository].toInstance(mockReturnsRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, onDeleteRedirectRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }

  }
}
