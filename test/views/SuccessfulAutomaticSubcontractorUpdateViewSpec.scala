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
import viewmodels.SuccessfulAutomaticSubcontractorUpdateViewModel
import views.html.SuccessfulAutomaticSubcontractorUpdateView

class SuccessfulAutomaticSubcontractorUpdateViewSpec extends SpecBase {

  "SuccessfulAutomaticSubcontractorUpdateViewS" - {
    "must render the page with the correct heading, paragraph, table and button" in new Setup {
      val html: HtmlFormat.Appendable = view(subcontractorsList)
      val doc: Document               = Jsoup.parse(html.body)

      doc.title                                   must include(messages("successfulAutomaticSubcontractorUpdate.title"))
      doc.select("h1").text                       must include(messages("successfulAutomaticSubcontractorUpdate.heading"))
      doc.select("p").text                        must include(messages("successfulAutomaticSubcontractorUpdate.p1"))
      doc.select("th").text                       must include(messages("successfulAutomaticSubcontractorUpdate.th.name"))
      doc.select("th").text                       must include(messages("successfulAutomaticSubcontractorUpdate.th.uniqueTaxReference"))
      doc.select("th").text                       must include(messages("successfulAutomaticSubcontractorUpdate.th.verificationNumber"))
      doc.select("th").text                       must include(messages("successfulAutomaticSubcontractorUpdate.th.dateAdded"))
      doc.getElementsByClass("govuk-button").text must include(messages("site.continue"))

      subcontractorsList.length mustBe 6
    }
  }

  trait Setup {
    val app: Application                                 = applicationBuilder().build()
    val view: SuccessfulAutomaticSubcontractorUpdateView =
      app.injector.instanceOf[SuccessfulAutomaticSubcontractorUpdateView]
    implicit val request: play.api.mvc.Request[_]        = FakeRequest()
    implicit val messages: Messages                      = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )

    val subcontractorsList: Seq[SuccessfulAutomaticSubcontractorUpdateViewModel] = Seq(
      SuccessfulAutomaticSubcontractorUpdateViewModel("Alice, A", "1111111111", " ", "01 Jan 2014"),
      SuccessfulAutomaticSubcontractorUpdateViewModel("Bob, B", "2222222222", " ", "01 Jan 2014"),
      SuccessfulAutomaticSubcontractorUpdateViewModel("Dave, D", "4444444444", "V1000000009", "07 May 2015"),
      SuccessfulAutomaticSubcontractorUpdateViewModel("Charles, C", "3333333333", "V1000000009", "01 Jan 2014"),
      SuccessfulAutomaticSubcontractorUpdateViewModel("Elise, E", "5555555555", "V1000000009", "07 May 2015"),
      SuccessfulAutomaticSubcontractorUpdateViewModel("Frank, F", "6666666666", "V1000000009", "07 Jan 2018")
    )
  }
}
