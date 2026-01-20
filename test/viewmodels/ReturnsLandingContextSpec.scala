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

package viewmodels

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ReturnsLandingContextSpec extends AnyWordSpec with Matchers {

  "ReturnsLandingContext" should {

    "hold the values passed to it" in {
      val returns = Seq(
        ReturnLandingViewModel("August 2025", "Standard", "19 September 2025", "Accepted")
      )

      val context = ReturnsLandingContext(
        contractorName = "ABC Construction Ltd",
        standardReturnLink = "/standard",
        nilReturnLink = "/nil",
        returnsList = returns
      )

      context.contractorName mustBe "ABC Construction Ltd"
      context.standardReturnLink mustBe "/standard"
      context.nilReturnLink mustBe "/nil"
      context.returnsList mustBe returns
    }

    "support copy with updated fields" in {
      val context = ReturnsLandingContext(
        contractorName = "ABC",
        standardReturnLink = "/standard",
        nilReturnLink = "/nil",
        returnsList = Seq.empty
      )

      val updated = context.copy(contractorName = "NEW")

      updated.contractorName mustBe "NEW"
      updated.standardReturnLink mustBe "/standard"
      updated.nilReturnLink mustBe "/nil"
      updated.returnsList mustBe Seq.empty
    }
  }
}
