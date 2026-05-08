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

package viewmodels.verify

import base.SpecBase
import models.CheckMode
import models.verify.VerificationTaxYearSelection
import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear}
import org.scalatest.matchers.must.Matchers
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import org.scalatest.freespec.AnyFreeSpec
import viewmodels.checkAnswers.VerificationHistorySelectTaxYearSummary
import pages.verify.VerificationHistorySelectTaxYearPage
import play.api.i18n.Messages

class VerificationHistorySelectTaxYearSummarySpec extends SpecBase with Matchers {

  val fakeRequest = FakeRequest()

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  "VerificationHistorySelectTaxYearSummary" - {

    "must return None when no answer is present" in new Setup {

      val answers = emptyUserAnswers

      val result = VerificationHistorySelectTaxYearSummary.row(answers)

      result mustBe None
    }

    "must return a row when AllTaxYears is selected" in new Setup {

      val answers = emptyUserAnswers
        .set(VerificationHistorySelectTaxYearPage, AllTaxYears)
        .success
        .value

      val result = VerificationHistorySelectTaxYearSummary.row(answers)

      result mustBe defined

      val row = result.value

      row.key.content.asHtml.toString must include(
        messages("verificationHistorySelectTaxYear.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString must include(
        messages("verificationHistorySelectTaxYear.all")
      )

      row.actions.get.items.head.href mustBe
        controllers.verify.routes.VerificationHistorySelectTaxYearController
          .onPageLoad(CheckMode)
          .url
    }

    "must return a row when a specific TaxYear is selected" in new Setup {

      val taxYear = "2023 to 2024"

      val answers = emptyUserAnswers
        .set(VerificationHistorySelectTaxYearPage, TaxYear(taxYear))
        .success
        .value

      val result = VerificationHistorySelectTaxYearSummary.row(answers)

      result mustBe defined

      val row = result.value

      row.value.content.asHtml.toString must include(taxYear)
    }
  }

  trait Setup {
    implicit val messages: Messages = messagesApi.preferred(fakeRequest)
  }
}
