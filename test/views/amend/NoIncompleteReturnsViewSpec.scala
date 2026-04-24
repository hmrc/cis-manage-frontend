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

package views.amend

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.amend.NoIncompleteReturnsView

class NoIncompleteReturnsViewSpec extends SpecBase {

  "NoIncompleteReturnsView" - {

    "must render the page with the correct content" in new Setup {
      val html: HtmlFormat.Appendable = view(cisId = "1")
      val doc: Document               = Jsoup.parse(html.toString)

      doc.title             must include(messages("amend.noIncompleteReturns.title"))
      doc.select("h1").text must include(messages("amend.noIncompleteReturns.heading"))

      doc.select("p.govuk-body").text must include(messages("amend.noIncompleteReturns.p1"))

      doc.select("p.govuk-body").text must include(messages("amend.noIncompleteReturns.p2"))
      doc.select("a.govuk-link").text must include(messages("amend.noIncompleteReturns.p2.link"))
      doc.select("p.govuk-body").text must include(messages("amend.noIncompleteReturns.p3"))
      doc.select("a.govuk-link").text must include(messages("amend.noIncompleteReturns.p3.link"))
    }
  }

  trait Setup {
    val app: Application              = applicationBuilder().build()
    val view: NoIncompleteReturnsView = app.injector.instanceOf[NoIncompleteReturnsView]

    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )
  }
}