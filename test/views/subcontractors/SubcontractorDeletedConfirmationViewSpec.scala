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
import views.html.subcontractors.SubcontractorDeletedConfirmationView

class SubcontractorDeletedConfirmationViewSpec extends SpecBase {

  "SubcontractorDeletedConfirmationView" - {

    "must render the page with the correct content" in new Setup {

      val subcontractorName     = "ABC Ltd"
      val subcontractorsPageUrl = "/subcontractors"
      val surveyUrl             = "/feedback"

      val html: HtmlFormat.Appendable =
        view(subcontractorName, subcontractorsPageUrl, surveyUrl)

      val doc: Document = Jsoup.parse(html.body)

      doc.title must include(messages("subcontractorDeletedConfirmation.title"))

      doc.select(".govuk-panel__title").text must include(
        messages("subcontractors.subcontractorDeletedConfirmation.heading")
      )

      doc.text() must include(
        messages("subcontractors.subcontractorDeletedConfirmation.p1", subcontractorName)
      )

      val subcontractorLink = doc.select(s"a[href='$subcontractorsPageUrl']")

      subcontractorLink.text must include(
        messages("subcontractors.subcontractorDeletedConfirmation.p2.link")
      )

      doc.select("h2").text must include(
        messages("subcontractors.subcontractorDeletedConfirmation.h2")
      )

      doc.text() must include(
        messages("subcontractors.subcontractorDeletedConfirmation.p3")
      )

      val surveyLink = doc.select(s"a[href='$surveyUrl']")

      surveyLink.text must include(
        messages("subcontractors.subcontractorDeletedConfirmation.p4.link")
      )
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()

    val view: SubcontractorDeletedConfirmationView =
      app.injector.instanceOf[SubcontractorDeletedConfirmationView]

    implicit val request: play.api.mvc.Request[_] = FakeRequest()

    implicit val messages: Messages =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )
  }
}
