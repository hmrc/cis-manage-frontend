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
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import pages.CisIdPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ManageService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.{ActionLinkViewModel, IncompleteReturnsRowViewModel}
import views.html.IncompleteReturnsView

import scala.concurrent.Future

class IncompleteReturnsControllerSpec extends SpecBase with MockitoSugar {

  "IncompleteReturnsController" - {

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

      when(mockService.getUnsubmittedMonthlyReturns(any[String])(any[HeaderCarrier]))
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

        verify(mockService).getUnsubmittedMonthlyReturns(any[String])(any[HeaderCarrier])
      }
    }

    "must redirect to JourneyRecovery when CisIdPage is missing" in {
      val mockService = mock[ManageService]

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ManageService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.IncompleteReturnsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verifyNoInteractions(mockService)
      }
    }
  }
}
