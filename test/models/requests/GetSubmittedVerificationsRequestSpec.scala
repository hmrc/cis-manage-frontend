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

package models.requests

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class GetSubmittedVerificationsRequestSpec extends AnyFreeSpec with Matchers {

  "GetSubmittedVerificationsRequest" - {

    "must write the instanceId to JSON" in {
      val request = GetSubmittedVerificationsRequest(
        instanceId = "900063"
      )

      Json.toJson(request) mustBe Json.obj(
        "instanceId" -> "900063"
      )
    }

    "must read the instanceId from JSON" in {
      val json = Json.obj(
        "instanceId" -> "900063"
      )

      json.as[GetSubmittedVerificationsRequest] mustBe
        GetSubmittedVerificationsRequest(
          instanceId = "900063"
        )
    }

    "must round trip to and from JSON" in {
      val request = GetSubmittedVerificationsRequest(
        instanceId = "900063"
      )

      Json.toJson(request).as[GetSubmittedVerificationsRequest] mustBe request
    }
  }
}
