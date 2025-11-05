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
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers.*
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.ContractorDashboardViewModel
import views.html.ContractorDashboardView

class ContractorDashboardViewSpec extends SpecBase {

  private val employerReference         = "123/AB45678"
  private val utr                       = "1234567890"
  private val returnCount               = 1
  private val returnDueDate             = "19 October 2025"
  private val noticeCount               = 2
  private val lastSubmittedDate         = "19 September 2025"
  private val lastSubmittedTaxMonthYear = "August 2025"

  private val viewModel = ContractorDashboardViewModel(
    employerReference = employerReference,
    utr = utr,
    returnCount = returnCount,
    returnDueDate = returnDueDate,
    noticeCount = noticeCount,
    lastSubmittedDate = lastSubmittedDate,
    lastSubmittedTaxMonthYear = lastSubmittedTaxMonthYear
  )

  "ContractorDashboardView" - {

    "render the page with expected header and title" in {
      val doc = render()

      doc.title() shouldBe s"${messages(app)("contractorDashboard.title")} - ${messages(app)("service.name")} - GOV.UK"

      val h1 = doc.selectFirst("h1")
      h1.text() shouldBe messages(app)("contractorDashboard.heading")
    }

    "show introductory paragraph" in {
      val doc = render()

      doc.text() should include(messages(app)("contractorDashboard.paragraph"))
    }

    "show employerReference and UTR in a summary list" in {
      val doc = render()

      val rows = doc.select(".govuk-summary-list .govuk-summary-list__row")
      rows.size() shouldBe 2

      val employerKey   = rows.get(0).selectFirst(".govuk-summary-list__key").text()
      val employerValue = rows.get(0).selectFirst(".govuk-summary-list__value").text()
      employerKey   shouldBe messages(app)("contractorDashboard.label.employerReference")
      employerValue shouldBe employerReference

      val utrKey   = rows.get(1).selectFirst(".govuk-summary-list__key").text()
      val utrValue = rows.get(1).selectFirst(".govuk-summary-list__value").text()
      utrKey   shouldBe messages(app)("contractorDashboard.label.utr")
      utrValue shouldBe utr
    }

    "render Action required section heading" in {
      val doc = render()

      val headings = doc.select("h2.govuk-heading-m")
      headings.eachText() should contain(messages(app)("contractorDashboard.subheading.actionRequired"))
    }

    "render the Return due dashboard card with count and date" in {
      val doc = render()

      val cards = doc.select(".govuk-summary-card")
      cards.size() should be >= 1

      val returnDueCard = cards.get(0)

      // Check the link title
      val cardTitle = returnDueCard.selectFirst(".govuk-summary-card__title")
      cardTitle.text() should include(messages(app)("contractorDashboard.link.returnDue"))

      // Check the count is displayed
      val content = returnDueCard.selectFirst(".govuk-summary-card__content")
      content.text() should include(returnCount.toString)

      // Check the due date is displayed
      content.text() should include(messages(app)("contractorDashboard.label.dueBy", returnDueDate))
    }

    "render the New notices dashboard card with count" in {
      val doc = render()

      val cards = doc.select(".govuk-summary-card")
      cards.size() should be >= 2

      val newNoticesCard = cards.get(1)

      // Check the link title
      val cardTitle = newNoticesCard.selectFirst(".govuk-summary-card__title")
      cardTitle.text() should include(messages(app)("contractorDashboard.link.newNotices"))

      // Check the count is displayed
      val content = newNoticesCard.selectFirst(".govuk-summary-card__content")
      content.text() should include(noticeCount.toString)

      // Check the subtitle is displayed
      content.text() should include(messages(app)("contractorDashboard.label.newNotices"))
    }

    "render Manage your CIS section heading" in {
      val doc = render()

      val headings = doc.select("h2.govuk-heading-m")
      headings.eachText() should contain(messages(app)("contractorDashboard.subheading.manage"))
    }

    "render Subcontractors link and description" in {
      val doc = render()

      doc.text() should include(messages(app)("contractorDashboard.link.subcontractors"))
      doc.text() should include(messages(app)("contractorDashboard.label.subcontractors"))
    }

    "render Return history link and description" in {
      val doc = render()

      doc.text() should include(messages(app)("contractorDashboard.link.history"))
      doc.text() should include(messages(app)("contractorDashboard.label.history"))
    }

    "render Notices and statements link and description" in {
      val doc = render()

      doc.text() should include(messages(app)("contractorDashboard.link.notices"))
      doc.text() should include(messages(app)("contractorDashboard.label.notices"))
    }

    "render Amend a return link and description" in {
      val doc = render()

      doc.text() should include(messages(app)("contractorDashboard.link.amend"))
      doc.text() should include(messages(app)("contractorDashboard.label.amend"))
    }

    "render Recent activity section heading" in {
      val doc = render()

      val headings = doc.select("h2.govuk-heading-m")
      headings.eachText() should contain(messages(app)("contractorDashboard.subheading.activity"))
    }

    "render Recent activity paragraph with last submitted date and tax month" in {
      val doc = render()

      val expectedActivity =
        messages(app)("contractorDashboard.paragraph.activity", lastSubmittedDate, lastSubmittedTaxMonthYear)
      doc.text() should include(expectedActivity)
    }

    "render sidebar with help and guidance" in {
      val doc = render()

      // Check sidebar title
      doc.text() should include(messages(app)("contractorDashboard.sidebar.title"))

      // Check navigation links
      doc.text() should include(messages(app)("contractorDashboard.sidebar.nav.whatIs.text"))
      doc.text() should include(messages(app)("contractorDashboard.sidebar.nav.guidance.text"))
      doc.text() should include(messages(app)("contractorDashboard.sidebar.nav.penalties.text"))

      // Verify the links have correct hrefs
      val links     = doc.select("a[href]")
      val linkTexts = links.eachText()

      linkTexts should contain(messages(app)("contractorDashboard.sidebar.nav.whatIs.text"))
      linkTexts should contain(messages(app)("contractorDashboard.sidebar.nav.guidance.text"))
      linkTexts should contain(messages(app)("contractorDashboard.sidebar.nav.penalties.text"))

      // Verify external URLs
      links.select("[href=https://www.gov.uk/what-is-the-construction-industry-scheme]").size() shouldBe 1
      links.select("[href=https://www.gov.uk/guidance/cis-monthly-returns]").size()             shouldBe 1
      links.select("[href=https://www.gov.uk/government/publications/cis-340]").size()          shouldBe 1
    }
  }

  private def render(): Document = {
    val application = app

    implicit val request: Request[AnyContent] = FakeRequest(GET, "/contractor-dashboard")
    implicit val msgs: Messages               = messages(application)

    val view = application.injector.instanceOf[ContractorDashboardView]
    val html = view(viewModel)

    Jsoup.parse(html.body)
  }
}
