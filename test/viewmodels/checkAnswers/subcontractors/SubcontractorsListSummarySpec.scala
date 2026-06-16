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

package viewmodels.checkAnswers.subcontractors

import base.SpecBase
import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues._
import org.scalatest.matchers.must.Matchers
import pages.subcontractors.SubcontractorsListPage
import play.api.i18n.{Lang, Messages, MessagesImpl}
import play.api.test.Helpers.stubMessagesApi

class SubcontractorsListSummarySpec extends SpecBase with Matchers {

  private val messagesApi                 = stubMessagesApi()
  private implicit val messages: Messages = MessagesImpl(Lang.defaultLang, messagesApi)

  "SubcontractorsListSummary.row" - {

    "must return a summary row when subcontractors list answer exists" in {

      val instanceId = "test-instance-id"

      val answers: UserAnswers =
        emptyUserAnswers
          .set(SubcontractorsListPage, "Test subcontractor value")
          .success
          .value

      val result = SubcontractorsListSummary.row(instanceId, answers)

      result mustBe defined

      val row = result.value

      row.key.content.asHtml.toString must include(
        messages("SubcontractorsList.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString must include("Test subcontractor value")

      row.actions mustBe defined

      val action = row.actions.value.items.head

      action.href mustBe controllers.subcontractors.routes.SubcontractorsListController
        .onPageLoad(instanceId, CheckMode, 1)
        .url

      action.content.asHtml.toString must include(messages("site.change"))

      action.visuallyHiddenText mustBe Some(
        messages("subcontractorsList.change.hidden")
      )

      action.attributes must contain(
        "id" -> "your-subcontractors-list"
      )
    }

    "must return None when subcontractors list answer does not exist" in {

      val instanceId = "test-instance-id"

      SubcontractorsListSummary.row(instanceId, emptyUserAnswers) mustBe None
    }
  }
}
