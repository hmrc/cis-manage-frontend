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
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.RetrievingSubcontractorsView

class RetrievingSubcontractorsViewSpec extends SpecBase {

  "RetrievingSubcontractorsView" - {

    "must render the header and paragraphs on the page" in new Setup {
      val runUrl = "/your-subcontractors/run/instance-123"
      val html   = view(runUrl)
      val doc    = Jsoup.parse(html.body)

      doc.title             must include(messages("retrievingSubcontractors.title"))
      doc.select("h1").text must include(messages("retrievingSubcontractors.heading"))
      doc.select("p").text  must include(messages("retrievingSubcontractors.p1"))
      doc.select("p").text  must include(messages("retrievingSubcontractors.p2"))
      doc.select("p").text  must include(messages("retrievingSubcontractors.p3.bold"))
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: RetrievingSubcontractorsView        = app.injector.instanceOf[RetrievingSubcontractorsView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
