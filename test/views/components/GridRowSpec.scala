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
import org.scalatest.matchers.should.Matchers._
import play.twirl.api.Html

class GridRowSpec extends SpecBase {

  private val template = app.injector.instanceOf[views.html.components.GridRow]

  "GridRow" - {

    "render a govuk-grid-row wrapper and include child content" in {
      val html = template()(Html("""<p class="govuk-body">Hello</p>"""))
      val doc  = Jsoup.parse(html.body)

      val wrapper = doc.selectFirst("div.govuk-grid-row")
      wrapper should not be null

      val p = wrapper.selectFirst("p.govuk-body")
      p          should not be null
      p.text() shouldBe "Hello"
    }

    "merge extra classes and apply attributes to the wrapper" in {
      val html = template(
        classes = "govuk-!-margin-top-4 extra",
        attributes = Map("data-test" -> "123")
      )(Html("""<span class="govuk-body">Content</span>"""))
      val doc  = Jsoup.parse(html.body)

      val wrapper = doc.selectFirst("div.govuk-grid-row")
      wrapper                                     should not be null
      wrapper.classNames()                        should contain allOf ("govuk-grid-row", "govuk-!-margin-top-4", "extra")
      wrapper.attr("data-test")                 shouldBe "123"
      wrapper.selectFirst(".govuk-body").text() shouldBe "Content"
    }
  }
}
