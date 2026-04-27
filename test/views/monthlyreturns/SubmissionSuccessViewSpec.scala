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

package views.monthlyreturns

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers.*
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import models.history.SubcontractorPayment
import viewmodels.SubmissionReceiptViewModel
import views.html.monthlyreturns.SubmissionSuccessView

class SubmissionSuccessViewSpec extends SpecBase {

  private val viewModelWithEmail = SubmissionReceiptViewModel(
    contractorName = "Test Contractor Ltd",
    payeReference = "123/AB12345",
    taxYear = 2024,
    taxMonth = 6,
    returnPeriodEnd = "June 2024",
    returnType = "Monthly",
    submissionType = "Monthly return",
    hmrcMark = Some("HMRC-123-ABC"),
    submittedAt = Some("4:43pm on 1 July 2024"),
    emailRecipient = Some("user@example.com"),
    instanceId = "INST001",
    items = Seq(
      SubcontractorPayment("John Smith", "5000.00", "1000.00", "800.00"),
      SubcontractorPayment("Jane Doe", "3000.00", "500.00", "500.00")
    )
  )

  private val viewModelWithoutEmail = SubmissionReceiptViewModel(
    contractorName = "Test Contractor Ltd",
    payeReference = "123/AB12345",
    taxYear = 2024,
    taxMonth = 6,
    returnPeriodEnd = "June 2024",
    returnType = "Monthly",
    submissionType = "Monthly return",
    hmrcMark = Some("HMRC-123-ABC"),
    submittedAt = Some("4:43pm on 1 July 2024"),
    emailRecipient = None,
    instanceId = "INST001",
    items = Seq(
      SubcontractorPayment("John Smith", "5000.00", "1000.00", "800.00")
    )
  )

  private val nilReturnViewModel = SubmissionReceiptViewModel(
    contractorName = "Nil Co",
    payeReference = "456/CD67890",
    taxYear = 2024,
    taxMonth = 3,
    returnPeriodEnd = "March 2024",
    returnType = "Nil",
    submissionType = "Monthly return",
    hmrcMark = None,
    submittedAt = None,
    emailRecipient = None,
    instanceId = "INST002",
    items = Seq.empty
  )

  private val groupedReferenceViewModel = SubmissionReceiptViewModel(
    contractorName = "Grouped Ref Co",
    payeReference = "789/EF12345",
    taxYear = 2024,
    taxMonth = 8,
    returnPeriodEnd = "August 2024",
    returnType = "Monthly",
    submissionType = "Monthly return",
    hmrcMark = Some("NZUWY4TFOR2XE3TUMVZXI2LSNVQXE23CMFZWKNRU"),
    submittedAt = Some("9:15am on 1 September 2024"),
    emailRecipient = Some("grouped@example.com"),
    instanceId = "INST003",
    items = Seq.empty
  )

  "SubmissionSuccessView" - {

    "render the page with dynamic title and confirmation panel" in {
      val doc = render(viewModelWithEmail)

      doc.title() shouldBe
        s"${messages(app)("submissionConfirmation.title", "Monthly")} - ${messages(app)("service.name")} - GOV.UK"

      doc.select(".govuk-panel__title").text() shouldBe messages(app)("submissionConfirmation.panel.heading", "Monthly")
      doc.select(".govuk-panel__body").text()    should include("HMRC-123-ABC")
    }

    "render dynamic title for Nil return" in {
      val doc = render(nilReturnViewModel)

      doc.select(".govuk-panel__title").text() shouldBe messages(app)("submissionConfirmation.panel.heading", "Nil")
    }

    "render the receipt reference with HTML line breaks instead of literal br text" in {
      val doc  = render(groupedReferenceViewModel)
      val body = doc.select(".govuk-panel__body")

      body.html() should include("<br>")
      body.text() should include("NZUWY4TFOR2XE3TUMVZXI2")
      body.text() should include("LSNVQXE23CMFZWKNRU")
      body.text() should not include "<br>"
    }

    "render summary list with submission details including submitted on" in {
      val doc = render(viewModelWithEmail)

      doc.text() should include("4:43pm on 1 July 2024")
      doc.text() should include("Test Contractor Ltd")
      doc.text() should include("123/AB12345")
      doc.text() should include("June 2024")
    }

    "render email row in summary list when email is present" in {
      val doc = render(viewModelWithEmail)

      doc.text() should include("user@example.com")
    }

    "not render email row when email is absent" in {
      val doc = render(viewModelWithoutEmail)

      doc.text() should not include "user@example.com"
    }

    "render no-email advice when email is absent" in {
      val doc = render(viewModelWithoutEmail)

      doc.text() should include(messages(app)("submissionConfirmation.noEmail"))
    }

    "not render no-email advice when email is present" in {
      val doc = render(viewModelWithEmail)

      doc.text() should not include messages(app)("submissionConfirmation.noEmail")
    }

    "render inset text with print link" in {
      val doc = render(viewModelWithEmail)

      doc.select(".govuk-inset-text").size()             shouldBe 1
      doc.select("[data-module=hmrc-print-link]").size() shouldBe 1
    }

    "render back to manage link" in {
      val doc = render(viewModelWithEmail)

      doc.text() should include(messages(app)("submissionConfirmation.backToManage"))
    }

    "render need help section with CIS enquiries link" in {
      val doc = render(viewModelWithEmail)

      doc.text() should include(messages(app)("submissionConfirmation.needHelp.heading"))
      doc.text() should include(messages(app)("submissionConfirmation.needHelp.link"))
    }

    "not show a back link" in {
      val doc = render(viewModelWithEmail)

      doc.select(".govuk-back-link").size() shouldBe 0
    }
  }

  private def render(viewModel: SubmissionReceiptViewModel): Document = {
    val application = app

    implicit val request: Request[AnyContent] = FakeRequest(GET, "/monthly-return/confirmation")
    implicit val msgs: play.api.i18n.Messages = messages(application)

    val view = application.injector.instanceOf[SubmissionSuccessView]
    val html = view(viewModel)

    Jsoup.parse(html.body)
  }
}
