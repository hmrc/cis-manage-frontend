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

package views.verify

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.verify.NoVerificationHistoryView

class NoVerificationHistoryViewSpec extends SpecBase {
  "NoVerificationHistoryView" - {
    "must render the page with the correct title, heading, paragraphs and link" in new Setup {
      val cisId                       = "1"
      val html: HtmlFormat.Appendable = view(cisId)
      val doc: Document               = Jsoup.parse(html.body)

      doc.title                                 must include(messages("verify.noVerificationHistory.title"))
      doc.select("h1").text                     must include(messages("verify.noVerificationHistory.heading"))
      doc.select("p").text                      must include(messages("verify.noVerificationHistory.p1"))
      doc.select("p").text                      must include(messages("verify.noVerificationHistory.p2"))
      doc.select("p").text                      must include(messages("verify.noVerificationHistory.backTo"))
      doc.getElementsByClass("govuk-link").text must include(
        messages("verify.noVerificationHistory.manageYourSubcontractors.link")
      )
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: NoVerificationHistoryView           = app.injector.instanceOf[NoVerificationHistoryView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
