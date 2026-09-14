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

package views.history

import base.SpecBase
import models.history.{SubcontractorPayment, SubmittedReturnPrintViewModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.Application
import play.api.i18n.*
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.history.PrintSubmissionDetailsView

class PrintSubmissionDetailsViewSpec extends SpecBase {

  "PrintSubmissionDetailsView" - {

    "must render all details correctly including subcontractor table when type is standard monthly return" in new Setup {
      val monthYear              = "April 2026"
      val submittedTime          = "8:46am"
      val submittedDate          = "16 March 2025"
      val receiptReferenceNumber = "ABC123456789"
      val submissionType         = "standard"
      val contractorName         = "PAL 355 Scheme"
      val payeReference          = "123/AB456"
      val totalPaymentsMade      = "£1900"
      val totalCostOfMaterials   = "£616"
      val totalTaxDeducted       = "£380"

      val subcontractors = Seq(
        SubcontractorPayment(
          name = "BuildRight Construction",
          verificationNumber = "V1234567890",
          paymentsMade = "£165",
          costOfMaterials = "£95",
          taxDeducted = "£95"
        ),
        SubcontractorPayment(
          name = "Northern Trades Ltd",
          verificationNumber = "V0987654321",
          paymentsMade = "£75",
          costOfMaterials = "£55",
          taxDeducted = "£55"
        ),
        SubcontractorPayment(
          name = "TyneWear Ltd",
          verificationNumber = "",
          paymentsMade = "£165",
          costOfMaterials = "£125",
          taxDeducted = "£55"
        )
      )

      val model = SubmittedReturnPrintViewModel(
        monthYear = "April 2026",
        submittedTime = submittedTime,
        submittedDate = submittedDate,
        receiptReferenceNumber = receiptReferenceNumber,
        submissionType = submissionType,
        contractorName = contractorName,
        payeReference = payeReference,
        totalPaymentsMade = totalPaymentsMade,
        totalCostOfMaterials = totalCostOfMaterials,
        totalTaxDeducted = totalTaxDeducted,
        subcontractors = subcontractors
      )

      lazy val html: HtmlFormat.Appendable = view(model, historyUrl)

      val doc: Document = Jsoup.parse(html.toString)
      doc.title must include(messages("history.printSubmissionDetails.title", monthYear))

      doc.select("h1").text must include(
        messages("history.printSubmissionDetails.heading", monthYear)
      )

      doc.select("h2").text must include(
        messages("history.printSubmissionDetails.submissionDetails.heading")
      )

      doc.select("p.govuk-body").text must include(
        messages(
          "history.printSubmissionDetails.submissionDetails.p",
          submittedTime,
          submittedDate
        )
      )

      val summaryText: String = doc.select(".govuk-summary-list").text()
      summaryText must include(receiptReferenceNumber)
      summaryText must include(messages("history.printSubmissionDetails.submissionDetails.submissionType.standard"))
      summaryText must include(contractorName)
      summaryText must include(payeReference)

      doc.select("h2").text must include(
        messages("history.printSubmissionDetails.paymentDetails.heading")
      )

      summaryText must include(subcontractors.size.toString)
      summaryText must include(messages(totalPaymentsMade))
      summaryText must include(totalCostOfMaterials)
      summaryText must include(totalTaxDeducted)

      doc.select("h2").text must include(
        messages("history.printSubmissionDetails.paymentsMadeToSubcontractors.heading")
      )

      val tableHead: String = doc.select("table thead tr").text()
      tableHead must include(
        messages("history.printSubmissionDetails.paymentsMadeToSubcontractors.subcontractor")
      )
      tableHead must include(
        messages("history.printSubmissionDetails.paymentsMadeToSubcontractors.verificationNumber")
      )
      tableHead must include(
        messages("history.printSubmissionDetails.paymentsMadeToSubcontractors.paymentsMade")
      )
      tableHead must include(
        messages("history.printSubmissionDetails.paymentsMadeToSubcontractors.costOfMaterials")
      )
      tableHead must include(messages("history.printSubmissionDetails.paymentsMadeToSubcontractors.taxDeducted"))

      val tableRows: Elements = doc.select("table tbody tr")
      tableRows.size mustBe subcontractors.size
      subcontractors.zipWithIndex.foreach { case (sub, idx) =>
        val row = tableRows.get(idx).text()
        row must include(sub.name)
        row must include(sub.verificationNumber)
        row must include(sub.paymentsMade)
        row must include(sub.costOfMaterials)
        row must include(sub.taxDeducted)
      }

      doc.select("a.govuk-link").text must include(
        messages("history.printSubmissionDetails.printThisPage.link")
      )

      doc.select("a.govuk-link").text must include(
        messages("history.printSubmissionDetails.monthlyReturnHistory.link")
      )
    }

    "must render verification number column when verification number is empty" in new Setup {
      val monthYear              = "April 2026"
      val submittedTime          = "8:46am"
      val submittedDate          = "16 March 2025"
      val receiptReferenceNumber = "ABC123456789"
      val submissionType         = "standard"
      val contractorName         = "PAL 355 Scheme"
      val payeReference          = "123/AB456"
      val totalPaymentsMade      = "£165"
      val totalCostOfMaterials   = "£95"
      val totalTaxDeducted       = "£95"

      val subcontractors = Seq(
        SubcontractorPayment(
          name = "BuildRight Construction",
          verificationNumber = "",
          paymentsMade = "£165",
          costOfMaterials = "£95",
          taxDeducted = "£95"
        )
      )

      val model = SubmittedReturnPrintViewModel(
        monthYear = monthYear,
        submittedTime = submittedTime,
        submittedDate = submittedDate,
        receiptReferenceNumber = receiptReferenceNumber,
        submissionType = submissionType,
        contractorName = contractorName,
        payeReference = payeReference,
        totalPaymentsMade = totalPaymentsMade,
        totalCostOfMaterials = totalCostOfMaterials,
        totalTaxDeducted = totalTaxDeducted,
        subcontractors = subcontractors
      )

      lazy val html: HtmlFormat.Appendable = view(model, historyUrl)

      val doc: Document = Jsoup.parse(html.toString)

      val tableHeaders: Elements = doc.select("table thead th")
      tableHeaders.size mustBe 5

      tableHeaders.text must include(
        messages("history.printSubmissionDetails.paymentsMadeToSubcontractors.verificationNumber")
      )

      val tableRows: Elements = doc.select("table tbody tr")
      tableRows.size mustBe 1

      val tableCells: Elements = tableRows.first().select("th, td")
      tableCells.size mustBe 5

      tableCells.get(0).text mustBe "BuildRight Construction"
      tableCells.get(1).text mustBe ""
      tableCells.get(2).text mustBe "£165"
      tableCells.get(3).text mustBe "£95"
      tableCells.get(4).text mustBe "£95"
    }

    "must render all details correctly when type is nil monthly return but receiptReferenceNumber is empty" in new Setup {
      val monthYear              = "April 2026"
      val submittedTime          = "8:46am"
      val submittedDate          = "16 March 2025"
      val receiptReferenceNumber = ""
      val submissionType         = "nil"
      val contractorName         = "PAL 355 Scheme"
      val payeReference          = "123/AB456"
      val totalPaymentsMade      = ""
      val totalCostOfMaterials   = ""
      val totalTaxDeducted       = ""

      val model = SubmittedReturnPrintViewModel(
        monthYear = monthYear,
        submittedTime = submittedTime,
        submittedDate = submittedDate,
        receiptReferenceNumber = receiptReferenceNumber,
        submissionType = submissionType,
        contractorName = contractorName,
        payeReference = payeReference,
        totalPaymentsMade = totalPaymentsMade,
        totalCostOfMaterials = totalCostOfMaterials,
        totalTaxDeducted = totalTaxDeducted,
        subcontractors = Seq.empty
      )

      lazy val html: HtmlFormat.Appendable = view(model, historyUrl)

      val doc: Document = Jsoup.parse(html.toString)
      doc.title must include(messages("history.printSubmissionDetails.title", monthYear))

      doc.select("h1").text must include(
        messages("history.printSubmissionDetails.heading", monthYear)
      )

      doc.select("h2").text must include(
        messages("history.printSubmissionDetails.submissionDetails.heading")
      )

      doc.select("p.govuk-body").text must include(
        messages(
          "history.printSubmissionDetails.submissionDetails.p",
          submittedTime,
          submittedDate
        )
      )

      val summaryText: String = doc.select(".govuk-summary-list").text()
      summaryText must include(messages("history.printSubmissionDetails.submissionDetails.submissionType.nil"))
      summaryText must include(contractorName)
      summaryText must include(payeReference)
    }
  }

  trait Setup {

    val app: Application                 = applicationBuilder().build()
    val view: PrintSubmissionDetailsView = app.injector.instanceOf[PrintSubmissionDetailsView]
    val historyUrl                       = controllers.history.routes.SubmittedReturnsController.onPageLoadAllYears().url

    implicit val request: Request[_] = FakeRequest()

    implicit val messages: Messages =
      MessagesImpl(
        Lang.defaultLang,
        app.injector.instanceOf[MessagesApi]
      )
  }
}
