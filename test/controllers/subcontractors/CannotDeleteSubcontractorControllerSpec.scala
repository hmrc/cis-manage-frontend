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
import models.NormalMode
import models.subcontractors.DeleteSubcontractorJourneyData
import pages.CisIdPage
import pages.subcontractors.DeleteSubcontractorJourneyPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.subcontractors.CannotDeleteSubcontractorView

class CannotDeleteSubcontractorControllerSpec extends SpecBase {

  private val subcontractorName = "Gamma Builders"

  "CannotDeleteSubcontractorController" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdPage, "1")
          .success
          .value
          .set(
            DeleteSubcontractorJourneyPage,
            DeleteSubcontractorJourneyData(
              subcontractorName = subcontractorName,
              subbieResourceRef = 10L,
              subcontractorCanBeDeleted = false
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.subcontractors.routes.CannotDeleteSubcontractorController
              .onPageLoad()
              .url
          )

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[CannotDeleteSubcontractorView]

        val subcontractorsPageUrl =
          controllers.subcontractors.routes.SubcontractorsListController
            .onPageLoad("1", NormalMode)
            .url

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            subcontractorName,
            subcontractorsPageUrl
          )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery when journey data is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdPage, "1")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.subcontractors.routes.CannotDeleteSubcontractorController
              .onPageLoad()
              .url
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when subcontractor can be deleted" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdPage, "1")
          .success
          .value
          .set(
            DeleteSubcontractorJourneyPage,
            DeleteSubcontractorJourneyData(
              subcontractorName = subcontractorName,
              subbieResourceRef = 10L,
              subcontractorCanBeDeleted = true
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.subcontractors.routes.CannotDeleteSubcontractorController
              .onPageLoad()
              .url
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
