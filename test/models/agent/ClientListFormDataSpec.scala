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

package models.agent

import base.SpecBase
import play.api.libs.json.Json

class ClientListFormDataSpec extends SpecBase {

  "ClientListFormData should serialize to JSON" in {
    val data = ClientListFormData("CN", "ABC Limited")
    val json = Json.toJson(data)
    assert((json \ "searchBy").as[String] === "CN")
    assert((json \ "searchFilter").as[String] === "ABC Limited")
  }

  "ClientListFormData should deserialize from JSON" in {
    val json = Json.parse("""
        |{
        |  "searchBy": "ER",
        |  "searchFilter": "1234567890"
        |}
        |""".stripMargin)
    val data = json.as[ClientListFormData]
    assert(data.searchBy === "ER")
    assert(data.searchFilter === "1234567890")
  }

  "ClientListFormData should serialize and deserialize consistently" in {
    val original = ClientListFormData("CR", "ABC/123456")
    val json     = Json.toJson(original)
    val parsed   = json.as[ClientListFormData]
    assert(parsed === original)
  }
}
