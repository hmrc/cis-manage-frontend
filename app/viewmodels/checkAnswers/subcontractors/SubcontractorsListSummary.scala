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

import models.{CheckMode, UserAnswers}
import pages.subcontractors.SubcontractorsListPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object SubcontractorsListSummary {

  def row(instanceId: String, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SubcontractorsListPage).map { answer =>
      SummaryListRowViewModel(
        key = "SubcontractorsList.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(answer).toString),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.subcontractors.routes.SubcontractorsListController.onPageLoad(instanceId, CheckMode, 1).url
          )
            .withVisuallyHiddenText(messages("subcontractorsList.change.hidden"))
            .withAttribute("id", "your-subcontractors-list")
        )
      )
    }
}
