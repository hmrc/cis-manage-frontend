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

class GridColumnOneHalfSpec extends SpecBase {

  private val template = app.injector.instanceOf[views.html.components.GridColumnOneHalf]

  "GridColumnOneHalf" - {

    "wrap content inside a govuk-grid-column-one-half div" in {
      val html = template(
        content = Html("""<p class="govuk-body">Hello</p>""")
      )
      val doc  = Jsoup.parse(html.body)

      val div = doc.selectFirst("div")
      div               should not be null
      div.className() shouldBe "govuk-grid-column-one-half"

      val p = div.selectFirst("p.govuk-body")
      p          should not be null
      p.text() shouldBe "Hello"
    }
  }
}
