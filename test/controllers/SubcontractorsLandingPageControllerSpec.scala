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
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.SubcontractorsLandingPageView

class SubcontractorsLandingPageControllerSpec extends UnitSpec {
  import org.mockito.ArgumentMatchers.any
  import org.mockito.Mockito.when
  import play.twirl.api.Html

  private val stubView    = mock[SubcontractorsLandingPageView]
  private val stubContent = "Subcontractors Landing Page"
  when(stubView.apply()(any, any)) thenReturn Html(stubContent)

  private val controllerUnderTest = new SubcontractorsLandingPageController(mockControllerComponents, stubView)

  "SubcontractorsLandingPageController" - {

    "must return OK and the correct view for a GET" in {
      mockControllerComponents.setUserAnswers(Some(emptyUserAnswers))
      val result = controllerUnderTest.onPageLoad(cisId)(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustEqual stubContent
    }
  }
}
