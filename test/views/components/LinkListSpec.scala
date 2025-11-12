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
import views.html.components.LinkList

class LinkListSpec extends SpecBase {

  private val template = app.injector.instanceOf[LinkList]

  "LinkList" - {

    "renders items with default list/link classes and no new-tab attrs" in {
      implicit val msgs = messages(app)

      val items = Seq(
        "test.nonexisting.key.1" -> "/one",
        "test.nonexisting.key.2" -> "/two"
      )

      val html = template(items)
      val doc  = Jsoup.parse(html.body)

      val ul = doc.selectFirst("ul")
      ul              should not be null
      ul.classNames() should contain allOf ("govuk-list", "govuk-!-font-size-16")

      val lis = ul.select("> li")
      lis.size() shouldBe 2

      val a1 = lis.get(0).selectFirst("a.govuk-link")
      a1.attr("href")      shouldBe "/one"
      a1.hasAttr("target") shouldBe false
      a1.hasAttr("rel")    shouldBe false
      a1.text()            shouldBe "test.nonexisting.key.1" // Messages falls back to key

      val a2 = lis.get(1).selectFirst("a.govuk-link")
      a2.attr("href") shouldBe "/two"
      a2.text()       shouldBe "test.nonexisting.key.2"
    }

    "applies class overrides and new-tab attributes when requested" in {
      implicit val msgs = messages(app)

      val html = template(
        items = Seq("test.nonexisting.key" -> "/k1"),
        listClasses = "govuk-list custom-list",
        linkClasses = "govuk-link custom-link",
        openInNewTab = true
      )
      val doc  = Jsoup.parse(html.body)

      val ul = doc.selectFirst("ul")
      ul.classNames() should contain allOf ("govuk-list", "custom-list")

      val a = ul.selectFirst("li a")
      a.classNames()     should contain allOf ("govuk-link", "custom-link")
      a.attr("target") shouldBe "_blank"
      a.attr("rel")    shouldBe "noreferrer noopener"
    }
  }
}
