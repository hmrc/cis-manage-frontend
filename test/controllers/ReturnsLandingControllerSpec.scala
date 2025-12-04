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
import models.UserAnswers
import pages.{CisIdPage, ContractorNamePage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.ReturnLandingViewModel
import views.html.ReturnsLandingView

class ReturnsLandingControllerSpec extends SpecBase {

  "ReturnsLanding Controller" - {

    "must return OK and the correct view for a GET" in {
      val returnsList = Seq(
        ReturnLandingViewModel("August 2025", "Standard", "19 September 2025", "Accepted"),
        ReturnLandingViewModel("July 2025", "Nil", "19 August 2025", "Accepted"),
        ReturnLandingViewModel("June 2025", "Standard", "18 July 2025", "Accepted")
      )

      val contractorName: String        = "ABC Construction Ltd"
      lazy val userAnswers: UserAnswers =
        userAnswersWithCisId
          .set(ContractorNamePage, contractorName)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsLandingController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[ReturnsLandingView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(contractorName, returnsList)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must throw IllegalStateException when ContractorNamePage is missing" in {
      lazy val userAnswers: UserAnswers =
        userAnswersWithCisId
          .set(CisIdPage, "some value")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request   = FakeRequest(GET, routes.ReturnsLandingController.onPageLoad().url)
        val exception = intercept[IllegalStateException] {
          contentAsString(route(application, request).value)
        }

        exception.getMessage mustEqual "contractorName missing from userAnswers"
      }
    }
  }
}
