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

package models.history

import base.SpecBase
import play.api.libs.json.Json

class AmendmentHandoffDataSpec extends SpecBase {

  "AmendmentHandoffData" - {

    "must serialise and deserialise" in {
      val model = AmendmentHandoffData(
        instanceId = "1",
        taxYear = 2026,
        taxMonth = 4,
        returnType = "Standard",
        acceptedTime = Some("2026-04-20T21:49:19.702Z")
      )

      Json.toJson(model).as[AmendmentHandoffData] mustBe model
    }
  }
}
