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

package views.verify

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers.*
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import viewmodels.*
import views.html.verify.VerificationHistoryView

class VerificationHistoryViewSpec extends SpecBase {

  private val singleYearViewModel = VerificationHistoryPageViewModel(
    taxYears = Seq(
      VerificationTaxYearViewModel(
        fromYear = 2026,
        toYear = 2027,
        rows = Seq(
          VerificationHistoryRowViewModel(
            verificationNumber = "V0004528765",
            dateSubmitted = "6 Apr 2026",
            verificationRequestLink = "#",
            submissionReceiptLink = "#"
          )
        )
      )
    ),
    selectedTaxYear = Some("2026"),
    instanceId = "900063"
  )

  private val allYearsViewModel = VerificationHistoryPageViewModel(
    taxYears = Seq(
      VerificationTaxYearViewModel(
        fromYear = 2026,
        toYear = 2027,
        rows = Seq(
          VerificationHistoryRowViewModel(
            verificationNumber = "V0004528765",
            dateSubmitted = "6 Apr 2026",
            verificationRequestLink = "#",
            submissionReceiptLink = "#"
          )
        )
      ),
      VerificationTaxYearViewModel(
        fromYear = 2025,
        toYear = 2026,
        rows = Seq(
          VerificationHistoryRowViewModel(
            verificationNumber = "V0004528759",
            dateSubmitted = "6 Apr 2025",
            verificationRequestLink = "#",
            submissionReceiptLink = "#"
          )
        )
      )
    ),
    selectedTaxYear = None,
    instanceId = "900063"
  )

  private val emptyViewModel = VerificationHistoryPageViewModel(
    taxYears = Seq.empty,
    selectedTaxYear = None,
    instanceId = "900063"
  )

  "VerificationHistoryView" - {

    "render the page with expected title and heading for a single year" in {
      val doc = render(singleYearViewModel)

      doc.title() shouldBe
        s"${messages(app)("verify.verificationHistory.singleYear.title")} - ${messages(app)("service.name")} - GOV.UK"

      doc.selectFirst("h1").text() shouldBe
        messages(app)("verify.verificationHistory.singleYear.heading", "2026", "2027")
    }

    "render the page with expected title and heading for all years" in {
      val doc = render(allYearsViewModel)

      doc.title() shouldBe
        s"${messages(app)("verify.verificationHistory.allYears.title")} - ${messages(app)("service.name")} - GOV.UK"

      doc.selectFirst("h1").text() shouldBe
        messages(app)("verify.verificationHistory.allYears.heading")
    }

    "not render tax year headings for a single tax year" in {
      val doc = render(singleYearViewModel)

      doc.select("h2.govuk-heading-m").eachText() shouldBe empty
    }

    "render tax year headings when showing all tax years" in {
      val doc = render(allYearsViewModel)

      val headings = doc.select("h2.govuk-heading-m").eachText()
      headings should contain(messages(app)("verify.verificationHistory.taxYear.heading", "2026", "2027"))
      headings should contain(messages(app)("verify.verificationHistory.taxYear.heading", "2025", "2026"))
    }

    "render the desktop table version" in {
      val doc = render(singleYearViewModel)

      val desktop = doc.selectFirst(".verification-history-desktop")
      desktop should not be null

      desktop.select("table").size()      shouldBe 1
      desktop.select("thead th").eachText() should contain allOf (
        messages(app)("verify.verificationHistory.table.verificationNumber"),
        messages(app)("verify.verificationHistory.table.dateSubmitted"),
        messages(app)("verify.verificationHistory.table.verificationRequest"),
        messages(app)("verify.verificationHistory.table.submissionReceipt")
      )

      desktop.text()                        should include("V0004528765")
      desktop.text()                        should include("6 Apr 2026")
      desktop.select("a.govuk-link").text() should include(messages(app)("site.view"))
    }

    "render the mobile stacked version" in {
      val doc = render(singleYearViewModel)

      val mobile = doc.selectFirst(".verification-history-mobile")
      mobile should not be null

      mobile.text()                                 should include("V0004528765")
      mobile.text()                                 should include(messages(app)("verify.verificationHistory.table.dateSubmitted"))
      mobile.text()                                 should include("6 Apr 2026")
      mobile.text()                                 should include(messages(app)("verify.verificationHistory.table.verificationRequest"))
      mobile.text()                                 should include(messages(app)("verify.verificationHistory.table.submissionReceipt"))
      mobile.select("a.govuk-link").text()          should include(messages(app)("site.view"))
      mobile.select(".govuk-summary-list").size() shouldBe 1
    }

    "render the back to manage subcontractors link" in {
      val doc = render(singleYearViewModel)

      val manageLink = doc.select("a:contains(Back to Manage your subcontractors)")
      manageLink should not be empty
    }

    "render the empty state when there are no verification requests" in {
      val doc = render(emptyViewModel)

      doc.text()                                            should include(messages(app)("verify.verificationHistory.noHistory"))
      doc.select(".verification-history-desktop").isEmpty shouldBe true
      doc.select(".verification-history-mobile").isEmpty  shouldBe true
    }

    "render the back to manage subcontractors link in the empty state" in {
      val doc = render(emptyViewModel)

      val manageLink = doc.select("a:contains(Back to Manage your subcontractors)")
      manageLink should not be empty
    }
  }

  private def render(viewModel: VerificationHistoryPageViewModel): Document = {
    val application = app

    implicit val request: Request[AnyContent] = FakeRequest(GET, "/verification-history")
    implicit val msgs: play.api.i18n.Messages = messages(application)

    val view = application.injector.instanceOf[VerificationHistoryView]
    val html = view(viewModel)

    Jsoup.parse(html.body)
  }
}
