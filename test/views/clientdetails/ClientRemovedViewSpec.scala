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
import org.scalatest.matchers.should.Matchers.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.clientdetails.ClientRemovedView
import config.FrontendAppConfig

class ClientRemovedViewSpec extends SpecBase {

  "ClientRemovedView" - {

    "render the page with expected header and title" in {
      val (doc, appConfig) = render()

      doc
        .title() shouldBe s"${messages(app)("clientdetails.clientRemoved.title")} - Construction Industry Scheme - GOV.UK"

      val back = doc.select("a.govuk-back-link")
      back.size() shouldBe 0

      val h1 = doc.selectFirst("h1")
      h1.text() shouldBe s"${messages(app)("clientdetails.clientRemoved.heading")}"
      val h2 = doc.selectFirst("h2")
      h2.text() shouldBe s"${messages(app)("clientdetails.clientRemoved.h2")}"
    }

    "render paragraph text and links" in {
      val (doc, _) = render()

      doc.text() should include(messages(app).apply("clientdetails.clientRemoved.p1"))
      doc.text() should include(messages(app).apply("clientdetails.clientRemoved.return.link"))
      doc.text() should include(messages(app).apply("clientdetails.clientRemoved.survey.link"))
      doc.text() should include(messages(app).apply("clientdetails.clientRemoved.survey.suffix"))
    }

  }

  private def render(): (Document, FrontendAppConfig) = {
    val application                           = app
    implicit val appConfig: FrontendAppConfig =
      application.injector.instanceOf[FrontendAppConfig]

    implicit val request = FakeRequest(GET, "/agent/authorised-client-manage-CIS-returns")
    implicit val msgs    = messages(application)

    val view = application.injector.instanceOf[ClientRemovedView]
    val html = view()

    (Jsoup.parse(html.body), appConfig)
  }
}
