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

package models.verify

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*

import java.time.{LocalDate, LocalDateTime}

class VerificationDataSpec extends AnyWordSpec with Matchers {

  private val subVerif = SubcontractorVerificationData("Sub One", "V123")
  private val dt       = LocalDateTime.of(2025, 5, 6, 7, 8, 9)

  private val request = VerificationRequestData(
    1L,
    "V999",
    LocalDate.of(2025, 5, 6),
    "ACCEPTED",
    2025,
    dt,
    Some("Contractor"),
    "123/AB456",
    "RCPT1",
    Seq(subVerif)
  )

  "SubcontractorVerificationData" should {
    "round-trip" in {
      Json.toJson(subVerif).validate[SubcontractorVerificationData] mustBe JsSuccess(subVerif)
    }
    "fail when a field is missing" in {
      Json.obj("name" -> "x").validate[SubcontractorVerificationData].isError mustBe true
    }
  }

  "VerificationRequestDetailData" should {
    "round-trip" in {
      val model = VerificationRequestDetailData("V1", dt, Seq(subVerif), Seq(subVerif.copy(name = "Two")))
      Json.toJson(model).validate[VerificationRequestDetailData] mustBe JsSuccess(model)
    }
    "round-trip with empty lists" in {
      val model = VerificationRequestDetailData("V1", dt, Nil, Nil)
      Json.toJson(model).as[VerificationRequestDetailData] mustBe model
    }
    "fail when the date is invalid" in {
      Json
        .obj(
          "verificationNumber"       -> "V1",
          "dateTimeSubmitted"        -> "not-a-date",
          "subcontractorsToVerify"   -> Json.arr(),
          "subcontractorsToReverify" -> Json.arr()
        )
        .validate[VerificationRequestDetailData]
        .isError mustBe true
    }
  }

  "VerificationRequestData" should {
    "round-trip a fully populated value" in {
      Json.toJson(request).validate[VerificationRequestData] mustBe JsSuccess(request)
    }
    "round-trip with no contractor name" in {
      val model = request.copy(contractorName = None)
      Json.toJson(model).as[VerificationRequestData] mustBe model
    }
    "fail when a required field is missing" in {
      Json.obj("verificationBatchId" -> 1).validate[VerificationRequestData].isError mustBe true
    }
  }

  "VerificationHistoryData" should {
    "round-trip with requests" in {
      val model = VerificationHistoryData(Seq(request))
      Json.toJson(model).validate[VerificationHistoryData] mustBe JsSuccess(model)
    }
    "round-trip empty" in {
      Json.toJson(VerificationHistoryData(Nil)).as[VerificationHistoryData] mustBe VerificationHistoryData(Nil)
    }
    "fail when the list is missing" in {
      Json.obj().validate[VerificationHistoryData].isError mustBe true
    }
  }
}
