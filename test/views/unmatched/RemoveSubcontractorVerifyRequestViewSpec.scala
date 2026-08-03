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

package views.unmatched

import base.SpecBase
import forms.unmatched.RemoveSubcontractorVerifyRequestFormProvider
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.unmatched.RemoveSubcontractorVerifyRequestView

class RemoveSubcontractorVerifyRequestViewSpec extends SpecBase with Matchers {

  "RemoveSubcontractorVerifyRequestView" - {

    "must render the page with the correct title, heading, paragraphs and link" in new Setup {
      val html = view(form, subcontractorName)
      val doc  = Jsoup.parse(html.body)

      doc.title             must include(messages("unmatched.removeSubcontractorVerifyRequest.title"))
      doc.select("h1").text must include(
        messages("unmatched.removeSubcontractorVerifyRequest.heading", subcontractorName)
      )
      doc.select("p").text  must include(messages("unmatched.removeSubcontractorVerifyRequest.p1"))
      doc.select(".govuk-radios__item").size() mustBe 2
    }
  }

  trait Setup {
    val app                                       = applicationBuilder().build()
    val view                                      = app.injector.instanceOf[RemoveSubcontractorVerifyRequestView]
    val formProvider                              = app.injector.instanceOf[RemoveSubcontractorVerifyRequestFormProvider]
    val form                                      = formProvider()
    val subcontractorName                         = "Test Subcontractor"
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
