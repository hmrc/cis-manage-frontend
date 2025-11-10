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
import views.html.components.HeadingLink

class HeadingLinkSpec extends SpecBase {

  private val template = app.injector.instanceOf[HeadingLink]

  "HeadingLink" - {

    "render an h3 with the default class and a linked, translated text" in {
      implicit val msgs = messages(app)

      val html = template(
        text = "test.nonexisting.key",
        href = "#"
      )

      val doc = Jsoup.parse(html.body)

      val h3 = doc.selectFirst("h3")
      h3               should not be null
      h3.className() shouldBe "govuk-heading-s"

      val a = h3.selectFirst("a.govuk-link")
      a                should not be null
      a.attr("href") shouldBe "#"
      a.text()       shouldBe "test.nonexisting.key"
    }

    "allow overriding the heading class" in {
      implicit val msgs = messages(app)

      val html = template(
        text = "test.nonexisting.key",
        href = "/path",
        headingClass = "govuk-heading-l"
      )

      val doc = Jsoup.parse(html.body)

      val h3 = doc.selectFirst("h3")
      h3               should not be null
      h3.className() shouldBe "govuk-heading-l"

      val a = h3.selectFirst("a.govuk-link")
      a.attr("href") shouldBe "/path"
      a.text()       shouldBe "test.nonexisting.key"
    }
  }
}
