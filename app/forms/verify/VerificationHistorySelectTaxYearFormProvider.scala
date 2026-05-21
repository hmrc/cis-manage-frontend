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

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class VerificationHistorySelectTaxYearFormProvider @Inject() extends Mappings {

  def apply(taxYears: Seq[String]): Form[String] =
    Form(
      "value" -> text("verify.verificationHistorySelectTaxYear.error.required")
        .verifying(
          "verificationHistorySelectTaxYear.error.invalid",
          value => taxYears.contains(value) || value == "all"
        )
    )
}
