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

import base.SpecBase
import models.agent.ClientListStatus
import org.scalatest.freespec.AnyFreeSpec
import play.api.libs.json.*

class GetClientListStatusResponseSpec extends SpecBase {

  "GetClientListStatusResponse" - {

    "must deserialize from JSON correctly" in {
      val json = Json.obj(
        "result" -> "in-progress"
      )

      val expectedModel =
        GetClientListStatusResponse(ClientListStatus.InProgress)

      json.as[GetClientListStatusResponse] mustEqual expectedModel
    }

    "must deserialize all valid client list statuses" in {
      val statuses = Seq(
        "initiate-download" -> ClientListStatus.InitiateDownload,
        "in-progress"       -> ClientListStatus.InProgress,
        "succeeded"         -> ClientListStatus.Succeeded,
        "failed"            -> ClientListStatus.Failed
      )

      statuses.foreach { case (value, expectedStatus) =>
        val json = Json.obj(
          "result" -> value
        )

        json.as[GetClientListStatusResponse] mustEqual
          GetClientListStatusResponse(expectedStatus)
      }
    }

    "must fail to deserialize when result is missing" in {
      val json = Json.obj(
        "wrongField" -> "value"
      )

      json.validate[GetClientListStatusResponse] mustBe a[JsError]
    }

    "must fail to deserialize an invalid client list status" in {
      val json = Json.obj(
        "result" -> "invalid-status"
      )

      json.validate[GetClientListStatusResponse] mustBe a[JsError]
    }

    "must fail to deserialize result with an invalid type" in {
      val json = Json.obj(
        "result" -> 123
      )

      json.validate[GetClientListStatusResponse] mustBe a[JsError]
    }
  }
}
