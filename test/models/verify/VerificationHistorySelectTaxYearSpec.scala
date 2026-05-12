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

import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

class VerificationHistorySelectTaxYearSpec extends AnyFreeSpec with Matchers {

  "VerificationTaxYearSelection" - {

    "must serialise and deserialise TaxYear" in {

      val model = TaxYear("2026 to 2027")

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
        .fromString("2026 to 2027") mustEqual TaxYear("2026 to 2027")
    }
  }
}
