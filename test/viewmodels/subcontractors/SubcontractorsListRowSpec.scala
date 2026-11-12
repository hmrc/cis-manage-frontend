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

package viewmodels.subcontractors

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*

class SubcontractorsListRowSpec extends AnyWordSpec with Matchers {

  "TaxTreatment format" should {
    TaxTreatment.values.foreach { t =>
      s"round-trip $t" in {
        Json.toJson(t) mustBe JsString(t.toString)
        JsString(t.toString).as[TaxTreatment] mustBe t
      }
    }
    "reject an unknown value" in {
      JsString("Other").validate[TaxTreatment].isError mustBe true
    }
    "reject a non-string value" in {
      JsNumber(1).validate[TaxTreatment].isError mustBe true
    }
  }

  "SubcontractorsListRow format" should {
    val row = SubcontractorsListRow(
      "1",
      "Name",
      "1234567890",
      verified = true,
      "V1",
      TaxTreatment.Gross,
      "1 Jan 2025",
      99L,
      "/amend/1"
    )

    "round-trip" in {
      Json.toJson(row).validate[SubcontractorsListRow] mustBe JsSuccess(row)
    }
    "fail with an invalid tax treatment" in {
      val json = Json.toJson(row).as[JsObject] + ("taxTreatment" -> JsString("Bad"))
      json.validate[SubcontractorsListRow].isError mustBe true
    }
    "fail when a field is missing" in {
      Json.obj("id" -> "1").validate[SubcontractorsListRow].isError mustBe true
    }
  }
}
