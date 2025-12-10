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
import viewmodels.{ManageNoticesStatementsPageViewModel, ManageNoticesStatementsRowViewModel}
import views.html.ManageNoticesStatementsView

class ManageNoticesStatementsControllerSpec extends SpecBase {

  "ManageNoticesStatements Controller" - {

    "must return OK and the correct view for a GET" in {
      val contractorName: String        = "ABC Construction Ltd"
      lazy val userAnswers: UserAnswers =
        userAnswersWithCisId
          .set(ContractorNamePage, contractorName)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ManageNoticesStatementsController.onPageLoad().url)

        val notices       = Seq(
          ManageNoticesStatementsRowViewModel(
            date = "15 October 2025",
            noticeType = messages(application)("manageNoticesStatements.noticeType.penaltyWarnings"),
            description = messages(application)("manageNoticesStatements.description.penaltyWarning", "5 October 2025"),
            status = messages(application)("manageNoticesStatements.status.unread"),
            statusColour = "govuk-tag--red",
            actionUrl = applicationConfig.noticesAndStatementsUrl
          ),
          ManageNoticesStatementsRowViewModel(
            date = "20 September 2025",
            noticeType = messages(application)("manageNoticesStatements.noticeType.confirmationStatements"),
            description = messages(application)("manageNoticesStatements.description.confirmation", "August 2025"),
            status = messages(application)("manageNoticesStatements.status.read"),
            statusColour = "govuk-tag--blue",
            actionUrl = applicationConfig.noticesAndStatementsUrl
          ),
          ManageNoticesStatementsRowViewModel(
            date = "20 August 2025",
            noticeType = messages(application)("manageNoticesStatements.noticeType.confirmationStatements"),
            description = messages(application)("manageNoticesStatements.description.confirmation", "July 2025"),
            status = messages(application)("manageNoticesStatements.status.read"),
            statusColour = "govuk-tag--blue",
            actionUrl = applicationConfig.noticesAndStatementsUrl
          )
        )
        val pageViewModel = ManageNoticesStatementsPageViewModel(notices)(messages(application))

        val result = route(application, request).value

        val view = application.injector.instanceOf[ManageNoticesStatementsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(contractorName, pageViewModel)(
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
        val request   = FakeRequest(GET, routes.ManageNoticesStatementsController.onPageLoad().url)
        val exception = intercept[IllegalStateException] {
          contentAsString(route(application, request).value)
        }

        exception.getMessage mustEqual "contractorName missing from userAnswers"
      }
    }
  }
}
