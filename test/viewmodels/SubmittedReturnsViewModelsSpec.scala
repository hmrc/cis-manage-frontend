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

package viewmodels

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class SubmittedReturnsViewModelsSpec extends AnyWordSpec with Matchers {

  "SubmittedReturnsViewModels" should {

    "store values correctly" in {
      val monthlyReturnLink = LinkViewModel(
        url = "/return/1",
        hiddenText = "Mar 2024"
      )

      val row = SubmittedReturnsRowViewModel(
        returnPeriodEnd = "31 March 2024",
        returnType = ReturnTypeViewModel.Nil,
        dateSubmitted = "1 April 2024",
        monthlyReturn = monthlyReturnLink,
        submissionReceipt = StatusViewModel.Text("site.view"),
        status = StatusViewModel.Text("history.returnHistory.status.amend")
      )

      val taxYear = TaxYearHistoryViewModel(
        fromYear = 2023,
        toYear = 2024,
        rows = Seq(row)
      )

      val viewModel = SubmittedReturnsPageViewModel(
        taxYears = Seq(taxYear),
        selectedTaxYear = Some("2023")
      )

      viewModel.taxYears                       shouldBe Seq(taxYear)
      viewModel.selectedTaxYear                shouldBe Some("2023")
      viewModel.showReturnToTaxYearsLink       shouldBe false
      viewModel.showTaxYearHeadings            shouldBe false
      viewModel.taxYears.head.rows.head.status shouldBe StatusViewModel.Text("history.returnHistory.status.amend")
    }

    "store StatusViewModel.Link values correctly" in {
      val monthlyReturnLink = LinkViewModel(
        url = "/return/2",
        hiddenText = "Apr 2024"
      )

      val receiptLink = LinkViewModel(
        url = "/receipt/2",
        hiddenText = "View receipt for Apr 2024"
      )

      val row = SubmittedReturnsRowViewModel(
        returnPeriodEnd = "30 April 2024",
        returnType = ReturnTypeViewModel.Standard,
        dateSubmitted = "2 May 2024",
        monthlyReturn = monthlyReturnLink,
        submissionReceipt = StatusViewModel.Link(
          link = receiptLink,
          textKey = "site.view",
          hiddenTextKey = "history.returnHistory.receipt.hidden"
        ),
        status = StatusViewModel.Text("history.returnHistory.status.awaitingConfirmation")
      )

      row.returnType        shouldBe ReturnTypeViewModel.Standard
      row.submissionReceipt shouldBe StatusViewModel.Link(
        link = receiptLink,
        textKey = "site.view",
        hiddenTextKey = "history.returnHistory.receipt.hidden"
      )
    }

    "store Unknown return type correctly" in {
      val row = SubmittedReturnsRowViewModel(
        returnPeriodEnd = "31 May 2024",
        returnType = ReturnTypeViewModel.Unknown,
        dateSubmitted = "1 June 2024",
        monthlyReturn = LinkViewModel(
          url = "/return/3",
          hiddenText = "May 2024"
        ),
        submissionReceipt = StatusViewModel.Text("site.view"),
        status = StatusViewModel.Text("UNKNOWN_STATUS")
      )

      row.returnType shouldBe ReturnTypeViewModel.Unknown
      row.status     shouldBe StatusViewModel.Text("UNKNOWN_STATUS")
    }

    "show tax year headings when there is more than one tax year" in {
      val taxYear1 = TaxYearHistoryViewModel(
        fromYear = 2023,
        toYear = 2024,
        rows = Seq.empty
      )

      val taxYear2 = TaxYearHistoryViewModel(
        fromYear = 2024,
        toYear = 2025,
        rows = Seq.empty
      )

      val viewModel = SubmittedReturnsPageViewModel(
        taxYears = Seq(taxYear1, taxYear2),
        selectedTaxYear = None,
        showReturnToTaxYearsLink = true
      )

      viewModel.showTaxYearHeadings      shouldBe true
      viewModel.showReturnToTaxYearsLink shouldBe true
    }

    "allow no selected tax year and no tax years" in {
      val viewModel = SubmittedReturnsPageViewModel(
        taxYears = Seq.empty,
        selectedTaxYear = None
      )

      viewModel.taxYears                 shouldBe Seq.empty
      viewModel.selectedTaxYear          shouldBe None
      viewModel.showReturnToTaxYearsLink shouldBe false
      viewModel.showTaxYearHeadings      shouldBe false
    }
  }
}
