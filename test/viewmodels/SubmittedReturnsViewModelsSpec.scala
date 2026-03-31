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

      viewModel.taxYears shouldBe Seq(taxYear)
      viewModel.selectedTaxYear shouldBe Some("2023")
      viewModel.taxYears.head.rows.head.status shouldBe StatusViewModel.Text("Submitted")
    }
  }
}
