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
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._

class GetClientListStatusResponseSpec extends AnyFreeSpec with Matchers {

  "GetClientListStatusResponse" - {

    "must serialize to JSON correctly" in {
      val model        = GetClientListStatusResponse("complete")
      val expectedJson = Json.obj("result" -> "complete")

      Json.toJson(model) mustEqual expectedJson
    }

    "must deserialize from JSON correctly" in {
      val json          = Json.obj("result" -> "processing")
      val expectedModel = GetClientListStatusResponse("processing")

      json.as[GetClientListStatusResponse] mustEqual expectedModel
    }

    "must round-trip correctly" in {
      val model  = GetClientListStatusResponse("pending")
      val json   = Json.toJson(model)
      val result = json.as[GetClientListStatusResponse]

      result mustEqual model
    }

    "must handle different status values" in {
      val statuses = Seq("complete", "processing", "pending", "failed", "error")

      statuses.foreach { status =>
        val model  = GetClientListStatusResponse(status)
        val json   = Json.toJson(model)
        val result = json.as[GetClientListStatusResponse]

        result.result mustEqual status
      }
    }

    "must fail to deserialize invalid JSON" in {
      val invalidJson = Json.obj("wrongField" -> "value")

      invalidJson.validate[GetClientListStatusResponse] mustBe a[JsError]
    }
  }
}
