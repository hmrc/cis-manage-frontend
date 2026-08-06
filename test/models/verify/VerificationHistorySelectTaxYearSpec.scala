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

package models.verify

import base.SpecBase
import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear, TaxYearPeriod}
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class VerificationHistorySelectTaxYearSpec extends SpecBase {

  "VerificationHistorySelectTaxYear" - {

    "options must mark the matching current tax year" in {
      val result = VerificationHistorySelectTaxYear.options(
        Seq(TaxYearPeriod(2026), TaxYearPeriod(2025)),
        currentDate = LocalDate.of(2026, 8, 6)
      )(messages(app))

      result.head.content.asHtml.toString must include("2026 to 2027 (current tax year)")
      result(1).content.asHtml.toString mustBe "2025 to 2026"
    }

    "options must use the previous start year before 6 April" in {
      val result = VerificationHistorySelectTaxYear.options(
        Seq(TaxYearPeriod(2026), TaxYearPeriod(2025)),
        currentDate = LocalDate.of(2026, 4, 5)
      )(messages(app))

      result.head.content.asHtml.toString mustBe "2026 to 2027"
      result(1).content.asHtml.toString must include("2025 to 2026 (current tax year)")
    }
  }

  "VerificationTaxYearSelection" - {

    "must serialise and deserialise TaxYear" in {

      val model = TaxYear(2026)

      val json = Json.toJson(model)

      json.validate[VerificationTaxYearSelection] mustEqual JsSuccess(model)
    }

    "must serialise and deserialise AllTaxYears" in {

      val model = AllTaxYears

      val json = Json.toJson(model)

      json.validate[VerificationTaxYearSelection] mustEqual JsSuccess(model)
    }

    "fromString must return AllTaxYears for all" in {

      VerificationTaxYearSelection.fromString("all") mustEqual AllTaxYears
    }

    "fromString must return TaxYear for tax year value" in {

      VerificationTaxYearSelection
        .fromString("2026") mustEqual TaxYear(2026)
    }
  }
}
