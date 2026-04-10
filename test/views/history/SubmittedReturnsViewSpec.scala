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

    "render the submitted returns table and row content" in {
      val doc = render(populatedViewModel)

      doc.text() should include("Tax year 2023 to 2024")
      doc.text() should include("Mar 2024")
      doc.text() should include(messages(app)("history.returnHistory.returnType.standard"))
      doc.text() should include("1 Apr 2024")
      doc.text() should include(messages(app)("site.view"))
      doc.text() should include(messages(app)("history.returnHistory.status.amend"))

      doc.select("a[href=/return/1]").text()            should include(messages(app)("site.view"))
      doc.select("a[href=/receipt/1]").text().isEmpty shouldBe true
    }

    "render the empty state when there are no submitted returns" in {
      val doc = render(emptyViewModel)

      doc.selectFirst("h1").text() shouldBe
        messages(app)("history.returnHistory.allYears.heading")

      doc.text() should include(messages(app)("history.returnHistory.noSubmittedReturns"))
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
