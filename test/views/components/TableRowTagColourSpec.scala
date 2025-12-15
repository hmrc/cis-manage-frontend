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
import org.scalatest.matchers.must.Matchers
import play.api.test.FakeRequest
import play.api.i18n.Messages
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.Application
import play.twirl.api.Html
import views.html.components.TableRowTagColour

class TableRowTagColourSpec extends SpecBase with Matchers {

  "TableRowTagColour" - {
    "must render the correct text and class for the strong element" in new Setup {
      val html: Html    = view("Some text", "")
      val doc: Document = Jsoup.parse(html.body)

      val strong: Elements = doc.select("strong")
      strong.text mustBe "Some text"
      strong.hasClass("govuk-tag") mustBe true
    }

    "must render a strong element with text and colour class when is provided" in new Setup {
      val html: Html    = view("Some text", "green")
      val doc: Document = Jsoup.parse(html.body)

      val strong: Elements = doc.select("strong")
      strong.text mustBe "Some text"
      strong.hasClass("govuk-tag") mustBe true
      strong.hasClass("green") mustBe true
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: TableRowTagColour                   = app.injector.instanceOf[TableRowTagColour]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
