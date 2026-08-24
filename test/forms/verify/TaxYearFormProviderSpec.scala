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
