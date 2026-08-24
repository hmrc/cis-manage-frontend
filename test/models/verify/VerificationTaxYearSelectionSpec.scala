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

import models.verify.VerificationTaxYearSelection.*
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.*

class VerificationTaxYearSelectionSpec
    extends AnyFreeSpec
    with Matchers
    with OptionValues
    with ScalaCheckPropertyChecks {
  import org.scalacheck.*

  given Arbitrary[VerificationTaxYearSelection] = Arbitrary {
    Gen.option(Gen.chooseNum(0, 999999)).map {
      case Some(startYear) => TaxYear(startYear)
      case None            => AllTaxYears
    }
  }

  "VerificationTaxYearSelection" - {

    "fromString" - {

      "must return AllTaxYears when value is all" in {

        VerificationTaxYearSelection.fromString("all") mustEqual AllTaxYears
      }

      "must return TaxYear when value is a start year" in {

        VerificationTaxYearSelection
          .fromString("2026") mustEqual
          TaxYear(2026)
      }
    }

    "TaxYearPeriod" - {

      "must derive end year and display value from start year" in {

        TaxYearPeriod(2026).endYear mustEqual 2027
        TaxYearPeriod(2026).toString mustEqual "2026 to 2027"
      }
    }

    "TaxYear JSON format" - {

      "must serialise TaxYear" in {

        Json.toJson(TaxYear(2026)) mustEqual
          Json.obj(
            "startYear" -> 2026
          )
      }

      "must deserialise TaxYear" in {

        Json
          .obj(
            "startYear" -> 2026
          )
          .as[TaxYear] mustEqual
          TaxYear(2026)
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
          TaxYear(2025)

        Json.toJson(value) mustEqual
          Json.obj(
            "startYear" -> 2025
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
            "startYear" -> 2024
          )
          .as[VerificationTaxYearSelection] mustEqual
          TaxYear(2024)
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

    "path decoder" - {
      "must recover an path-encoded tax year selection" in forAll { (givenSelection: VerificationTaxYearSelection) =>
        val encodedSelection = givenSelection.toPath
        val decodedSelection = VerificationTaxYearSelection fromPath encodedSelection
        decodedSelection.value mustBe givenSelection
      }

      val invalidPaths = Table(
        "Scenario"                           -> "Invalid Path",
        "start year isn't a number"          -> "asdf-to-2000",
        "end year isn't a number"            -> "1999-to-ghjk",
        "start year isn't 1 before end year" -> "1999-to-2001"
      )

      forAll(invalidPaths) { (scenario, invalidPath) =>
        val result = VerificationTaxYearSelection fromPath invalidPath
        result mustBe empty
      }
    }
  }
}
