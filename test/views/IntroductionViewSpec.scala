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

package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.IntroductionView

class IntroductionViewSpec extends SpecBase {

  "IntroductionViewSpec" - {

    "must render the page with the correct main content" in new Setup {
      val html: HtmlFormat.Appendable = view()
      val doc: Document               = Jsoup.parse(html.body)

      doc.title             must include(messages("introduction.title"))
      doc.select("h1").text must include(messages("introduction.heading"))

      doc.select("p").text  must include(messages("introduction.p1"))
      doc.select("li").text must include(messages("introduction.listItem1"))
      doc.select("li").text must include(messages("introduction.listItem2"))
      doc.select("li").text must include(messages("introduction.listItem3"))
      doc.select("li").text must include(messages("introduction.listItem4"))
      doc.select("li").text must include(messages("introduction.listItem5"))
      doc.select("li").text must include(messages("introduction.listItem6"))
      doc.select("li").text must include(messages("introduction.listItem7"))

      doc.select("p").text must include(messages("introduction.inset"))

      doc.select("h2").text must include(messages("introduction.h2"))
      doc.select("p").text  must include(messages("introduction.p2"))
      doc.select("li").text must include(messages("introduction.listItem8"))
      doc.select("li").text must include(messages("introduction.listItem9"))
      doc.select("li").text must include(messages("introduction.listItem10"))

      doc.select("span").text                   must include(messages("introduction.details.summary"))
      doc.select("h3").text                     must include(messages("introduction.details.h3"))
      doc.select("p").text                      must include(messages("introduction.details.p1"))
      doc.select("p").text                      must include(messages("introduction.details.p2"))
      doc.getElementsByClass("govuk-link").text must include(messages("introduction.details.link"))

      doc.getElementsByClass("govuk-button").text must include(messages("site.continue"))
    }

    "must render the page with the correct sidebar links and header" in new Setup {
      val html: HtmlFormat.Appendable = view()
      val doc: Document               = Jsoup.parse(html.body)

      doc.select("h2").text                     must include(messages("introduction.sidebar.h2"))
      doc.getElementsByClass("govuk-link").text must include(messages("introduction.sidebar.link1"))
      doc.getElementsByClass("govuk-link").text must include(messages("introduction.sidebar.link2"))
      doc.getElementsByClass("govuk-link").text must include(messages("introduction.sidebar.link3"))
      doc.getElementsByClass("govuk-link").text must include(messages("introduction.sidebar.link4"))
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: IntroductionView                    = app.injector.instanceOf[IntroductionView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
