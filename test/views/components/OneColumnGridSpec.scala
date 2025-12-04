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

class OneColumnGridSpec extends SpecBase {

  private val oneColumnGrid = app.injector.instanceOf[views.html.components.OneColumnGrid]

  "OneColumnGrid" - {

    "should wrap content inside the one column div" in {
      val html = oneColumnGrid(
        content = Html("""<p class="govuk-body">Hello</p>""")
      )
      val doc  = Jsoup.parse(html.body)

      val rows = doc.getElementsByClass("govuk-grid-row")
      rows.size mustBe 1

      val outerDiv = rows.first
      val innerDiv = outerDiv.getElementsByClass("govuk-grid-column-one-half")
      innerDiv.size() shouldBe 1

      val paragraph = innerDiv.select("p.govuk-body")
      paragraph.text() shouldBe "Hello"
    }
  }
}
