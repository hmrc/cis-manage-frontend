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

package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.components.PtaCard

class PtaCardSpec extends SpecBase with Matchers {

  "PtaCard" - {
    "must render the correct link, title and paragraph" in new Setup {
      val title   = "My card title"
      val link    = "/some-link"
      val content = Html("Some description text")

      val html: Html    = view(title, link, content)
      val doc: Document = Jsoup.parse(html.body)

      val anchor = doc.select("a.pta-card-link-wrapper")
      anchor.size mustBe 1
      anchor.attr("href") mustBe link
      anchor.hasClass("govuk-link") mustBe true
      anchor.hasClass("govuk-summary-card") mustBe true

      val h3 = doc.select("h3.pta-card-heading")
      h3.text mustBe title

      val p = doc.select("p.pta-card-text")
      p.text mustBe "Some description text"

      doc.select("div.pta-card").size mustBe 1
      doc.select("div.pta-card-body").size mustBe 1
    }

    "must apply all expected classes for styling" in new Setup {
      val html: Html    = view("Title", "/link", Html("Content"))
      val doc: Document = Jsoup.parse(html.body)

      val wrapper = doc.select("a.pta-card-link-wrapper")
      wrapper.hasClass("govuk-link") mustBe true
      wrapper.hasClass("govuk-summary-card") mustBe true

      val card = doc.select("div.pta-card")
      card.hasClass("govuk-summary-card__title-wrapper") mustBe true

      val body = doc.select("div.pta-card-body")
      body.size mustBe 1
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: PtaCard                             = app.injector.instanceOf[PtaCard]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
