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

package views.clientdetails

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.clientdetails.ManageClientDetailsView
import config.FrontendAppConfig

class ManageClientDetailsViewSpec extends SpecBase {

  private val uniqueId    = "1"
  private val clientName  = "ABC Construction Ltd"
  private val employerRef = "123/AB45678"
  private val clientRef   = "AOR1"

  "ManageClientDetailsView" - {

    "render the page with expected header and title" in {
      val (doc, appConfig) = render()

      doc
        .title() shouldBe s"${messages(app)("clientdetails.manageClientDetails.title")} - Construction Industry Scheme - GOV.UK"

      val back = doc.select("a.govuk-back-link")
      back.size() shouldBe 1

      val h1 = doc.selectFirst("h1")
      h1.text() shouldBe s"${messages(app)("clientdetails.manageClientDetails.heading", clientName)}"
    }

    "show employerRef and clientRef in a summary list" in {
      val (doc, _) = render()

      val rows = doc.select(".govuk-summary-list .govuk-summary-list__row")
      rows.size() shouldBe 2

      val employerKey   = rows.get(0).selectFirst(".govuk-summary-list__key").text()
      val employerValue = rows.get(0).selectFirst(".govuk-summary-list__value").text()
      employerKey   shouldBe messages(app).apply("clientdetails.manageClientDetails.employerRef.key")
      employerValue shouldBe employerRef

      val clientKey   = rows.get(1).selectFirst(".govuk-summary-list__key").text()
      val clientValue = rows.get(1).selectFirst(".govuk-summary-list__value").text()
      clientKey   shouldBe messages(app).apply("clientdetails.manageClientDetails.clientRef.key")
      clientValue shouldBe clientRef
    }

    "render Change link alongside Client Reference" in {
      val (doc, _) = render()

      doc.text() should include(messages(app).apply("clientdetails.manageClientDetails.clientRef.key"))
      doc.text() should include(messages(app).apply("site.change"))
    }

    "render Return to client dashboard link" in {
      val (doc, _) = render()

      doc.text() should include(messages(app).apply("clientdetails.manageClientDetails.return.link"))
    }

  }

  private def render(): (Document, FrontendAppConfig) = {
    val application                           = app
    implicit val appConfig: FrontendAppConfig =
      application.injector.instanceOf[FrontendAppConfig]

    implicit val request = FakeRequest(GET, "/agent/authorised-client-manage-CIS-returns")
    implicit val msgs    = messages(application)

    val view = application.injector.instanceOf[ManageClientDetailsView]
    val html = view(
      uniqueId = uniqueId,
      clientName = clientName,
      employerRef = employerRef,
      clientRef = clientRef
    )

    (Jsoup.parse(html.body), appConfig)
  }
}
