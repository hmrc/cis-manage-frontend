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

package viewmodels.history

import base.SpecBase
import models.CheckMode
import org.scalatest.OptionValues
import pages.history.SubmittedReturnsChooseTaxYearPage
import play.api.i18n.Messages
import play.api.test.Helpers
import viewmodels.checkAnswers.history.SubmittedReturnsChooseTaxYearSummary

class SubmittedReturnsChooseTaxYearSummarySpec extends SpecBase with OptionValues {

  private implicit val messages: Messages = Helpers.stubMessages()

  "SubmittedReturnsChooseTaxYearSummary" - {

    "when answer is present" - {

      "must return a SummaryListRow for a specific tax year" in {
        val answers = emptyUserAnswers
          .set(SubmittedReturnsChooseTaxYearPage, "2022 to 2023")
          .success
          .value

        val result = SubmittedReturnsChooseTaxYearSummary.row(answers).value

        result.key.content.asHtml.toString must include(
          messages("history.submittedReturnsChooseTaxYear.checkYourAnswersLabel")
        )

        result.value.content.asHtml.toString must include(
          messages("history.submittedReturnsChooseTaxYear.2022 to 2023")
        )

        result.actions.value.items.head.href mustBe
          controllers.history.routes.SubmittedReturnsChooseTaxYearController
            .onPageLoad(CheckMode)
            .url

        result.actions.value.items.head.visuallyHiddenText.value mustBe
          messages("history.submittedReturnsChooseTaxYear.change.hidden")
      }

      "must return a SummaryListRow for 'all'" in {
        val answers = emptyUserAnswers
          .set(SubmittedReturnsChooseTaxYearPage, "all")
          .success
          .value

        val result = SubmittedReturnsChooseTaxYearSummary.row(answers).value

        result.key.content.asHtml.toString must include(
          messages("history.submittedReturnsChooseTaxYear.checkYourAnswersLabel")
        )

        result.value.content.asHtml.toString must include(
          messages("history.submittedReturnsChooseTaxYear.all")
        )

        result.actions.value.items.head.href mustBe
          controllers.history.routes.SubmittedReturnsChooseTaxYearController
            .onPageLoad(CheckMode)
            .url

        result.actions.value.items.head.visuallyHiddenText.value mustBe
          messages("history.submittedReturnsChooseTaxYear.change.hidden")
      }
    }

    "when answer is not present" - {

      "must return None" in {
        val answers = emptyUserAnswers

        val result = SubmittedReturnsChooseTaxYearSummary.row(answers)

        result mustBe None
      }
    }
  }
}
