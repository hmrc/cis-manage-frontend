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

package viewmodels

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

case class ManageNoticesStatementsRowViewModel(
  date: String,
  noticeType: String,
  description: String,
  status: String,
  statusColour: String,
  actionUrl: String
)

case class ManageNoticesStatementsPageViewModel(
  noticeTypeItems: Seq[SelectItem],
  readStatusItems: Seq[SelectItem],
  dateRangeItems: Seq[RadioItem],
  notices: Seq[ManageNoticesStatementsRowViewModel],
  totalRecords: Int
)

object ManageNoticesStatementsPageViewModel {

  def apply(
    notices: Seq[ManageNoticesStatementsRowViewModel]
  )(implicit messages: Messages): ManageNoticesStatementsPageViewModel =
    apply(notices, notices.size)

  def apply(
    notices: Seq[ManageNoticesStatementsRowViewModel],
    totalRecords: Int
  )(implicit messages: Messages): ManageNoticesStatementsPageViewModel =
    ManageNoticesStatementsPageViewModel(
      noticeTypeItems = Seq(
        SelectItem(
          value = Some("all"),
          text = messages("manageNoticesStatements.noticeType.all")
        ),
        SelectItem(
          value = Some("confirmationStatements"),
          text = messages("manageNoticesStatements.noticeType.confirmationStatements")
        ),
        SelectItem(
          value = Some("penaltyWarnings"),
          text = messages("manageNoticesStatements.noticeType.penaltyWarnings")
        ),
        SelectItem(
          value = Some("general"),
          text = messages("manageNoticesStatements.noticeType.general")
        )
      ),
      readStatusItems = Seq(
        SelectItem(
          value = Some("all"),
          text = messages("manageNoticesStatements.status.all")
        ),
        SelectItem(
          value = Some("read"),
          text = messages("manageNoticesStatements.status.read")
        ),
        SelectItem(
          value = Some("unread"),
          text = messages("manageNoticesStatements.status.unread")
        )
      ),
      dateRangeItems = Seq(
        RadioItem(
          value = Some("last7Days"),
          content = Text(messages("manageNoticesStatements.dateFilter.last7Days"))
        ),
        RadioItem(
          value = Some("lastMonth"),
          content = Text(messages("manageNoticesStatements.dateFilter.lastMonth"))
        ),
        RadioItem(
          value = Some("dateRange"),
          content = Text(messages("manageNoticesStatements.dateFilter.dateRange"))
        ),
        RadioItem(
          value = Some("all"),
          content = Text(messages("manageNoticesStatements.dateFilter.all"))
        )
      ),
      notices = notices,
      totalRecords = totalRecords
    )
}
