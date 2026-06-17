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
import views.html.subcontractors.CannotDeleteSubcontractorView

class CannotDeleteSubcontractorViewSpec extends SpecBase {

  "CannotDeleteSubcontractorView" - {

    "must render the page with the correct content" in new Setup {

      val subcontractorName     = "ABC Ltd"
      val subcontractorsPageUrl = "/subcontractors"

      val html: HtmlFormat.Appendable = view(subcontractorName, subcontractorsPageUrl)
      val doc: Document               = Jsoup.parse(html.body)

      doc.title must include(messages("subcontractors.cannotDeleteSubcontractor.title"))

      doc.select("h1").text must include(
        messages("subcontractors.cannotDeleteSubcontractor.heading", subcontractorName)
      )

      doc.text() must include(messages("subcontractors.cannotDeleteSubcontractor.p1"))
      doc.text() must include(messages("subcontractors.cannotDeleteSubcontractor.p2"))

      doc.text()                                must include(messages("subcontractors.cannotDeleteSubcontractor.p3.text"))
      doc.getElementsByClass("govuk-link").text must include(
        messages("subcontractors.cannotDeleteSubcontractor.p3.link")
      )

      val links = doc.getElementsByClass("govuk-link")

      links.eachAttr("href").contains(subcontractorsPageUrl) mustBe true
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: CannotDeleteSubcontractorView       = app.injector.instanceOf[CannotDeleteSubcontractorView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()

    implicit val messages: Messages =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )
  }
}
