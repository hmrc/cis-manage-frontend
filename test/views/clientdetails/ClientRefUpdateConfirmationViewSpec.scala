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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.clientdetails.ClientRefUpdateConfirmationView

class ClientRefUpdateConfirmationViewSpec extends SpecBase {

  "ClientRefUpdateConfirmationView" - {

    "must render the page with panel, next steps heading and dashboard link" in new Setup {
      val html: HtmlFormat.Appendable = view("ABC123", "XYZ456")
      val doc: Document               = Jsoup.parse(html.body)

      doc.title()             must include(messages("clientdetails.clientRefUpdateConfirmation.title"))
      doc.select(".govuk-panel__title").text() mustBe messages("clientdetails.clientRefUpdateConfirmation.heading")
      doc.select("p").text()  must include("You changed this client reference from ABC123 to XYZ456.")
      doc.select("h2").text() must include(messages("clientdetails.clientRefUpdateConfirmation.h2"))
      doc.select("p").text()  must include(messages("clientdetails.clientRefUpdateConfirmation.p2"))

      val dashboardHref = controllers.contractor.routes.ContractorLandingController.onPageLoad().url
      val dashboardLink = doc.select(s"a.govuk-link[href='$dashboardHref']")
      dashboardLink.text() mustBe messages("clientdetails.clientRefUpdateConfirmation.p2.link")
      dashboardLink.attr("href") mustBe dashboardHref
    }

    "must not show a back link" in new Setup {
      val html: HtmlFormat.Appendable = view("ABC123", "XYZ456")
      val doc: Document               = Jsoup.parse(html.body)

      doc.select(".govuk-back-link").size() mustBe 0
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: ClientRefUpdateConfirmationView     = app.injector.instanceOf[ClientRefUpdateConfirmationView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
