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
        text = "View return",
        url = "/return/1",
        hiddenText = "for March 2024"
      )

      val submissionReceiptLink = LinkViewModel(
        text = "View receipt",
        url = "/receipt/1",
        hiddenText = "for March 2024"
      )

      val row = SubmittedReturnsRowViewModel(
        returnPeriodEnd = "31 March 2024",
        dateSubmitted = "1 April 2024",
        monthlyReturn = monthlyReturnLink,
        submissionReceipt = submissionReceiptLink,
        status = StatusViewModel.Text("Submitted")
      )

      val taxYear = TaxYearHistoryViewModel(
        taxYearCaption = "2023 to 2024",
        rows = Seq(row)
      )

      val viewModel = SubmittedReturnsPageViewModel(
        taxYears = Seq(taxYear),
        selectedTaxYear = Some("2023")
      )

      viewModel.taxYears                       shouldBe Seq(taxYear)
      viewModel.selectedTaxYear                shouldBe Some("2023")
      viewModel.taxYears.head.rows.head.status shouldBe StatusViewModel.Text("Submitted")
    }
  }
}
