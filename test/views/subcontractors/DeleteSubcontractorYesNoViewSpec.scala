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
import forms.subcontractors.DeleteSubcontractorYesNoFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.i18n.Messages
import views.html.subcontractors.DeleteSubcontractorYesNoView

class DeleteSubcontractorYesNoViewSpec extends SpecBase {

  "DeleteSubcontractorYesNoView" - {

    "must render the content on the page" in new Setup {

      val subcontractorName = "subcontractorName"
      val html              = view(subcontractorName, form, NormalMode)
      val doc               = Jsoup.parse(html.body)

      doc.title               must include(messages("subcontractors.deleteSubcontractorYesNo.title"))
      doc.select("h1").text() must include(
        messages("subcontractors.deleteSubcontractorYesNo.heading", subcontractorName)
      )
      doc.select("p").text()  must include(messages("subcontractors.deleteSubcontractorYesNo.p"))
      doc.select(".govuk-radios__item").size() mustBe 2

      doc
        .select("legend.govuk-visually-hidden")
        .text() must include(messages("subcontractors.deleteSubcontractorYesNo.heading", subcontractorName))
    }
  }

  trait Setup {
    val app = applicationBuilder().build()

    val view         = app.injector.instanceOf[DeleteSubcontractorYesNoView]
    val formProvider = app.injector.instanceOf[DeleteSubcontractorYesNoFormProvider]
    val form         = formProvider()

    implicit val request: play.api.mvc.Request[_] = FakeRequest()

    implicit val messages: Messages =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )
  }
}
