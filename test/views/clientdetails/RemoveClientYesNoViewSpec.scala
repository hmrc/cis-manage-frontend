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
import forms.clientdetails.RemoveClientYesNoFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.clientdetails.RemoveClientYesNoView

class RemoveClientYesNoViewSpec extends SpecBase {
  "RemoveClientView" - {
    "must render the content on the page" in new Setup {
      val clientName = "clientName"
      val html       = view(clientName, form, NormalMode)
      val doc        = Jsoup.parse(html.body)

      doc.title               must include(messages("clientdetails.removeClient.title", clientName))
      doc.select("h1").text() must include(messages("clientdetails.removeClient.heading", clientName))
      doc.select(".govuk-radios__item").size() mustBe 2
    }
  }

  trait Setup {
    val app                                       = applicationBuilder().build()
    val view                                      = app.injector.instanceOf[RemoveClientYesNoView]
    val formProvider                              = app.injector.instanceOf[RemoveClientYesNoFormProvider]
    val form                                      = formProvider()
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
