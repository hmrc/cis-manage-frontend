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

package controllers.history

import base.SpecBase
import models.{Deletable, NotDeletable, UnsubmittedMonthlyReturnsRow, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.CisIdPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.ManageService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.{ActionLinkViewModel, IncompleteReturnsRowViewModel}
import views.html.IncompleteReturnsView

import scala.concurrent.Future

class IncompleteReturnsControllerSpec extends SpecBase with MockitoSugar {

  "IncompleteReturnsController.onPageLoad" - {

    "must return OK and the correct view for a GET when CisIdPage is present" in {
      val mockService = mock[ManageService]

      val rows = Seq(
        IncompleteReturnsRowViewModel(
          returnPeriodEnd = "5 April 2025",
          returnType = "Standard",
          lastUpdate = "20 April 2026",
          status = "In progress",
          action = Seq(
            ActionLinkViewModel(
              textKey = "incompleteReturns.action.continue",
              href = "/some-url"
            )
          ),
          amendment = Some("N")
        )
      )

      when(mockService.getUnsubmittedMonthlyReturnRows(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(rows))

      val userAnswers = emptyUserAnswers.set(CisIdPage, "1234567890").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[ManageService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.IncompleteReturnsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IncompleteReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(rows)(request, messages(application)).toString

        verify(mockService).getUnsubmittedMonthlyReturnRows(any[String])(any[HeaderCarrier])
      }
    }

    "must redirect to NoIncompleteReturnsController when there are no incomplete returns" in {
      val mockService = mock[ManageService]

      when(mockService.getUnsubmittedMonthlyReturnRows(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Seq.empty))

      val userAnswers = emptyUserAnswers.set(CisIdPage, "123").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[ManageService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.IncompleteReturnsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.amend.routes.NoIncompleteReturnsController.onPageLoad().url

        verify(mockService).getUnsubmittedMonthlyReturnRows(any[String])(any[HeaderCarrier])
      }
    }
  }

  "IncompleteReturnsController.onDeleteRedirect" - {

    val monthlyReturnId = 3000L

    lazy val onDeleteRedirectRoute: String =
      routes.IncompleteReturnsController.onDeleteRedirect(monthlyReturnId).url

    "must redirect to DeleteAmendedNilMonthlyReturnController" in {
      val mockManageService     = mock[ManageService]
      val mockSessionRepository = mock[SessionRepository]

      val mockDeletableResult = UnsubmittedMonthlyReturnsRow(
        monthlyReturnId = monthlyReturnId,
        taxYear = 2026,
        taxMonth = 4,
        returnType = "Nil",
        status = "In Progress",
        lastUpdate = None,
        amendment = Some("Y"),
        deletable = true
      )

      when(
        mockManageService.checkUnsubmittedMonthlyReturnDeletion(any[UserAnswers], any[Long])(
          any[HeaderCarrier]
        )
      ).thenReturn(Future.successful(Deletable(mockDeletableResult)))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageService].toInstance(mockManageService)
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
      val mockManageService     = mock[ManageService]
      val mockSessionRepository = mock[SessionRepository]

      val mockDeletableResult = UnsubmittedMonthlyReturnsRow(
        monthlyReturnId = monthlyReturnId,
        taxYear = 2026,
        taxMonth = 4,
        returnType = "Nil",
        status = "In Progress",
        lastUpdate = None,
        amendment = Some("N"),
        deletable = true
      )

      when(
        mockManageService.checkUnsubmittedMonthlyReturnDeletion(any[UserAnswers], any[Long])(
          any[HeaderCarrier]
        )
      ).thenReturn(Future.successful(Deletable(mockDeletableResult)))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageService].toInstance(mockManageService)
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
      val mockManageService     = mock[ManageService]
      val mockSessionRepository = mock[SessionRepository]

      val mockDeletableResult = UnsubmittedMonthlyReturnsRow(
        monthlyReturnId = monthlyReturnId,
        taxYear = 2026,
        taxMonth = 4,
        returnType = "Standard",
        status = "In Progress",
        lastUpdate = None,
        amendment = Some("Y"),
        deletable = true
      )

      when(
        mockManageService.checkUnsubmittedMonthlyReturnDeletion(any[UserAnswers], any[Long])(
          any[HeaderCarrier]
        )
      ).thenReturn(Future.successful(Deletable(mockDeletableResult)))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageService].toInstance(mockManageService)
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
      val mockManageService     = mock[ManageService]
      val mockSessionRepository = mock[SessionRepository]

      val mockDeletableResult = UnsubmittedMonthlyReturnsRow(
        monthlyReturnId = monthlyReturnId,
        taxYear = 2026,
        taxMonth = 4,
        returnType = "Standard",
        status = "In Progress",
        lastUpdate = None,
        amendment = Some("N"),
        deletable = true
      )

      when(
        mockManageService.checkUnsubmittedMonthlyReturnDeletion(any[UserAnswers], any[Long])(
          any[HeaderCarrier]
        )
      ).thenReturn(Future.successful(Deletable(mockDeletableResult)))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageService].toInstance(mockManageService)
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
      val mockManageService     = mock[ManageService]
      val mockSessionRepository = mock[SessionRepository]

      when(
        mockManageService.checkUnsubmittedMonthlyReturnDeletion(any[UserAnswers], any[Long])(
          any[HeaderCarrier]
        )
      ).thenReturn(Future.successful(NotDeletable))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageService].toInstance(mockManageService)
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

    "must redirect to journey recovery when unable to resolve the route" in {
      val mockManageService     = mock[ManageService]
      val mockSessionRepository = mock[SessionRepository]

      val mockDeletableResult = UnsubmittedMonthlyReturnsRow(
        monthlyReturnId = monthlyReturnId,
        taxYear = 2026,
        taxMonth = 4,
        returnType = "Invalid Type",
        status = "In Progress",
        lastUpdate = None,
        amendment = Some("N"),
        deletable = true
      )

      when(
        mockManageService.checkUnsubmittedMonthlyReturnDeletion(any[UserAnswers], any[Long])(
          any[HeaderCarrier]
        )
      ).thenReturn(Future.successful(Deletable(mockDeletableResult)))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageService].toInstance(mockManageService)
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
