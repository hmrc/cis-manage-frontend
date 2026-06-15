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
import views.html.verify.VerificationRequestView

class VerificationRequestViewSpec extends SpecBase {

  private val viewModelWithReverify = VerificationRequestPageViewModel(
    submittedTime = "14:30",
    submittedDate = "6 February 2027",
    verificationNumber = "V0004528765",
    totalSubcontractors = 7,
    subcontractorsToVerify = Seq(
      SubcontractorRowViewModel("Amity Marine Contractors", "V0004528765"),
      SubcontractorRowViewModel("Brody, Martin", "V0004528765")
    ),
    subcontractorsToReverify = Seq(
      SubcontractorRowViewModel("Orca Industrial", "V0004528765/L")
    ),
    manageSubcontractorsUrl = "/manage-subcontractors/900063"
  )

  private val viewModelWithoutReverify = viewModelWithReverify.copy(
    subcontractorsToReverify = Seq.empty,
    totalSubcontractors = 2
  )

  "VerificationRequestView" - {

    "render the page with expected title" in {
      val doc = render(viewModelWithReverify)

      doc.title() shouldBe
        s"${messages(app)("verify.verificationRequest.title")} - ${messages(app)("service.name")} - GOV.UK"
    }

    "render the H1 heading" in {
      val doc = render(viewModelWithReverify)

      doc.selectFirst("h1").text() shouldBe messages(app)("verify.verificationRequest.heading")
    }

    "render the Submission details H2 heading" in {
      val doc = render(viewModelWithReverify)

      doc.select("h2").eachText() should contain(messages(app)("verify.verificationRequest.submissionDetails.heading"))
    }

    "render the submitted at paragraph" in {
      val doc = render(viewModelWithReverify)

      doc.text() should include("Submitted at 14:30 on 6 February 2027")
    }

    "render the verification number in the summary list" in {
      val doc = render(viewModelWithReverify)

      doc.text() should include(messages(app)("verify.verificationRequest.verificationNumber"))
      doc.text() should include("V0004528765")
    }

    "render the subcontractors count in the summary list" in {
      val doc = render(viewModelWithReverify)

      doc.text() should include(messages(app)("verify.verificationRequest.subcontractorsInRequest"))
      doc.text() should include("7")
    }

    "render the Subcontractors to verify heading" in {
      val doc = render(viewModelWithReverify)

      doc.select("h2").eachText() should contain(
        messages(app)("verify.verificationRequest.subcontractorsToVerify.heading")
      )
    }

    "render the subcontractors to verify table with correct data" in {
      val doc = render(viewModelWithReverify)

      doc.text() should include("Amity Marine Contractors")
      doc.text() should include("Brody, Martin")
    }

    "render the table headers" in {
      val doc = render(viewModelWithReverify)

      val headers = doc.select("thead th").eachText()
      headers should contain(messages(app)("verify.verificationRequest.table.name"))
      headers should contain(messages(app)("verify.verificationRequest.table.verificationNumber"))
    }

    "render the Subcontractors to reverify section when reverifications exist" in {
      val doc = render(viewModelWithReverify)

      doc.select("h2").eachText() should contain(
        messages(app)("verify.verificationRequest.subcontractorsToReverify.heading")
      )
      doc.text()                  should include("Orca Industrial")
    }

    "not render the Subcontractors to reverify section when no reverifications exist" in {
      val doc = render(viewModelWithoutReverify)

      doc.select("h2").eachText() should not contain messages(app)(
        "verify.verificationRequest.subcontractorsToReverify.heading"
      )
      doc.text()                  should not include "Orca Industrial"
    }

    "render the print link" in {
      val doc = render(viewModelWithReverify)

      val printLink = doc.select("a[data-module=hmrc-print-link]")
      printLink          should not be empty
      printLink.text() shouldBe messages(app)("verify.verificationRequest.printThisRequest")
    }

    "render the back to manage subcontractors link" in {
      val doc = render(viewModelWithReverify)

      val manageLink = doc.select(s"a[href=/manage-subcontractors/900063]")
      manageLink          should not be empty
      manageLink.text() shouldBe messages(app)("verify.verificationRequest.backToManage")
    }

    "render the back link" in {
      val doc = render(viewModelWithReverify)

      val backLink = doc.select(".govuk-back-link")
      backLink should not be empty
    }
  }

  private def render(viewModel: VerificationRequestPageViewModel): Document = {
    val application = app

    implicit val request: Request[AnyContent] = FakeRequest(GET, "/verification-request")
    implicit val msgs: play.api.i18n.Messages = messages(application)

    val view = application.injector.instanceOf[VerificationRequestView]
    val html = view(viewModel)

    Jsoup.parse(html.body)
  }
}
