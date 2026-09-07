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

import com.google.inject.Inject
import forms.mappings.Mappings
import models.verify.VerificationTaxYearSelection
import play.api.data.Form

class TaxYearFormProvider @Inject() extends Mappings {
  import VerificationTaxYearSelection.*

  private val ALL = "all"

  def apply(taxYears: Seq[TaxYearPeriod]): Form[VerificationTaxYearSelection] =
    val taxYearStrings = taxYears.map(_.startYear.toString).toList
    val validOptions   = ALL :: taxYearStrings
    Form(
      "value" -> text("verify.verificationHistorySelectTaxYear.error.required")
        .verifying("verificationHistorySelectTaxYear.error.invalid", validOptions.contains)
        .transform[VerificationTaxYearSelection](
          VerificationTaxYearSelection.fromString,
          toInputValue
        )
    )

  private def toInputValue(selection: VerificationTaxYearSelection) =
    selection match
      case TaxYear(startYear) => startYear.toString
      case AllTaxYears        => ALL
}
