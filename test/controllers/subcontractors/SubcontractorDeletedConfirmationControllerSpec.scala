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
import models.NormalMode
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.subcontractors.SubcontractorDeletedConfirmationView
import models.subcontractors.DeleteSubcontractorJourneyData
import pages.CisIdPage
import pages.subcontractors.DeleteSubcontractorJourneyPage

class SubcontractorDeletedConfirmationControllerSpec extends SpecBase {

  private val cisId = "cis-123"

  private val userAnswers =
    emptyUserAnswers
      .set(CisIdPage, cisId)
      .success
      .value
      .set(
        DeleteSubcontractorJourneyPage,
        DeleteSubcontractorJourneyData(
          subcontractorName = "ABC Contractors",
          subbieResourceRef = 10L,
          subcontractorCanBeDeleted = true
        )
      )
      .success
      .value

  "SubcontractorDeletedConfirmationController" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.subcontractors.routes.SubcontractorDeletedConfirmationController
              .onPageLoad()
              .url
          )

        val result =
          route(application, request).value

        val view =
          application.injector
            .instanceOf[SubcontractorDeletedConfirmationView]

        val expectedSubcontractorName = "ABC Contractors"

        val expectedUrl =
          controllers.subcontractors.routes.SubcontractorsListController
            .onPageLoad(cisId, NormalMode)
            .url

        val expectedSurveyUrl = "#"

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            expectedSubcontractorName,
            expectedUrl,
            expectedSurveyUrl
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to journey recovery when journey data is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdPage, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.subcontractors.routes.SubcontractorDeletedConfirmationController
              .onPageLoad()
              .url
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }
  }
}
