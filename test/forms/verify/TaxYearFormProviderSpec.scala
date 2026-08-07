package forms.verify

import org.scalatest.LoneElement
import org.scalatest.matchers.must
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpec

class TaxYearFormProviderSpec extends AnyWordSpec with must.Matchers with TableDrivenPropertyChecks with LoneElement {
  import models.verify.VerificationTaxYearSelection.*

  "Tax Year form binding" should {
    val givenTaxYears = 2021 to 2024 map TaxYearPeriod.apply
    val form          = new TaxYearFormProvider()(givenTaxYears)

    val happyScenarios = Table(
      ("Given Input", "Expected Output"),
      ("2021",        TaxYear(2021)),
      ("2022",        TaxYear(2022)),
      ("2023",        TaxYear(2023)),
      ("2024",        TaxYear(2024)),
      ("all",         AllTaxYears)
    )

    "yield correct value when input is valid" in
      forAll(happyScenarios) { (givenInput, expectedOutput) =>
        val filledForm = form.bind(Map("value" -> givenInput))

        filledForm.get mustBe expectedOutput
        filledForm.errors mustBe empty
      }

    val unhappyScenarios = Table(
      ("Given Data",           "Expected Error"),
      (Map("value" -> "2020"), "verificationHistorySelectTaxYear.error.invalid"),
      (Map("value" -> "2025"), "verificationHistorySelectTaxYear.error.invalid"),
      (Map("value" -> "All"),  "verificationHistorySelectTaxYear.error.invalid"),
      (Map("value" -> "ALL"),  "verificationHistorySelectTaxYear.error.invalid"),
      (Map(),                  "verify.verificationHistorySelectTaxYear.error.required")
    )

    "yield an error when input is invalid" in
      forAll(unhappyScenarios) { (givenData, expectedError) =>
        val filledForm = form.bind(givenData)

        filledForm.value mustBe empty
        filledForm.errors.loneElement.message mustBe expectedError
      }
  }
}
