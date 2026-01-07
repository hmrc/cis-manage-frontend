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

package views.agent

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

import base.SpecBase
import forms.ClientListSearchFormProvider
import models.agent.ClientListFormData
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import scala.jdk.CollectionConverters._
import viewmodels.agent.ClientStatus.Active
import viewmodels.agent.{ClientListViewModel, SearchByList}
import viewmodels.govuk.PaginationFluency._
import views.ViewSpecGetters
import views.html.agent.ClientListSearchView

class ClientListSearchViewSpec extends SpecBase with Matchers with ViewSpecGetters {

  "ClientListSearchView" - {

    "must render the page with heading, paragraph, input and button" in new Setup {
      val html: HtmlFormat.Appendable = view(
        form = form,
        searchByOptions = searchOptions,
        clientList = clientList,
        paginationViewModel = PaginationViewModel(),
        sortBy = None,
        sortOrder = None
      )
      val doc: Document               = Jsoup.parse(html.body)
      doc.title                must include(messages("agent.clientListSearch.title"))
      doc.select("h1").text mustBe messages("agent.clientListSearch.heading")
      doc.select("label").text must include(messages("agent.clientListSearch.searchBy.label"))

      def validateSelectValues(document: Document, searchByOptions: Seq[SearchByList], numberOfSets: Int = 1): Unit = {
        val elements: List[Element] = getElementsBySelector(document, "option")
        elements.size                     shouldBe searchByOptions.size + 1
        elements.map(_.attr("value")).toSet should contain allElementsOf searchByOptions.map(_.value)
      }

      validateSelectValues(doc, searchOptions, 3)

      doc.select("label").text                  must include(messages("agent.clientListSearch.searchFilter.label"))
      doc.select("button[type=submit]").text mustBe messages("site.search")
      doc.getElementsByClass("govuk-link").text must include(messages("agent.clientListSearch.clearSearch"))

      val hint: Element = getElementByClass(doc, "govuk-hint")
      hint.text() mustBe messages("agent.clientListSearch.searchFilter.label.hint")

      doc.getElementById("table-heading").text mustBe messages("agent.clientListSearch.table.caption")

    }

    "must render the client list table with headers and rows" in new Setup {
      val html: HtmlFormat.Appendable = view(
        form = form,
        searchByOptions = searchOptions,
        clientList = clientList,
        paginationViewModel = PaginationViewModel(),
        sortBy = None,
        sortOrder = None
      )
      val doc: Document               = Jsoup.parse(html.body)

      val table = doc.getElementById("agent-client-list")
      table must not be null
      table.attr("data-module") mustBe "moj-sortable-table"

      val headers = table.select("thead th").eachText().asScala.toSeq
      headers mustBe Seq(
        messages("agent.clientListSearch.th.clientName"),
        messages("agent.clientListSearch.th.employersReference"),
        messages("agent.clientListSearch.th.clientReference"),
        messages("agent.clientListSearch.th.actions")
      )

      val rows = table.select("tbody tr").asScala
      rows.size mustBe clientList.size

      val firstRowCells = rows.head.select("td").asScala
      firstRowCells(0).text() mustBe clientList.head.clientName
      firstRowCells(1).text() mustBe clientList.head.employerReference
      firstRowCells(2).text() mustBe clientList.head.clientReference
      firstRowCells(3).text() mustBe messages("agent.clientListSearch.td.actions.remove")
    }

    "must include data-sort-value attribute on client name rows" in new Setup {
      val html: HtmlFormat.Appendable = view(
        form = form,
        searchByOptions = searchOptions,
        clientList = clientList,
        paginationViewModel = PaginationViewModel(),
        sortBy = None,
        sortOrder = None
      )
      val doc: Document               = Jsoup.parse(html.body)

      val rows = doc.select("#agent-client-list tbody tr").asScala
      rows.zip(clientList).foreach { case (row, client) =>
        val firstCell = row.select("td").first()
        firstCell.attr("data-sort-value") mustBe client.clientName
      }
    }

    "must include data-sort-column attributes on sortable headers" in new Setup {
      val html: HtmlFormat.Appendable = view(
        form = form,
        searchByOptions = searchOptions,
        clientList = clientList,
        paginationViewModel = PaginationViewModel(),
        sortBy = None,
        sortOrder = None
      )
      val doc: Document               = Jsoup.parse(html.body)

      val headers = doc.select("#agent-client-list thead th").asScala
      headers(0).attr("data-sort-column") mustBe "clientName"
      headers(1).attr("data-sort-column") mustBe "employerReference"
      headers(2).attr("data-sort-column") mustBe "clientReference"
      headers(3).attr("data-sort-column") mustBe ""
    }

    "must display correct aria-sort state when sorting by clientName ascending" in new Setup {
      val html: HtmlFormat.Appendable = view(
        form = form,
        searchByOptions = searchOptions,
        clientList = clientList,
        paginationViewModel = PaginationViewModel(),
        sortBy = Some("clientName"),
        sortOrder = Some("ascending")
      )
      val doc: Document               = Jsoup.parse(html.body)

      val headers = doc.select("#agent-client-list thead th").asScala
      headers(0).attr("aria-sort") mustBe "ascending"
      headers(1).attr("aria-sort") mustBe "none"
      headers(2).attr("aria-sort") mustBe "none"
    }

    "must display correct aria-sort state when sorting by clientName descending" in new Setup {
      val html: HtmlFormat.Appendable = view(
        form = form,
        searchByOptions = searchOptions,
        clientList = clientList,
        paginationViewModel = PaginationViewModel(),
        sortBy = Some("clientName"),
        sortOrder = Some("descending")
      )
      val doc: Document               = Jsoup.parse(html.body)

      val headers = doc.select("#agent-client-list thead th").asScala
      headers(0).attr("aria-sort") mustBe "descending"
      headers(1).attr("aria-sort") mustBe "none"
      headers(2).attr("aria-sort") mustBe "none"
    }

    "must display correct aria-sort state when sorting by employerReference" in new Setup {
      val html: HtmlFormat.Appendable = view(
        form = form,
        searchByOptions = searchOptions,
        clientList = clientList,
        paginationViewModel = PaginationViewModel(),
        sortBy = Some("employerReference"),
        sortOrder = Some("ascending")
      )
      val doc: Document               = Jsoup.parse(html.body)

      val headers = doc.select("#agent-client-list thead th").asScala
      headers(0).attr("aria-sort") mustBe "none"
      headers(1).attr("aria-sort") mustBe "ascending"
      headers(2).attr("aria-sort") mustBe "none"
    }

    "must display correct aria-sort state when sorting by clientReference" in new Setup {
      val html: HtmlFormat.Appendable = view(
        form = form,
        searchByOptions = searchOptions,
        clientList = clientList,
        paginationViewModel = PaginationViewModel(),
        sortBy = Some("clientReference"),
        sortOrder = Some("descending")
      )
      val doc: Document               = Jsoup.parse(html.body)

      val headers = doc.select("#agent-client-list thead th").asScala
      headers(0).attr("aria-sort") mustBe "none"
      headers(1).attr("aria-sort") mustBe "none"
      headers(2).attr("aria-sort") mustBe "descending"
    }

    "must display none for all columns when no sorting is applied" in new Setup {
      val html: HtmlFormat.Appendable = view(
        form = form,
        searchByOptions = searchOptions,
        clientList = clientList,
        paginationViewModel = PaginationViewModel(),
        sortBy = None,
        sortOrder = None
      )
      val doc: Document               = Jsoup.parse(html.body)

      val headers = doc.select("#agent-client-list thead th").asScala
      headers(0).attr("aria-sort") mustBe "none"
      headers(1).attr("aria-sort") mustBe "none"
      headers(2).attr("aria-sort") mustBe "none"
    }

    "must show error summary and messages when form has errors" in new Setup {
      val boundWithError: Form[ClientListFormData] = form.bind(Map("searchBy" -> "", "searchFilter" -> ""))
      val html: HtmlFormat.Appendable              =
        view(
          form = boundWithError,
          searchByOptions = searchOptions,
          clientList = clientList,
          paginationViewModel = PaginationViewModel(),
          sortBy = None,
          sortOrder = None
        )
      val doc: Document                            = Jsoup.parse(html.body)

      doc.title must startWith(messages("error.title.prefix"))
      doc.select(".govuk-error-summary").size() mustBe 1
      val expected: String = messages("agent.clientListSearch.searchBy.error.required")
      doc.text() must include(expected)
    }

    "must not render pagination when there are 10 or fewer clients" in new Setup {
      val html: HtmlFormat.Appendable = view(
        form = form,
        searchByOptions = searchOptions,
        clientList = clientList,
        paginationViewModel = PaginationViewModel(),
        sortBy = None,
        sortOrder = None
      )
      val doc: Document               = Jsoup.parse(html.body)

      doc.select(".govuk-pagination").size() mustBe 1
      doc.select(".govuk-pagination__list").size() mustBe 0
      doc.select(".govuk-pagination__prev").size() mustBe 0
      doc.select(".govuk-pagination__next").size() mustBe 0
    }

    "must render pagination when paginationViewModel has items" in new Setup {
      val paginationViewModel = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1").withCurrent(true),
          PaginationItemViewModel("2", "/test?page=2")
        ),
        next = Some(PaginationLinkViewModel("/test?page=2").withText("site.pagination.next"))
      )

      val html: HtmlFormat.Appendable = view(
        form = form,
        searchByOptions = searchOptions,
        clientList = clientList,
        paginationViewModel = paginationViewModel,
        sortBy = None,
        sortOrder = None
      )
      val doc: Document               = Jsoup.parse(html.body)

      doc.select(".govuk-pagination").size() mustBe 1
      doc.select(".govuk-pagination__list").size() mustBe 1
      doc.select(".govuk-pagination__item").size() mustBe 2
      doc.select(".govuk-pagination__item--current").size() mustBe 1
      doc.select(".govuk-pagination__next").size() mustBe 1
      doc.select(".govuk-pagination__prev").size() mustBe 0
    }

    "must render pagination with previous link on page 2" in new Setup {
      val paginationViewModel = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1"),
          PaginationItemViewModel("2", "/test?page=2").withCurrent(true)
        ),
        previous = Some(PaginationLinkViewModel("/test?page=1").withText("site.pagination.previous")),
        next = Some(PaginationLinkViewModel("/test?page=3").withText("site.pagination.next"))
      )

      val html: HtmlFormat.Appendable = view(
        form = form,
        searchByOptions = searchOptions,
        clientList = clientList,
        paginationViewModel = paginationViewModel,
        sortBy = None,
        sortOrder = None
      )
      val doc: Document               = Jsoup.parse(html.body)

      doc.select(".govuk-pagination__prev").size() mustBe 1
      doc.select(".govuk-pagination__next").size() mustBe 1
      doc.select(".govuk-pagination__item--current").size() mustBe 1
    }

    "must render pagination below the table" in new Setup {
      val paginationViewModel = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1").withCurrent(true)
        )
      )

      val html: HtmlFormat.Appendable = view(
        form = form,
        searchByOptions = searchOptions,
        clientList = clientList,
        paginationViewModel = paginationViewModel,
        sortBy = None,
        sortOrder = None
      )
      val doc: Document               = Jsoup.parse(html.body)

      val table      = doc.getElementById("agent-client-list")
      val pagination = doc.select(".govuk-pagination").first()

      table      must not be null
      pagination must not be null

      val bodyHtml        = html.body
      val tableIndex      = bodyHtml.indexOf("agent-client-list")
      val paginationIndex = bodyHtml.indexOf("govuk-pagination")

      tableIndex      must be >= 0
      paginationIndex must be >= 0
      paginationIndex must be > tableIndex
    }
  }

  trait Setup {
    val app: Application                           = applicationBuilder().build()
    val view: ClientListSearchView                 = app.injector.instanceOf[ClientListSearchView]
    val formProvider: ClientListSearchFormProvider = app.injector.instanceOf[ClientListSearchFormProvider]
    val form: Form[ClientListFormData]             = formProvider()
    val searchOptions: Seq[SearchByList]           = SearchByList.searchByOptions
    val clientList: Seq[ClientListViewModel]       = Seq(
      ClientListViewModel("123", "ABC Construction Ltd", "123/AB45678", "AOR-001", Active),
      ClientListViewModel("123", "ABC Property Services", "789/EF23456", "AOR-002", Active),
      ClientListViewModel("123", "Capital Construction Group", "345/IJ67890", "AOR-003", Active)
    )
    implicit val request: play.api.mvc.Request[_]  = FakeRequest()
    implicit val messages: Messages                = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
