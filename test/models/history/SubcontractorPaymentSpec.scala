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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsSuccess, Json}

class SubcontractorPaymentSpec extends AnyFreeSpec with Matchers {

  "SubcontractorPayment JSON format" - {

    "must write to JSON and read back (round-trip)" in {
      val model = SubcontractorPayment(
        name = "BuildRight Construction",
        verificationNumber = "V1234567890",
        paymentsMade = "£165",
        costOfMaterials = "£95",
        taxDeducted = "£95"
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "name"               -> "BuildRight Construction",
        "verificationNumber" -> "V1234567890",
        "paymentsMade"       -> "£165",
        "costOfMaterials"    -> "£95",
        "taxDeducted"        -> "£95"
      )

      json.as[SubcontractorPayment] mustBe model
    }

    "must read valid JSON" in {
      val json = Json.obj(
        "name"               -> "Northern Trades Ltd",
        "verificationNumber" -> "V0987654321",
        "paymentsMade"       -> "£75",
        "costOfMaterials"    -> "£55",
        "taxDeducted"        -> "£55"
      )

      json.validate[SubcontractorPayment] mustBe JsSuccess(
        SubcontractorPayment(
          name = "Northern Trades Ltd",
          verificationNumber = "V0987654321",
          paymentsMade = "£75",
          costOfMaterials = "£55",
          taxDeducted = "£55"
        )
      )
    }

    "must read valid JSON when verification number is empty" in {
      val json = Json.obj(
        "name"               -> "TyneWear Ltd",
        "verificationNumber" -> "",
        "paymentsMade"       -> "£165",
        "costOfMaterials"    -> "£125",
        "taxDeducted"        -> "£55"
      )

      json.validate[SubcontractorPayment] mustBe JsSuccess(
        SubcontractorPayment(
          name = "TyneWear Ltd",
          verificationNumber = "",
          paymentsMade = "£165",
          costOfMaterials = "£125",
          taxDeducted = "£55"
        )
      )
    }

    "must fail when required fields are missing" in {
      val json = Json.obj(
        "name" -> "TyneWear Ltd"
      )

      json.validate[SubcontractorPayment] mustBe a[JsError]
    }

    "must serialise a sequence of subcontractors" in {
      val list = Seq(
        SubcontractorPayment(
          name = "A",
          verificationNumber = "V1111111111",
          paymentsMade = "£1",
          costOfMaterials = "£2",
          taxDeducted = "£3"
        ),
        SubcontractorPayment(
          name = "B",
          verificationNumber = "",
          paymentsMade = "£4",
          costOfMaterials = "£5",
          taxDeducted = "£6"
        )
      )

      val json = Json.toJson(list)

      json mustBe Json.arr(
        Json.obj(
          "name"               -> "A",
          "verificationNumber" -> "V1111111111",
          "paymentsMade"       -> "£1",
          "costOfMaterials"    -> "£2",
          "taxDeducted"        -> "£3"
        ),
        Json.obj(
          "name"               -> "B",
          "verificationNumber" -> "",
          "paymentsMade"       -> "£4",
          "costOfMaterials"    -> "£5",
          "taxDeducted"        -> "£6"
        )
      )

      json.as[Seq[SubcontractorPayment]] mustBe list
    }
  }
}
