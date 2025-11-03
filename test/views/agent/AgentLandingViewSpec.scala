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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.agent.AgentLandingView
import config.FrontendAppConfig
import java.time.{LocalDate, YearMonth}
import java.time.format.DateTimeFormatter

class AgentLandingViewSpec extends SpecBase {

  private val clientName            = "ABC Construction Ltd"
  private val employerRef           = "123/AB45678"
  private val utr                   = "1234567890"
  private val returnsDueCount       = 1
  private val returnsDueBy          = LocalDate.of(2025, 10, 19)
  private val newNoticesCount       = 2
  private val lastSubmittedDate     = LocalDate.of(2025, 9, 19)
  private val lastSubmittedTaxMonth = YearMonth.of(2025, 8)

  "AgentLandingView" - {

    "render the page with expected header, caption and title" in {
      val (doc, appConfig) = render()

      doc.title() shouldBe s"${messages(app).apply("agent.landing.title", clientName)} - GOV.UK"

      val back = doc.selectFirst("a.govuk-back-link")
      back                         should not be null
      back.attr("aria-disabled") shouldBe "true"
      back.hasAttr("onclick")    shouldBe true

      val caption = doc.selectFirst("span.govuk-caption-l")
      caption.text() shouldBe clientName

      val h1 = doc.selectFirst("h1")
      h1.text() shouldBe messages(app).apply("agent.landing.h1")
    }

    "show employerRef and UTR in a summary list" in {
      val (doc, _) = render()

      val rows = doc.select(".govuk-summary-list .govuk-summary-list__row")
      rows.size() shouldBe 2

      val employerKey   = rows.get(0).selectFirst(".govuk-summary-list__key").text()
      val employerValue = rows.get(0).selectFirst(".govuk-summary-list__value").text()
      employerKey   shouldBe messages(app).apply("agent.landing.employerRef.key")
      employerValue shouldBe employerRef

      val utrKey   = rows.get(1).selectFirst(".govuk-summary-list__key").text()
      val utrValue = rows.get(1).selectFirst(".govuk-summary-list__value").text()
      utrKey   shouldBe messages(app).apply("agent.landing.utr.key")
      utrValue shouldBe utr
    }

    "render the Action required cards with counts and dates" in {
      val (doc, _) = render()

      doc.selectFirst("h2.govuk-heading-m").text() shouldBe messages(app).apply("agent.landing.h2.actionRequired")

      val halves = doc.select(".govuk-grid-row .govuk-grid-column-one-half")
      halves.size() should be >= 2

      val card1 = halves.get(0)
      card1.getElementsByClass("govuk-!-font-size-36").first().text() shouldBe returnsDueCount.toString
      val expectedDueBy = messages(app).apply(
        "agent.landing.card.returnDue.dueBy",
        returnsDueBy.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
      )
      card1.text() should include(expectedDueBy)

      val card2 = halves.get(1)
      card2.getElementsByClass("govuk-!-font-size-36").first().text() shouldBe newNoticesCount.toString
      card2.text()                                                      should include(messages(app).apply("agent.landing.card.notices.subtitle"))
    }

    "render Manage client links and blurbs across grid rows" in {
      val (doc, _) = render()

      doc.select("h2.govuk-heading-m").eachText() should contain(messages(app).apply("agent.landing.h2.manageClient"))

      doc.text() should include(messages(app).apply("agent.landing.manage.subcontractors"))
      doc.text() should include(messages(app).apply("agent.landing.manage.subcontractors.blurb"))

      doc.text() should include(messages(app).apply("agent.landing.manage.returnHistory"))
      doc.text() should include(messages(app).apply("agent.landing.manage.returnHistory.blurb"))

      doc.text() should include(messages(app).apply("agent.landing.manage.notices"))
      doc.text() should include(messages(app).apply("agent.landing.manage.notices.blurb"))

      doc.text() should include(messages(app).apply("agent.landing.manage.amend"))
      doc.text() should include(messages(app).apply("agent.landing.manage.amend.blurb"))

      doc.text() should include(messages(app).apply("agent.landing.manage.clientDetails"))
      doc.text() should include(messages(app).apply("agent.landing.manage.clientDetails.blurb"))
    }

    "render Recent activity with formatted date and tax month" in {
      val (doc, _) = render()

      doc.select("h2.govuk-heading-m").eachText() should contain(messages(app).apply("agent.landing.h2.recentActivity"))

      val line = messages(app).apply(
        "agent.landing.recentActivity.line",
        lastSubmittedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
        lastSubmittedTaxMonth.getMonth.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.UK),
        lastSubmittedTaxMonth.getYear.toString
      )

      doc.select("p.govuk-body").eachText().toArray.map(_.toString) should contain(line)
    }
  }

  private def render(): (Document, FrontendAppConfig) = {
    val application                           = app
    implicit val appConfig: FrontendAppConfig =
      application.injector.instanceOf[FrontendAppConfig]

    implicit val request = FakeRequest(GET, "/agent/authorised-client-manage-CIS-returns")
    implicit val msgs    = messages(application)

    val view = application.injector.instanceOf[AgentLandingView]
    val html = view(
      clientName = clientName,
      employerRef = employerRef,
      utr = utr,
      returnsDueCount = returnsDueCount,
      returnsDueBy = returnsDueBy,
      newNoticesCount = newNoticesCount,
      lastSubmittedDate = lastSubmittedDate,
      lastSubmittedTaxMonth = lastSubmittedTaxMonth
    )

    (Jsoup.parse(html.body), appConfig)
  }
}
