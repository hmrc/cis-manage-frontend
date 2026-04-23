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

package models.response

import models.history.{SubmittedSchemeData, SubmittedSubmissionData}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

import java.time.Instant

class GetSubmittedMonthlyReturnsDataResponseSpec extends AnyWordSpec with Matchers {

  "GetSubmittedMonthlyReturnsDataResponse JSON format" should {

    "serialize and deserialize correctly" in {
      val model = GetSubmittedMonthlyReturnsDataResponse(
        scheme = SubmittedSchemeData("Scheme Name", "163", "AB0063"),
        monthlyReturnId = 3000L,
        taxYear = 2025,
        taxMonth = 1,
        returnType = "Nil",
        monthlyReturnItems = Seq.empty,
        submission = SubmittedSubmissionData(
          submissionId = 10L,
          submissionType = Some("Original"),
          activeObjectId = Some(20L),
          status = "Accepted",
          hmrcMarkGenerated = Some("mark1"),
          hmrcMarkGgis = Some("ggis1"),
          emailRecipient = Some("test@example.com"),
          acceptedTime = Some(Instant.now())
        )
      )

      val json = Json.toJson(model)
      json.as[GetSubmittedMonthlyReturnsDataResponse] mustBe model
    }
  }
}
