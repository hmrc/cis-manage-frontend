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

package views.subcontractors

import base.SpecBase
import forms.subcontractors.SubcontractorsListFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import viewmodels.govuk.PaginationFluency.*
import viewmodels.subcontractors.{SubcontractorsListData, SubcontractorsListRow}
import views.html.subcontractors.SubcontractorsListView

class SubcontractorsListViewSpec extends SpecBase with Matchers {

  "SubcontractorsListView" - {

    "must render title, headings, hint, search field, filters, table, pagination and back link" in new Setup {

      val html: HtmlFormat.Appendable =
        view(
          form,
          rows,
          pagination,
          page = 1,
          totalPages = 2,
          startIndex = 1,
          totalCount = rows.size,
          instanceId = instanceId,
          searchTerm = "",
          verificationStatus = "all",
          taxTreatment = "all"
        )

      val doc: Document = Jsoup.parse(html.body)

      doc.title must include(messages("subcontractors.subcontractorsList.title"))

      doc.select("h1").text mustBe messages("subcontractors.subcontractorsList.heading")

      doc.select(".govuk-heading-m").first.text.trim mustBe
        messages("subcontractors.subcontractorsList.h2")

      doc.select(".govuk-hint").text mustBe messages("subcontractors.subcontractorsList.hint")

      doc.select("input[name=searchTerm]").size() mustBe 1

      doc.select("select[name=verificationStatus]").size() mustBe 1

      doc.select("select[name=taxTreatment]").size() mustBe 1

      doc.select("button.govuk-button").first.text.trim mustBe
        messages("subcontractors.subcontractorsList.searchAndFilter")

      val clearFiltersLink =
        doc.select(
          s"a[href='${controllers.subcontractors.routes.SubcontractorsListController.onPageLoad(instanceId).url}']"
        )

      clearFiltersLink.size() mustBe 1
      clearFiltersLink.text() mustBe messages("subcontractors.subcontractorsList.clearFilters")

      doc.select("#subcontractors-table tbody tr").size() mustBe rows.size

      doc.select(".govuk-pagination").size() mustBe 1

      val backLink =
        doc.select(s"a[href='${controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId).url}']")

      backLink.size() mustBe 1
      backLink.text() mustBe messages("subcontractors.subcontractorsList.manage")

      doc.body().text() must include(messages("subcontractors.subcontractorsList.backTo"))
    }

    "must render showing results text when pagination exists" in new Setup {

      val html =
        view(
          form,
          rows,
          pagination,
          page = 1,
          totalPages = 2,
          startIndex = 1,
          totalCount = rows.size,
          instanceId = instanceId,
          searchTerm = "",
          verificationStatus = "all",
          taxTreatment = "all"
        )

      val doc = Jsoup.parse(html.body)

      doc.text must include(
        messages(
          "subcontractors.subcontractorsList.showingResults",
          1,
          1 + rows.size - 1,
          rows.size
        )
      )
    }

    "must NOT render showing results text when pagination is empty" in new Setup {

      val html =
        view(
          form,
          rows,
          PaginationViewModel(),
          page = 1,
          totalPages = 1,
          startIndex = 1,
          totalCount = rows.size,
          instanceId = instanceId,
          searchTerm = "",
          verificationStatus = "all",
          taxTreatment = "all"
        )

      val doc = Jsoup.parse(html.body)

      doc.text must not include messages(
        "subcontractors.subcontractorsList.showingResults",
        1,
        1 + rows.size - 1,
        rows.size
      )
    }

    "must render pagination when items exist" in new Setup {

      val paginationWithItems =
        PaginationViewModel(
          items = Seq(
            PaginationItemViewModel("1", "").withCurrent(true),
            PaginationItemViewModel("2", "")
          ),
          next = Some(PaginationLinkViewModel("").withText("site.pagination.next"))
        )

      val html =
        view(
          form,
          rows,
          paginationWithItems,
          page = 1,
          totalPages = 2,
          startIndex = 1,
          totalCount = rows.size,
          instanceId = instanceId,
          searchTerm = "",
          verificationStatus = "all",
          taxTreatment = "all"
        )

      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-pagination").size() mustBe 1
    }

    "must render error summary when form has errors" in new Setup {

      val formWithError =
        form.withError("searchTerm", "error.required")

      val html =
        view(
          formWithError,
          rows,
          pagination,
          page = 1,
          totalPages = 2,
          startIndex = 1,
          totalCount = rows.size,
          instanceId = instanceId,
          searchTerm = "",
          verificationStatus = "all",
          taxTreatment = "all"
        )

      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").size() mustBe 1
    }

    "must prepopulate search term and selected filters" in new Setup {

      val html =
        view(
          form.fill("Alan"),
          rows,
          pagination,
          page = 2,
          totalPages = 4,
          startIndex = 9,
          totalCount = 20,
          instanceId = instanceId,
          searchTerm = "Alan",
          verificationStatus = "verified",
          taxTreatment = "gross"
        )

      val doc = Jsoup.parse(html.body)

      doc.select("input[name=searchTerm]").attr("value") mustBe "Alan"
      doc.select("select[name=verificationStatus] option[selected]").attr("value") mustBe "verified"
      doc.select("select[name=taxTreatment] option[selected]").attr("value") mustBe "gross"
    }

    "must render delete links for each subcontractor" in new Setup {

      val html =
        view(
          form,
          rows,
          pagination,
          page = 1,
          totalPages = 2,
          startIndex = 1,
          totalCount = rows.size,
          instanceId = instanceId,
          searchTerm = "",
          verificationStatus = "all",
          taxTreatment = "all"
        )

      val doc = Jsoup.parse(html.body)

      rows.foreach { row =>

        val expectedUrl =
          controllers.subcontractors.routes.GetSubcontractorForDeleteController
            .onPageLoad(row.subbieResourceRef)
            .url

        doc.select(s"a[href='$expectedUrl']").size() mustBe 1
      }
    }
  }

  trait Setup {

    val app: Application = applicationBuilder().build()

    implicit val request: FakeRequest[_] =
      FakeRequest()

    implicit val messages: Messages =
      MessagesImpl(Lang.defaultLang, app.injector.instanceOf[MessagesApi])

    val view: SubcontractorsListView =
      app.injector.instanceOf[SubcontractorsListView]

    val form =
      new SubcontractorsListFormProvider()()

    val rows: Seq[SubcontractorsListRow] =
      SubcontractorsListData.rows.take(6)

    val pagination: PaginationViewModel =
      PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "").withCurrent(true),
          PaginationItemViewModel("2", "")
        ),
        next = Some(PaginationLinkViewModel("").withText("site.pagination.next"))
      )

    val instanceId: String = "test-instance-id"
  }
}
