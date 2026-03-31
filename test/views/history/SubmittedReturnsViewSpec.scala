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
        taxYearCaption = "2023 to 2024",
        rows = Seq(
          SubmittedReturnsRowViewModel(
            returnPeriodEnd = "31 March 2024",
            dateSubmitted = "1 April 2024",
            monthlyReturn = LinkViewModel(
              text = "View return",
              url = "/return/1",
              hiddenText = "for March 2024"
            ),
            submissionReceipt = LinkViewModel(
              text = "View receipt",
              url = "/receipt/1",
              hiddenText = "for March 2024"
            ),
            status = StatusViewModel.Text("Submitted")
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
        s"${messages(app)("returnHistory.singleYear.title", "2023", "2024")} - ${messages(app)("service.name")} - GOV.UK"

      doc.selectFirst("h1").text() shouldBe
        messages(app)("returnHistory.singleYear.heading", "2023", "2024")
    }

    "render the submitted returns table and row content" in {
      val doc = render(populatedViewModel)

      doc.text() should include("2023 to 2024")
      doc.text() should include("31 March 2024")
      doc.text() should include("1 April 2024")
      doc.text() should include("View return")
      doc.text() should include("View receipt")
      doc.text() should include("Submitted")

      doc.select("a[href=/return/1]").text()  should include("View return")
      doc.select("a[href=/receipt/1]").text() should include("View receipt")
    }

    "render the empty state when there are no submitted returns" in {
      val doc = render(emptyViewModel)

      doc.selectFirst("h1").text() shouldBe
        messages(app)("returnHistory.allYears.heading")

      doc.text() should include(messages(app)("returnHistory.noSubmittedReturns"))
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
