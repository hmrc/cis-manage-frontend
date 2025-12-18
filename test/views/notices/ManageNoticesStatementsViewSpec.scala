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

package views.notices

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import scala.jdk.CollectionConverters.*
import viewmodels.notices.{ManageNoticesStatementsPageViewModel, ManageNoticesStatementsRowViewModel}
import views.html.notices.ManageNoticesStatementsView

class ManageNoticesStatementsViewSpec extends SpecBase {

  "ManageNoticesStatementsView" - {

    "must render headings, details content and intro" in new Setup {
      val html: HtmlFormat.Appendable = view(contractorName, instanceId, pageViewModel)
      val doc: Document               = Jsoup.parse(html.body)

      doc.title                                                   must include(messages("manageNoticesStatements.title"))
      doc.select("h1").text                                       must include(messages("manageNoticesStatements.heading"))
      doc.getElementsByClass("govuk-caption-l").text              must include(contractorName)
      doc.select("p").text                                        must include(messages("manageNoticesStatements.intro"))
      doc.select("summary span.govuk-details__summary-text").text must include(
        messages("manageNoticesStatements.link.understandingNotices")
      )
      val detailHeadings = doc.select("details h3").eachText().asScala.toSeq
      detailHeadings must contain(messages("manageNoticesStatements.details.confirmationStatements.heading"))
      detailHeadings must contain(messages("manageNoticesStatements.details.penaltyWarnings.heading"))
      detailHeadings must contain(messages("manageNoticesStatements.details.generalNotices.heading"))
      detailHeadings must contain(messages("manageNoticesStatements.details.howLong.heading"))
      val detailParagraphs = doc.select("details p").text
      detailParagraphs must include(messages("manageNoticesStatements.details.confirmationStatements.body"))
      detailParagraphs must include(messages("manageNoticesStatements.details.penaltyWarnings.body"))
      detailParagraphs must include(messages("manageNoticesStatements.details.generalNotices.body"))
      detailParagraphs must include(messages("manageNoticesStatements.details.howLong.body"))
    }

    "must render search filters with provided options and button" in new Setup {
      val html: HtmlFormat.Appendable = view(contractorName, instanceId, pageViewModel)
      val doc: Document               = Jsoup.parse(html.body)

      doc.select("h2").eachText().asScala must contain(messages("manageNoticesStatements.search.heading"))
      doc.select("p").text                must include(messages("manageNoticesStatements.search.description"))

      val noticeTypeOptions = doc.select("#noticeType option").eachText().asScala.toSeq
      noticeTypeOptions mustEqual pageViewModel.noticeTypeItems.map(_.text)
      doc.select("#noticeType option").eachAttr("value").asScala.toSeq mustEqual
        pageViewModel.noticeTypeItems.flatMap(_.value)

      val statusOptions = doc.select("#noticeStatus option").eachText().asScala.toSeq
      statusOptions mustEqual pageViewModel.readStatusItems.map(_.text)
      doc.select("#noticeStatus option").eachAttr("value").asScala.toSeq mustEqual
        pageViewModel.readStatusItems.flatMap(_.value)

      doc.select("fieldset legend").isEmpty mustBe true
      doc.select("input[name=dateRange]").eachAttr("value").asScala.toSeq mustEqual
        pageViewModel.dateRangeItems.flatMap(_.value)
      doc.select("label.govuk-radios__label").eachText().asScala.toSeq mustEqual
        Seq(
          messages("manageNoticesStatements.dateFilter.last7Days"),
          messages("manageNoticesStatements.dateFilter.lastMonth"),
          messages("manageNoticesStatements.dateFilter.dateRange"),
          messages("manageNoticesStatements.dateFilter.all")
        )

      doc.select("button.govuk-button").text must include(messages("manageNoticesStatements.search.button"))
    }

    "must show search results summary, table rows and view all link" in new Setup {
      val html: HtmlFormat.Appendable = view(contractorName, instanceId, pageViewModel)
      val doc: Document               = Jsoup.parse(html.body)

      doc.select("h2").eachText().asScala must contain(messages("manageNoticesStatements.searchResults.heading"))
      doc.select("p").text                must include(
        messages(
          "manageNoticesStatements.searchResults.summary",
          pageViewModel.notices.size,
          pageViewModel.totalRecords
        )
      )
      doc.select("h2").eachText().asScala must contain(messages("manageNoticesStatements.recentNotices.heading"))

      val rows          = doc.select("table tbody tr")
      rows.size() mustBe pageViewModel.notices.size
      val firstRowCells = rows.get(0).select("th, td")
      firstRowCells.get(0).text() mustBe "2025-01-01"
      firstRowCells.get(1).text() mustBe "Confirmation statements"
      firstRowCells.get(2).text() mustBe "Return accepted"
      firstRowCells.get(3).select("strong").text() mustBe "Read"
      firstRowCells.get(4).select("a").attr("href") mustBe "/view/1"

      val viewAllLink = doc
        .select("a.govuk-link")
        .stream()
        .iterator()
        .asScala
        .find(_.text() == messages("manageNoticesStatements.searchResults.viewAll"))
      viewAllLink.map(_.attr("href")) mustBe Some(applicationConfig.noticesAndStatementsUrl)
    }
  }

  trait Setup {
    val app: Application                                    = applicationBuilder().build()
    val view: ManageNoticesStatementsView                   = app.injector.instanceOf[ManageNoticesStatementsView]
    implicit val request: play.api.mvc.Request[_]           = FakeRequest()
    implicit val messages: Messages                         = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
    val contractorName                                      = "ABC Construction Ltd"
    val instanceId                                          = "123"
    val pageViewModel: ManageNoticesStatementsPageViewModel = ManageNoticesStatementsPageViewModel(
      Seq(
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
      ),
      totalRecords = 8
    )
  }
}
