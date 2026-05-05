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

package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.test.FakeRequest
import play.api.i18n.*
import play.twirl.api.HtmlFormat
import play.api.mvc.Request
import viewmodels.{ActionLinkViewModel, IncompleteReturnsRowViewModel}
import views.html.IncompleteReturnsView

class IncompleteReturnsViewSpec extends SpecBase {

  "IncompleteReturnsView" - {
    "must render the page with the correct html elements" in new Setup {
      val html: HtmlFormat.Appendable = view(viewModel)
      val doc: Document               = Jsoup.parse(html.body)

      doc.title             must include(msgs("incompleteReturns.title"))
      doc.select("h1").text must include(msgs("incompleteReturns.heading"))
      doc.text              must include(msgs("incompleteReturns.message"))

      doc.select("th").text must include(msgs("incompleteReturns.table.returnPeriodEnd"))
      doc.select("th").text must include(msgs("incompleteReturns.table.returnType"))
      doc.select("th").text must include(msgs("incompleteReturns.table.lastUpdate"))
      doc.select("th").text must include(msgs("incompleteReturns.table.status"))
      doc.select("th").text must include(msgs("incompleteReturns.table.action"))

      doc.text                                  must include("Jan 2025")
      doc.text                                  must include("Nil")
      doc.text                                  must include("01 Jan 2025")
      doc.text                                  must include("In progress")
      doc.getElementsByClass("govuk-link").text must include(msgs("incompleteReturns.action.continue"))
      doc.getElementsByClass("govuk-link").text must include(msgs("incompleteReturns.action.delete"))
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()

    val view: IncompleteReturnsView =
      app.injector.instanceOf[IncompleteReturnsView]

    implicit val request: Request[_] = FakeRequest()
    implicit val msgs: Messages      = messages(app)

    val viewModel: Seq[IncompleteReturnsRowViewModel] = Seq(
      IncompleteReturnsRowViewModel(
        returnPeriodEnd = "Jan 2025",
        returnType = "Nil",
        lastUpdate = "01 Jan 2025",
        status = "In progress",
        action = Seq(
          ActionLinkViewModel(
            textKey = "incompleteReturns.action.continue",
            href = "/continue"
          ),
          ActionLinkViewModel(
            textKey = "incompleteReturns.action.delete",
            href = "/delete"
          )
        ),
        amendment = Some("N")
      )
    )
  }
}
