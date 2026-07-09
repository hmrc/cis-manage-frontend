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

package views.agent

import base.SpecBase
import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers.*
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.agent.AgentLandingView

class AgentLandingViewSpec extends SpecBase {

  private val uniqueId    = "1"
  private val agentName   = "Agent name"
  private val schemeName  = "UHD Contractor Control Group"
  private val employerRef = "123/AB45678"

  "AgentLandingView" - {

    "render the page with expected header and title" in {
      val (doc, appConfig) = render()

      doc
        .title() shouldBe s"${messages(app)("agent.landing.title")} - Construction Industry Scheme - GOV.UK"

      val back = doc.select("a.govuk-back-link")
      back.size() shouldBe 1

      val h1 = doc.selectFirst("h1")
      h1.text() shouldBe messages(app).apply("agent.landing.h1")
    }

    "show employerRef, scheme name and client name in a summary list" in {
      val (doc, _) = render()

      val rows = doc.select(".govuk-summary-list .govuk-summary-list__row")
      rows.size() shouldBe 3

      val agentNameKey   = rows.get(0).selectFirst(".govuk-summary-list__key").text()
      val agentNameValue = rows.get(0).selectFirst(".govuk-summary-list__value").text()
      agentNameKey   shouldBe messages(app).apply("agent.landing.agentName.key")
      agentNameValue shouldBe agentName

      val schemeNameKey   = rows.get(1).selectFirst(".govuk-summary-list__key").text()
      val schemeNameValue = rows.get(1).selectFirst(".govuk-summary-list__value").text()
      schemeNameKey   shouldBe messages(app).apply("agent.landing.schemeName.key")
      schemeNameValue shouldBe schemeName

      val employerKey   = rows.get(2).selectFirst(".govuk-summary-list__key").text()
      val employerValue = rows.get(2).selectFirst(".govuk-summary-list__value").text()
      employerKey   shouldBe messages(app).apply("agent.landing.employerRef.key")
      employerValue shouldBe employerRef
    }

    "show 'Not provided' when scheme name is empty" in {
      val (doc, _) = render(schemeName = "")

      val rows = doc.select(".govuk-summary-list .govuk-summary-list__row")

      val schemeNameKey   = rows.get(1).selectFirst(".govuk-summary-list__key").text()
      val schemeNameValue = rows.get(1).selectFirst(".govuk-summary-list__value").text()

      schemeNameKey   shouldBe messages(app).apply("agent.landing.schemeName.key")
      schemeNameValue shouldBe messages(app).apply("agent.landing.schemeName.key.notProvided")
    }

    "render Manage client links and key value pairs across grid rows" in {
      val (doc, _) = render()

      doc.select("#subsection-title").text() should include(messages(app).apply("agent.landing.h2.help"))
      doc.select(".govuk-link").text()       should include(messages(app).apply("agent.landing.help.link1"))
      doc.select(".govuk-link").text()       should include(messages(app).apply("agent.landing.help.link2"))
      doc.select(".govuk-link").text()       should include(messages(app).apply("agent.landing.help.link3"))

      doc.text() should include(messages(app).apply("agent.landing.card.manageYourCisReturns.title"))
      doc.text() should include(messages(app).apply("agent.landing.card.manageYourCisReturns.p"))

      doc.text() should include(messages(app).apply("agent.landing.card.manageYourSubcontractors.title"))
      doc.text() should include(messages(app).apply("agent.landing.card.manageYourSubcontractors.p"))

      doc.text() should include(messages(app).apply("agent.landing.card.manageYourContractorDetails.title"))
      doc.text() should include(messages(app).apply("agent.landing.card.manageYourContractorDetails.p"))

      doc.text() should include(messages(app).apply("agent.landing.card.appealPenalty.title"))
      doc.text() should include(messages(app).apply("agent.landing.card.appealPenalty.p"))

      doc.text() should include(messages(app).apply("agent.landing.card.manageClientDetails.title"))
      doc.text() should include(messages(app).apply("agent.landing.card.manageClientDetails.p"))

      doc.text() should include(messages(app).apply("agent.landing.card.removeClient.title"))
      doc.text() should include(messages(app).apply("agent.landing.card.removeClient.p"))

      doc.text() should include(messages(app).apply("agent.landing.card.noticesAndStatements.title"))
      doc.text() should include(messages(app).apply("agent.landing.card.noticesAndStatements.p"))
    }
  }

  private def render(schemeName: String = this.schemeName): (Document, FrontendAppConfig) = {
    val application                           = app
    implicit val appConfig: FrontendAppConfig =
      application.injector.instanceOf[FrontendAppConfig]

    implicit val request: FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest(GET, "/agent/authorised-client-manage-CIS-returns")
    implicit val msgs: Messages                               = messages(application)

    val view = application.injector.instanceOf[AgentLandingView]
    val html = view(
      uniqueId = uniqueId,
      agentName = agentName,
      employerRef = employerRef,
      schemeName = schemeName
    )

    (Jsoup.parse(html.body), appConfig)
  }
}
