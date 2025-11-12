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
import org.scalatest.matchers.should.Matchers.*
import play.twirl.api.Html
import views.html.components.SummaryCardBlock

class SummaryCardBlockSpec extends SpecBase {

  private val template = app.injector.instanceOf[SummaryCardBlock]

  "SummaryCardBlock" - {

    "renders GOV.UK summary card wrapper, title, and content" in {
      val html = template(
        title = Html("Card title")
      )(
        content = Html("""<p class="govuk-body">Hello</p>""")
      )

      val doc = Jsoup.parse(html.body)

      val wrapper = doc.selectFirst("div.govuk-summary-card")
      wrapper should not be null

      val title = wrapper.selectFirst("h3.govuk-summary-card__title")
      title          should not be null
      title.text() shouldBe "Card title"

      val content = wrapper.selectFirst("div.govuk-summary-card__content")
      content                                      should not be null
      content.selectFirst("p.govuk-body").text() shouldBe "Hello"
    }

    "allows rich HTML in the title (e.g. a link)" in {
      val html = template(
        title = Html("""<a href="#" class="govuk-link">View details</a>""")
      )(
        content = Html("""<span class="govuk-body">Body</span>""")
      )

      val doc = Jsoup.parse(html.body)

      val titleLink = doc.selectFirst("h3.govuk-summary-card__title a.govuk-link")
      titleLink                should not be null
      titleLink.attr("href") shouldBe "#"
      titleLink.text()       shouldBe "View details"
    }
  }
}
