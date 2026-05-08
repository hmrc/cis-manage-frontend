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

import models.verify.VerificationTaxYearSelection._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._

class VerificationTaxYearSelectionSpec extends AnyFreeSpec with Matchers {

  "VerificationTaxYearSelection" - {

    "fromString" - {

      "must return AllTaxYears when value is all" in {

        VerificationTaxYearSelection.fromString("all") mustEqual AllTaxYears
      }

      "must return TaxYear when value is a tax year" in {

        VerificationTaxYearSelection
          .fromString("2026 to 2027 (current tax year)") mustEqual
          TaxYear("2026 to 2027 (current tax year)")
      }
    }

    "TaxYear JSON format" - {

      "must serialise TaxYear" in {

        Json.toJson(TaxYear("2026 to 2027")) mustEqual
          Json.obj(
            "value" -> "2026 to 2027"
          )
      }

      "must deserialise TaxYear" in {

        Json
          .obj(
            "value" -> "2026 to 2027"
          )
          .as[TaxYear] mustEqual
          TaxYear("2026 to 2027")
      }
    }

    "AllTaxYears JSON format" - {

      "must serialise AllTaxYears" in {

        Json.toJson(AllTaxYears) mustEqual
          Json.obj(
            "all" -> true
          )
      }

      "must deserialise AllTaxYears" in {

        Json
          .obj(
            "all" -> true
          )
          .as[AllTaxYears.type] mustEqual AllTaxYears
      }

      "must fail when all is false" in {

        Json
          .obj(
            "all" -> false
          )
          .validate[AllTaxYears.type]
          .isError mustEqual true
      }
    }

    "VerificationTaxYearSelection JSON format" - {

      "must serialise TaxYear selection" in {

        val value: VerificationTaxYearSelection =
          TaxYear("2025 to 2026")

        Json.toJson(value) mustEqual
          Json.obj(
            "value" -> "2025 to 2026"
          )
      }

      "must serialise AllTaxYears selection" in {

        val value: VerificationTaxYearSelection =
          AllTaxYears

        Json.toJson(value) mustEqual
          Json.obj(
            "all" -> true
          )
      }

      "must deserialise TaxYear selection" in {

        Json
          .obj(
            "value" -> "2024 to 2025"
          )
          .as[VerificationTaxYearSelection] mustEqual
          TaxYear("2024 to 2025")
      }

      "must deserialise AllTaxYears selection" in {

        Json
          .obj(
            "all" -> true
          )
          .as[VerificationTaxYearSelection] mustEqual
          AllTaxYears
      }

      "must fail for invalid json" in {

        Json
          .obj(
            "all" -> false
          )
          .validate[VerificationTaxYearSelection]
          .isError mustEqual true
      }
    }
  }
}
