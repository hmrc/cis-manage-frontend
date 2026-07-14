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

package controllers.subcontractors

import base.SpecBase
import controllers.routes
import models.response.GetSubcontractorForDeleteResponse
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.CisIdPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SubcontractorService

import scala.concurrent.Future

class GetSubcontractorForDeleteControllerSpec extends SpecBase with MockitoSugar {

  val subbieResourceRef = 10L
  val cisId             = "123"

  val okResponse =
    GetSubcontractorForDeleteResponse(
      subcontractorName = "Gamma Builders",
      subcontractorCanBeDeleted = true
    )

  val cannotDeleteResponse =
    GetSubcontractorForDeleteResponse(
      subcontractorName = "Gamma Builders",
      subcontractorCanBeDeleted = false
    )

  lazy val routeUrl: String =
    controllers.subcontractors.routes.GetSubcontractorForDeleteController
      .onPageLoad(subbieResourceRef)
      .url

  "GetSubcontractorForDeleteController" - {

    "must redirect to DeleteSubcontractorYesNoController when subcontractor can be deleted" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdPage, cisId)
          .success
          .value

      val mockService = mock[SubcontractorService]

      when(
        mockService.getSubcontractorDeleteStatus(
          eqTo(cisId),
          eqTo(subbieResourceRef)
        )(any())
      ).thenReturn(
        Future.successful(okResponse)
      )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubcontractorService].toInstance(mockService)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(GET, routeUrl)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.subcontractors.routes.DeleteSubcontractorYesNoController
            .onPageLoad()
            .url

        verify(mockService)
          .getSubcontractorDeleteStatus(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any())
      }
    }

    "must redirect to CannotDeleteSubcontractorController when subcontractor cannot be deleted" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdPage, cisId)
          .success
          .value

      val mockService = mock[SubcontractorService]

      when(
        mockService.getSubcontractorDeleteStatus(
          eqTo(cisId),
          eqTo(subbieResourceRef)
        )(any())
      ).thenReturn(
        Future.successful(cannotDeleteResponse)
      )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubcontractorService].toInstance(mockService)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(GET, routeUrl)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.subcontractors.routes.CannotDeleteSubcontractorController
            .onPageLoad()
            .url

        verify(mockService)
          .getSubcontractorDeleteStatus(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any())
      }
    }

    "must redirect to JourneyRecovery when CisId is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      running(application) {

        val request =
          FakeRequest(GET, routeUrl)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when service fails" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdPage, cisId)
          .success
          .value

      val mockService = mock[SubcontractorService]

      when(
        mockService.getSubcontractorDeleteStatus(
          eqTo(cisId),
          eqTo(subbieResourceRef)
        )(any())
      ).thenReturn(
        Future.failed(new RuntimeException("boom"))
      )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubcontractorService].toInstance(mockService)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(GET, routeUrl)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService)
          .getSubcontractorDeleteStatus(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any())
      }
    }
  }
}
