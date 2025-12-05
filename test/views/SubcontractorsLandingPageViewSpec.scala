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
import views.html.SubcontractorsLandingPageView

class SubcontractorsLandingPageViewSpec extends SpecBase {

  "SubcontractorsLandingPageView" - {

    "must render the correct title, heading, paragraphs and links" in new Setup {
      val contractorName              = "ABC Organisation Ltd"
      val html: HtmlFormat.Appendable = view(contractorName)
      val doc: Document               = Jsoup.parse(html.body)

      doc.title                                 must include(messages("subcontractorsLandingPage.title"))
      doc.select("h1").text                     must include(messages("subcontractorsLandingPage.heading"))
      doc.select("p").text                      must include(messages("subcontractorsLandingPage.p1"))
      doc.select("p").text                      must include(messages("subcontractorsLandingPage.hint"))
      doc.getElementsByClass("govuk-link").text must include(messages("subcontractorsLandingPage.addSubcontractors"))
      doc.select("p").text                      must include(messages("subcontractorsLandingPage.addSubcontractors.p1"))
      doc.getElementsByClass("govuk-link").text must include(messages("subcontractorsLandingPage.verifySubcontractors"))
      doc.select("p").text                      must include(messages("subcontractorsLandingPage.verifySubcontractors.p1"))
      doc.getElementsByClass("govuk-link").text must include(messages("subcontractorsLandingPage.subcontractorList"))
      doc.select("p").text                      must include(messages("subcontractorsLandingPage.subcontractorList.p1"))
      doc.select("h2").text                     must include(messages("subcontractorsLandingPage.h2"))
      doc.select("p").text                      must include(messages("subcontractorsLandingPage.p2"))
      doc.getElementsByClass("govuk-link").text must include(messages("subcontractorsLandingPage.p2.link"))
      doc.select("p").text                      must include(messages("subcontractorsLandingPage.p3"))

      doc.select("h2").text                     must include(messages("subcontractorsLandingPage.aside.h2"))
      doc.getElementsByClass("govuk-link").text must include(messages("subcontractorsLandingPage.aside.link1"))
      doc.getElementsByClass("govuk-link").text must include(messages("subcontractorsLandingPage.aside.link2"))
      doc.getElementsByClass("govuk-link").text must include(messages("subcontractorsLandingPage.aside.link3"))
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: SubcontractorsLandingPageView       =
      app.injector.instanceOf[SubcontractorsLandingPageView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
