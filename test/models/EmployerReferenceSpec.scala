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
import play.api.libs.json.Json

class EmployerReferenceSpec extends SpecBase {
  "EmployerReference" - {
    "serialize to JSON correctly" in {
      val employerRef = EmployerReference("123", "AB456")
      val json = Json.toJson(employerRef)
      (json \ "taxOfficeNumber").as[String] mustBe "123"
      (json \ "taxOfficeReference").as[String] mustBe "AB456"
    }
    "deserialize from JSON correctly" in {
      val json = Json.parse(
        """
          |{
          |  "taxOfficeNumber": "987",
          |  "taxOfficeReference": "CD789"
          |}
          |""".stripMargin
      )
      val result = json.as[EmployerReference]
      result.taxOfficeNumber mustBe "987"
      result.taxOfficeReference mustBe "CD789"
    }
    "round-trip serialize and deserialize correctly" in {
      val employerRef = EmployerReference("321", "XY123")
      val json = Json.toJson(employerRef)
      val result = json.as[EmployerReference]
      result mustBe employerRef
    }
  }
}