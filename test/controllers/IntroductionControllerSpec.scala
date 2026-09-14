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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.IntroductionView

class IntroductionControllerSpec extends UnitSpec {
  private val stubView    = mock[IntroductionView]
  private val stubContent = "IntroductionView"
  when(stubView.apply()(any, any)) thenReturn play.twirl.api.Html(stubContent)

  private val controllerUnderTest = new IntroductionController(mockCisControllerComponents, mockSessionRepo, stubView)

  "IntroductionController.onPageLoad" - {
    "must return OK and the correct view for a GET" in {
      val result = controllerUnderTest.onPageLoad(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual stubContent
    }
  }

  "IntroductionController.affinityGroupRouting" - {
    "for a contractor" - {
      "must redirect to the contractor landing page" in {
        mockCisControllerComponents.loginAsOrg()
        val result = controllerUnderTest.affinityGroupRouting(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual contractor.routes.ContractorLandingController.onPageLoad().url
      }
    }

    "for an agent" - {
      "must redirect to the agent landing page" in {
        mockCisControllerComponents.loginAsAgent()
        val result = controllerUnderTest.affinityGroupRouting(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual agent.routes.RetrievingClientController.onPageLoad().url
      }
    }
  }
}
