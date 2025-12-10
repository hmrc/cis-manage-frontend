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

package viewmodels.notices

import base.SpecBase
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class ManageNoticesStatementsPageViewModelSpec extends SpecBase {

  private val app = applicationBuilder().build()
  given msgs: Messages = messages(app)

  "ManageNoticesStatementsPageViewModel" - {

    "should build notice type items with All first and no default selection" in {
      val viewModel = ManageNoticesStatementsPageViewModel(Seq.empty, totalRecords = 0)

      assert(
        viewModel.noticeTypeItems.map(_.value) == Seq(
          Some("all"),
          Some("confirmationStatements"),
          Some("penaltyWarnings"),
          Some("general")
        )
      )

      assert(
        viewModel.noticeTypeItems.map(_.text) == Seq(
          msgs("manageNoticesStatements.noticeType.all"),
          msgs("manageNoticesStatements.noticeType.confirmationStatements"),
          msgs("manageNoticesStatements.noticeType.penaltyWarnings"),
          msgs("manageNoticesStatements.noticeType.general")
        )
      )

      viewModel.noticeTypeItems.foreach(item => assert(item.selected == false))
    }

    "should build read status items with no default selection" in {
      val viewModel = ManageNoticesStatementsPageViewModel(Seq.empty, totalRecords = 0)

      val texts      = viewModel.readStatusItems.map(_.text)
      val values     = viewModel.readStatusItems.map(_.value)
      val selections = viewModel.readStatusItems.map(_.selected)

      assert(texts == Seq(
        msgs("manageNoticesStatements.status.all"),
        msgs("manageNoticesStatements.status.read"),
        msgs("manageNoticesStatements.status.unread")
      ))
      assert(values == Seq(Some("all"), Some("read"), Some("unread")))
      selections.foreach(sel => assert(sel == false))
    }

    "should build date range items with none checked" in {
      val viewModel = ManageNoticesStatementsPageViewModel(Seq.empty, totalRecords = 0)

      val values   = viewModel.dateRangeItems.map(_.value)
      val contents = viewModel.dateRangeItems.map(_.content)
      val checked  = viewModel.dateRangeItems.map(_.checked)

      assert(values == Seq(Some("last7Days"), Some("lastMonth"), Some("dateRange"), Some("all")))
      assert(contents == Seq(
        Text("Last 7 days"),
        Text("Last month"),
        Text("Date range"),
        Text("All")
      ))
      checked.foreach(isChecked => assert(isChecked == false))
    }

    "should include notices and total record count" in {
      val notices = Seq(
        ManageNoticesStatementsRowViewModel("2025-01-01", "confirmationStatements", "desc1", "read", "green", "#1"),
        ManageNoticesStatementsRowViewModel("2025-01-02", "penaltyWarnings", "desc2", "unread", "red", "#2")
      )

      val viewModel = ManageNoticesStatementsPageViewModel(notices, totalRecords = 5)

      assert(viewModel.notices == notices)
      assert(viewModel.totalRecords == 5)
    }
  }
}

