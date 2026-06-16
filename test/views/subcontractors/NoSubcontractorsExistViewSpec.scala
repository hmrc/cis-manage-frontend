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

package views.subcontractors

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.subcontractors.NoSubcontractorsExistView

class NoSubcontractorsExistViewSpec extends SpecBase {

  "NoSubcontractorsExistView" - {

    "must render the page with the correct content" in new Setup {
      val html: HtmlFormat.Appendable = view(cisId = "1")
      val doc: Document               = Jsoup.parse(html.toString)

      doc.title             must include(messages("subcontractors.noSubcontractorsExist.title"))
      doc.select("h1").text must include(messages("subcontractors.noSubcontractorsExist.heading"))

      doc.select("p.govuk-body").text must include(messages("subcontractors.noSubcontractorsExist.p1"))

      doc.select("p.govuk-body").text must include(messages("subcontractors.noSubcontractorsExist.p2.prefix"))
      doc.select("a.govuk-link").text must include(messages("subcontractors.noSubcontractorsExist.p2.link"))
      doc.select("p.govuk-body").text must include(messages("subcontractors.noSubcontractorsExist.p2.suffix"))

      doc.select("p.govuk-body").text must include(messages("subcontractors.noSubcontractorsExist.p3.prefix"))
      doc.select("a.govuk-link").text must include(messages("subcontractors.noSubcontractorsExist.p3.link"))
    }
  }

  trait Setup {
    val app: Application                = applicationBuilder().build()
    val view: NoSubcontractorsExistView = app.injector.instanceOf[NoSubcontractorsExistView]

    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )
  }
}
