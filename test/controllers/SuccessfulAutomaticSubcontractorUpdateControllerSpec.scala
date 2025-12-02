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

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.SuccessfulAutomaticSubcontractorUpdateViewModel
import views.html.SuccessfulAutomaticSubcontractorUpdateView

class SuccessfulAutomaticSubcontractorUpdateControllerSpec extends SpecBase {

  "SuccessfulAutomaticSubcontractorUpdate Controller" - {
    "must return OK and the correct view for a GET" in {
      val subcontractorsList: Seq[SuccessfulAutomaticSubcontractorUpdateViewModel] = Seq(
        SuccessfulAutomaticSubcontractorUpdateViewModel("Alice, A", "1111111111", " ", "01 Jan 2014"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Bob, B", "2222222222", " ", "01 Jan 2014"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Dave, D", "4444444444", "V1000000009", "07 May 2015"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Charles, C", "3333333333", "V1000000009", "01 Jan 2014"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Elise, E", "5555555555", "V1000000009", "07 May 2015"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Frank, F", "6666666666", "V1000000009", "07 Jan 2018")
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.SuccessfulAutomaticSubcontractorUpdateController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SuccessfulAutomaticSubcontractorUpdateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(subcontractorsList)(request, messages(application)).toString
      }
    }
  }
}
