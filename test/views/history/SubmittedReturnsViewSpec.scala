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

package views.history

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers.*
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import viewmodels.*
import views.html.history.SubmittedReturnsView

class SubmittedReturnsViewSpec extends SpecBase {

  private val populatedViewModel = SubmittedReturnsPageViewModel(
    taxYears = Seq(
      TaxYearHistoryViewModel(
        fromYear = 2023,
        toYear = 2024,
        rows = Seq(
          SubmittedReturnsRowViewModel(
            returnPeriodEnd = "Mar 2024",
            returnType = ReturnTypeViewModel.Standard,
            dateSubmitted = "1 Apr 2024",
            monthlyReturn = LinkViewModel(
              url = "/return/1",
              hiddenText = "Mar 2024"
            ),
            submissionReceipt = StatusViewModel.Text("site.view"),
            status = StatusViewModel.Text("history.returnHistory.status.amend")
          )
        )
      )
    ),
    selectedTaxYear = Some("2023")
  )

  private val emptyViewModel = SubmittedReturnsPageViewModel(
    taxYears = Seq.empty,
    selectedTaxYear = None
  )

  "SubmittedReturnsView" - {

    "render the page with expected title and heading for a single year" in {
      val doc = render(populatedViewModel)

      doc.title() shouldBe
        s"${messages(app)("history.returnHistory.singleYear.title", "2023", "2024")} - ${messages(app)("service.name")} - GOV.UK"

      doc.selectFirst("h1").text() shouldBe
        messages(app)("history.returnHistory.singleYear.heading", "2023", "2024")
    }

    "not render the tax year heading for a single tax year" in {
      val doc = render(populatedViewModel)

      doc.select("h2.govuk-heading-m").eachText() shouldBe empty
    }

    "render the desktop table version" in {
      val doc = render(populatedViewModel)

      val desktop = doc.selectFirst(".return-history-desktop")
      desktop should not be null

      desktop.text() should include("Mar 2024")
      desktop.text() should include(messages(app)("history.returnHistory.returnType.standard"))
      desktop.text() should include("1 Apr 2024")
      desktop.text() should include(messages(app)("site.view"))
      desktop.text() should include(messages(app)("history.returnHistory.status.amend"))

      desktop.select("table").size()      shouldBe 1
      desktop.select("thead th").eachText() should contain allOf (
        messages(app)("history.returnHistory.table.returnPeriodEnd"),
        messages(app)("history.returnHistory.table.returnType"),
        messages(app)("history.returnHistory.table.dateSubmitted"),
        messages(app)("history.returnHistory.table.monthlyReturn"),
        messages(app)("history.returnHistory.table.submissionReceipt"),
        messages(app)("history.returnHistory.table.status")
      )

      desktop.select("a[href=/return/1]").text()            should include(messages(app)("site.view"))
      desktop.select("a[href=/receipt/1]").text().isEmpty shouldBe true
    }

    "render the mobile stacked version" in {
      val doc = render(populatedViewModel)

      val mobile = doc.selectFirst(".return-history-mobile")
      mobile should not be null

      mobile.text() should include("Mar 2024")
      mobile.text() should include(messages(app)("history.returnHistory.table.returnType"))
      mobile.text() should include(messages(app)("history.returnHistory.returnType.standard"))
      mobile.text() should include(messages(app)("history.returnHistory.table.dateSubmitted"))
      mobile.text() should include("1 Apr 2024")
      mobile.text() should include(messages(app)("history.returnHistory.table.monthlyReturn"))
      mobile.text() should include(messages(app)("history.returnHistory.table.submissionReceipt"))
      mobile.text() should include(messages(app)("history.returnHistory.table.status"))
      mobile.text() should include(messages(app)("history.returnHistory.status.amend"))

      mobile.select(".govuk-summary-list").size()        shouldBe 1
      mobile.select("a[href=/return/1]").text()            should include(messages(app)("site.view"))
      mobile.select("a[href=/receipt/1]").text().isEmpty shouldBe true
    }

    "render the empty state when there are no submitted returns" in {
      val doc = render(emptyViewModel)

      doc.selectFirst("h1").text() shouldBe
        messages(app)("history.returnHistory.allYears.heading")

      doc.text()                                      should include(messages(app)("history.returnHistory.noSubmittedReturns"))
      doc.select(".return-history-desktop").isEmpty shouldBe true
      doc.select(".return-history-mobile").isEmpty  shouldBe true
    }
  }

  private def render(viewModel: SubmittedReturnsPageViewModel): Document = {
    val application = app

    implicit val request: Request[AnyContent] = FakeRequest(GET, "/submitted-returns")
    implicit val msgs: play.api.i18n.Messages = messages(application)

    val view = application.injector.instanceOf[SubmittedReturnsView]
    val html = view(viewModel)

    Jsoup.parse(html.body)
  }
}
