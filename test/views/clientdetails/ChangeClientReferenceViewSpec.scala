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

package views.clientdetails

import base.SpecBase
import forms.clientdetails.ChangeClientReferenceFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.clientdetails.ChangeClientReferenceView

class ChangeClientReferenceViewSpec extends SpecBase {

  "ChangeClientReferenceView" - {

    "must return the correct html" in new Setup {
      val doc: Document = Jsoup.parse(html.body)
      doc.select("title").text       must include(messages("clientdetails.changeClientReference.title"))
      doc.select("h1").text          must include(messages("clientdetails.changeClientReference.heading"))
      doc.select(".govuk-hint").text must include(messages("clientdetails.changeClientReference.hint"))
      doc.select("input[type=text]").size() mustBe 1
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: ChangeClientReferenceView           = app.injector.instanceOf[ChangeClientReferenceView]
    val formProvider                              = new ChangeClientReferenceFormProvider()
    val form: Form[String]                        = formProvider()
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
    val html: HtmlFormat.Appendable               = view(form, NormalMode)
  }
}
