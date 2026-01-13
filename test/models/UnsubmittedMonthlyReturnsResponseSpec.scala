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

package models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

import java.time.LocalDateTime

class UnsubmittedMonthlyReturnsResponseSpec extends AnyWordSpec with Matchers {

  "UnsubmittedMonthlyReturnsRow" should {
    "round-trip to/from JSON" in {
      val model = UnsubmittedMonthlyReturnsRow(
        taxYear = 2025,
        taxMonth = 1,
        returnType = "Nil",
        status = "PENDING",
        lastUpdate = Some(LocalDateTime.parse("2025-01-01T00:00:00"))
      )

      Json.toJson(model).as[UnsubmittedMonthlyReturnsRow] mustBe model
    }
  }

  "UnsubmittedMonthlyReturnsResponse" should {
    "round-trip to/from JSON" in {
      val model = UnsubmittedMonthlyReturnsResponse(
        unsubmittedCisReturns = Seq(
          UnsubmittedMonthlyReturnsRow(2025, 1, "Nil", "PENDING", None)
        )
      )

      Json.toJson(model).as[UnsubmittedMonthlyReturnsResponse] mustBe model
    }
  }
}
