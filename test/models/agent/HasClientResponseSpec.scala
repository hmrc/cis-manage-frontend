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

package models.agent

import base.SpecBase
import play.api.libs.json.{JsError, Json}

class HasClientResponseSpec extends SpecBase {

  "HasClientResponse" - {

    "must deserialize from JSON correctly" in {
      val json = Json.obj(
        "hasClient" -> true
      )

      json.as[HasClientResponse] mustEqual HasClientResponse(hasClient = true)
    }

    "must fail to deserialize invalid JSON" in {
      val json = Json.obj(
        "wrongField" -> true
      )

      json.validate[HasClientResponse] mustBe a[JsError]
    }
  }
}
