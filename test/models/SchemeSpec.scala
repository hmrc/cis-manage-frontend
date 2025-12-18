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

package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

class SchemeSpec extends AnyFreeSpec with Matchers {

  "Scheme JSON" - {

    "round-trips" in {
      val model = Scheme(123, "CIS-123", Some("1234567890"), Some("Test Ltd"), Some("Y"), Some(2))
      Json.toJson(model).as[Scheme] shouldBe model
    }

    "reads when optional fields are missing" in {
      val json = Json.obj(
        "schemeId"   -> 123,
        "instanceId" -> "CIS-123"
      )

      json.as[Scheme] shouldBe Scheme(123, "CIS-123", None, None, None, None)
    }
  }
}
