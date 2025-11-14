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

package controllers.contractor

import base.SpecBase
import config.FrontendAppConfig
import controllers.contractor.ContractorLandingController.viewModel
import models.UserAnswers
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import services.ManageService
import uk.gov.hmrc.http.UpstreamErrorResponse
import views.html.contractor.ContractorLandingView

import scala.concurrent.Future

class ContractorLandingControllerSpec extends SpecBase {

  lazy val contractorLandingRoute: String =
    controllers.contractor.routes.ContractorLandingController.onPageLoad().url

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, contractorLandingRoute)

  "ContractorLandingController" - {

    "must return OK and the correct view for a GET" in {
      val mockManageService = mock[ManageService]
      when(mockManageService.resolveAndStoreCisId(any[UserAnswers])(any()))
        .thenReturn(Future.successful(("CIS-123", emptyUserAnswers)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

      running(application) {
        implicit val appConfig: FrontendAppConfig =
          application.injector.instanceOf[FrontendAppConfig]

        val request  = FakeRequest(GET, routes.ContractorLandingController.onPageLoad().url)
        val result   = route(application, request).value
        val view     = application.injector.instanceOf[ContractorLandingView]
        val expected = view(viewModel(appConfig))(request, messages(application))

        status(result)          shouldBe OK
        contentType(result)       should contain(HTML)
        contentAsString(result) shouldBe expected.toString
      }
    }

    "must redirect to Journey Recovery when service return NOT_FOUND" in {
      val mockManageService = mock[ManageService]
      when(mockManageService.resolveAndStoreCisId(any[UserAnswers])(any()))
        .thenReturn(Future.failed(UpstreamErrorResponse("not found", NOT_FOUND, NOT_FOUND)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

      running(application) {
        val result = route(application, getRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to system error page if unable to retrieve cisId" in {
      val mockManageService = mock[ManageService]
      when(mockManageService.resolveAndStoreCisId(any[UserAnswers])(any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

      running(application) {
        val result = route(application, getRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

  }
}
