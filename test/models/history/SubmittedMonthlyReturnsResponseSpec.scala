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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*

import java.time.Instant

class SubmittedMonthlyReturnsResponseSpec extends AnyWordSpec with Matchers {

  private val scheme = SchemeData("Scheme", "123", "AB456")
  private val mr     = MonthlyReturnData(1L, 2025, 4, "N", "SUBMITTED", Some(2L), Some("Y"), Some("items"))
  private val sub    = SubmissionData(
    5L,
    Some("MONTHLY"),
    Some(6L),
    "ACCEPTED",
    Some("mark"),
    Some("ggis"),
    Some("a@b.com"),
    Some(Instant.parse("2025-05-06T07:08:09Z"))
  )

  "SchemeData" should {
    "round-trip" in {
      Json.toJson(scheme).validate[SchemeData] mustBe JsSuccess(scheme)
    }
    "fail when a field is missing" in {
      Json.obj("name" -> "x").validate[SchemeData].isError mustBe true
    }
  }

  "MonthlyReturnData" should {
    "round-trip a fully populated value" in {
      Json.toJson(mr).validate[MonthlyReturnData] mustBe JsSuccess(mr)
    }
    "read optionals as None when absent" in {
      val json = Json.obj(
        "monthlyReturnId"    -> 1,
        "taxYear"            -> 2025,
        "taxMonth"           -> 4,
        "nilReturnIndicator" -> "N",
        "status"             -> "SUBMITTED"
      )
      json.as[MonthlyReturnData] mustBe MonthlyReturnData(1L, 2025, 4, "N", "SUBMITTED", None, None, None)
    }
    "fail when a required field is missing" in {
      Json.obj("monthlyReturnId" -> 1).validate[MonthlyReturnData].isError mustBe true
    }
  }

  "SubmissionData" should {
    "round-trip a fully populated value" in {
      Json.toJson(sub).validate[SubmissionData] mustBe JsSuccess(sub)
    }
    "read optionals as None when absent" in {
      Json.obj("submissionId" -> 5, "status" -> "ACCEPTED").as[SubmissionData] mustBe
        SubmissionData(5L, None, None, "ACCEPTED", None, None, None, None)
    }
    "fail when status is missing" in {
      Json.obj("submissionId" -> 5).validate[SubmissionData].isError mustBe true
    }
  }

  "SubmittedMonthlyReturnsResponse" should {
    "round-trip with data" in {
      val model = SubmittedMonthlyReturnsResponse(scheme, Seq(mr), Seq(sub))
      Json.toJson(model).validate[SubmittedMonthlyReturnsResponse] mustBe JsSuccess(model)
    }
    "round-trip with empty lists" in {
      val model = SubmittedMonthlyReturnsResponse(scheme, Nil, Nil)
      Json.toJson(model).as[SubmittedMonthlyReturnsResponse] mustBe model
    }
    "fail when scheme is missing" in {
      Json.obj("monthlyReturns" -> Json.arr(), "submissions" -> Json.arr())
        .validate[SubmittedMonthlyReturnsResponse]
        .isError mustBe true
    }
  }
}
