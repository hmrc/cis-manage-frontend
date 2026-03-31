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

package models

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

import java.time.Instant

class SubmittedReturnsDataSpec extends AnyWordSpec with Matchers {

  "SubmittedReturnsData format" should {
    "serialize and deserialize correctly" in {
      val data = SubmittedReturnsData(
        scheme = SubmittedSchemeData(
          name = "Test Scheme",
          taxOfficeNumber = "123",
          taxOfficeReference = "AB456"
        ),
        monthlyReturn = Seq(
          SubmittedMonthlyReturnData(
            monthlyReturnId = 1L,
            taxYear = 2024,
            taxMonth = 3,
            nilReturnIndicator = "Y",
            status = "Submitted",
            supersededBy = None,
            amendmentStatus = Some("None"),
            monthlyReturnItems = Some("items")
          )
        ),
        submissions = Seq(
          SubmittedSubmissionData(
            submissionId = 10L,
            submissionType = Some("Original"),
            activeObjectId = 20L,
            status = "Accepted",
            hmrcMarkGenerated = Some("mark1"),
            hmrcMarkGgis = Some("ggis1"),
            emailRecipient = Some("test@example.com"),
            acceptedTime = Some(Instant.parse("2024-03-01T12:00:00Z"))
          )
        )
      )

      val json = Json.toJson(data)
      json.as[SubmittedReturnsData] shouldBe data
    }
  }
}
