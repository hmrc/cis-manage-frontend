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

package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.FakeRequest
import play.twirl.api.Html
import scala.jdk.CollectionConverters.*
import viewmodels.ManageNoticesStatementsRowViewModel
import views.html.components.NoticesTable

class NoticesTableSpec extends SpecBase {

  "NoticesTable" - {

    "must render table headers and rows from view model" in new Setup {
      val notices = Seq(
        ManageNoticesStatementsRowViewModel(
          date = "2025-01-01",
          noticeType = "Confirmation statements",
          description = "Return accepted",
          status = "Read",
          statusColour = "green",
          actionUrl = "/view/1"
        ),
        ManageNoticesStatementsRowViewModel(
          date = "2025-02-01",
          noticeType = "Penalty warnings",
          description = "Late submission",
          status = "Unread",
          statusColour = "red",
          actionUrl = "/view/2"
        )
      )

      val html: Html    = view(notices)
      val doc: Document = Jsoup.parse(html.body)

      val headers = doc.select("thead th").eachText().asScala.toSeq
      assert(
        headers == Seq(
        messages("manageNoticesStatements.table.date"),
        messages("manageNoticesStatements.table.type"),
        messages("manageNoticesStatements.table.description"),
        messages("manageNoticesStatements.table.status"),
        messages("manageNoticesStatements.table.action")
        )
      )

      val rows = doc.select("tbody tr")
      assert(rows.size() == 2)

      val firstRowCells = rows.get(0).select("th, td")
      assert(firstRowCells.get(0).text() == "2025-01-01")
      assert(firstRowCells.get(1).text() == "Confirmation statements")
      assert(firstRowCells.get(2).text() == "Return accepted")
      assert(firstRowCells.get(3).select("strong").text() == "Read")
      assert(firstRowCells.get(3).select("strong").hasClass("green"))
      assert(firstRowCells.get(4).select("a").text() == messages("manageNoticesStatements.table.action.view"))
      assert(firstRowCells.get(4).select("a").attr("href") == "/view/1")

      val secondRowCells = rows.get(1).select("th, td")
      assert(secondRowCells.get(0).text() == "2025-02-01")
      assert(secondRowCells.get(1).text() == "Penalty warnings")
      assert(secondRowCells.get(2).text() == "Late submission")
      assert(secondRowCells.get(3).select("strong").text() == "Unread")
      assert(secondRowCells.get(3).select("strong").hasClass("red"))
      assert(secondRowCells.get(4).select("a").text() == messages("manageNoticesStatements.table.action.view"))
      assert(secondRowCells.get(4).select("a").attr("href") == "/view/2")
    }
  }

  trait Setup {
    val app       = applicationBuilder().build()
    val view      = app.injector.instanceOf[NoticesTable]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: play.api.i18n.Messages = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}

