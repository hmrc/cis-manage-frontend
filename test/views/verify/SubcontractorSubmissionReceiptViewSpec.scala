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
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.verify.SubcontractorSubmissionReceiptView

class SubcontractorSubmissionReceiptViewSpec extends SpecBase {

  "SubcontractorSubmissionReceiptView" - {

    "must render the page with the correct content" in new Setup {
      val submissionTime = "12:00"
      val submissionDate = "18 May 2025"
      val contractorName = "John Doe"
      val employerRef    = "ABC12345"
      val irNumber       = "123456"
      val cisId          = "1"

      val html: HtmlFormat.Appendable = view(
        submissionTime,
        submissionDate,
        contractorName,
        employerRef,
        irNumber,
        cisId
      )
      val doc: Document               = Jsoup.parse(html.toString)

      doc.title must include(
        messages("verify.subcontractorSubmissionReceipt.title")
      )

      doc.select("h1").text must include(
        messages("verify.subcontractorSubmissionReceipt.heading")
      )

      doc.select("h2").text must include(
        messages("verify.subcontractorSubmissionReceipt.subheading")
      )

      doc.select("p.govuk-body").text must include(
        messages(
          "verify.subcontractorSubmissionReceipt.p1",
          submissionTime,
          submissionDate
        )
      )

      val summaryText: String = doc.select(".govuk-summary-list").text()
      summaryText must include(messages("verify.subcontractorSubmissionReceipt.summaryList.key1"))
      summaryText must include(contractorName)
      summaryText must include(messages("verify.subcontractorSubmissionReceipt.summaryList.key2"))
      summaryText must include(employerRef)
      summaryText must include(messages("verify.subcontractorSubmissionReceipt.summaryList.key3"))
      summaryText must include(irNumber)

      doc.select("p.govuk-body").text must include(
        messages("verify.subcontractorSubmissionReceipt.p2")
      )

      doc.select("p.govuk-body").text must include(
        messages("verify.subcontractorSubmissionReceipt.p3")
      )

      doc.select("a.govuk-link").text must include(
        messages("verify.subcontractorSubmissionReceipt.link1")
      )

      doc.select("p.govuk-body").text must include(messages("verify.subcontractorSubmissionReceipt.link2.prefix"))
      doc.select("a.govuk-link").text must include(messages("verify.subcontractorSubmissionReceipt.link2.link"))
    }
  }

  trait Setup {
    val app: Application                         = applicationBuilder().build()
    val view: SubcontractorSubmissionReceiptView = app.injector.instanceOf[SubcontractorSubmissionReceiptView]

    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )
  }
}
