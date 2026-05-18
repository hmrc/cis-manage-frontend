/*
 * Copyright 2025 HM Revenue & Customs
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

package generators

import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryVerificationHistorySelectTaxYear: Arbitrary[String] =
    Arbitrary {
      Gen.oneOf(
        "all",
        "2026 to 2027 (current tax year)",
        "2025 to 2026",
        "2024 to 2025"
      )
    }

  implicit def arbitrarySubmittedReturnsChooseTaxYear(implicit taxYears: Seq[String]): Arbitrary[String] =
    Arbitrary(Gen.oneOf(taxYears :+ "all"))
}
