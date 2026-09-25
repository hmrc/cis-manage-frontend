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

package views.templates

import base.SpecBase
import org.jsoup.Jsoup
import org.scalatest.matchers.should.Matchers.*
import play.twirl.api.Html
import views.html.templates.TwoThirdsOneThirdLayout

class TwoThirdsOneThirdLayoutSpec extends SpecBase {

  private val template = app.injector.instanceOf[TwoThirdsOneThirdLayout]

  "TwoThirdsOneThirdLayout" - {

    "renders content, then sidebar, then the report technical issue" in {
      val html = template(
        sidebar = Html("""<p class="govuk-body sidebar-copy">Sidebar block</p>"""),
        reportTechnicalIssue = Html("""<a class="govuk-link hmrc-report-technical-issue" href="#">Report issue</a>""")
      )(Html("""<p class="govuk-body main-copy">Main block</p>"""))

      val doc     = Jsoup.parse(html.body)
      val columns = doc.select(".govuk-grid-row > div")

      columns.size() shouldBe 3

      columns.get(0).className()                                    shouldBe "govuk-grid-column-two-thirds"
      columns.get(0).selectFirst("p.main-copy").text()              shouldBe "Main block"
      columns.get(0).select(".hmrc-report-technical-issue").isEmpty shouldBe true

      columns.get(1).className()                          shouldBe "govuk-grid-column-one-third"
      columns.get(1).selectFirst("p.sidebar-copy").text() shouldBe "Sidebar block"

      columns.get(2).className()                                        shouldBe "govuk-grid-column-two-thirds"
      columns.get(2).selectFirst(".hmrc-report-technical-issue").text() shouldBe "Report issue"
    }
  }
}
