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
import play.api.libs.json.Json

import java.time.LocalDateTime

class MonthlyReturnCompleteResponseSpec extends AnyWordSpec with Matchers {

  "CompleteSchemeData" should {
    "round-trip to/from JSON" in {
      val model = CompleteSchemeData(
        schemeId = 1,
        instanceId = "INST001",
        accountsOfficeReference = "123P",
        taxOfficeNumber = "123",
        taxOfficeReference = "ABC456",
        utr = Some("1234567890"),
        name = Some("Test Contractor"),
        emailAddress = Some("test@example.com")
      )
      Json.toJson(model).as[CompleteSchemeData] mustBe model
    }

    "round-trip with optional fields absent" in {
      val model = CompleteSchemeData(
        schemeId = 1,
        instanceId = "INST001",
        accountsOfficeReference = "123P",
        taxOfficeNumber = "123",
        taxOfficeReference = "ABC456"
      )
      Json.toJson(model).as[CompleteSchemeData] mustBe model
    }
  }

  "CompleteMonthlyReturnData" should {
    "round-trip to/from JSON" in {
      val model = CompleteMonthlyReturnData(
        monthlyReturnId = 100L,
        taxYear = 2024,
        taxMonth = 6,
        nilReturnIndicator = Some("N"),
        decInformationCorrect = Some("Y"),
        decNilReturnNoPayments = None,
        status = Some("SUBMITTED"),
        lastUpdate = Some(LocalDateTime.of(2024, 7, 1, 10, 30, 0)),
        amendment = Some("N"),
        supersededBy = None
      )
      Json.toJson(model).as[CompleteMonthlyReturnData] mustBe model
    }
  }

  "CompleteSubcontractorData" should {
    "round-trip to/from JSON" in {
      val model = CompleteSubcontractorData(
        subcontractorId = 200L,
        utr = Some("2345678901"),
        firstName = Some("John"),
        surname = Some("Smith"),
        tradingName = Some("Smith Builders"),
        subcontractorType = Some("Individual"),
        verificationNumber = Some("V12345"),
        taxTreatment = Some("Net"),
        displayName = Some("John Smith")
      )
      Json.toJson(model).as[CompleteSubcontractorData] mustBe model
    }
  }

  "CompleteMonthlyReturnItemData" should {
    "round-trip to/from JSON" in {
      val model = CompleteMonthlyReturnItemData(
        monthlyReturnId = 100L,
        monthlyReturnItemId = 300L,
        totalPayments = Some("5000.00"),
        costOfMaterials = Some("1000.00"),
        totalDeducted = Some("800.00"),
        subcontractorId = Some(200L),
        subcontractorName = Some("John Smith"),
        verificationNumber = Some("V12345")
      )
      Json.toJson(model).as[CompleteMonthlyReturnItemData] mustBe model
    }
  }

  "CompleteSubmissionData" should {
    "round-trip to/from JSON" in {
      val model = CompleteSubmissionData(
        submissionId = 400L,
        submissionType = "Original",
        activeObjectId = Some(100L),
        status = Some("Accepted"),
        hmrcMarkGenerated = Some("HMRC-123-ABC"),
        hmrcMarkGgis = None,
        emailRecipient = Some("user@example.com"),
        acceptedTime = Some("2024-07-01T10:30:00")
      )
      Json.toJson(model).as[CompleteSubmissionData] mustBe model
    }
  }

  "MonthlyReturnCompleteResponse" should {
    "round-trip to/from JSON" in {
      val model = MonthlyReturnCompleteResponse(
        scheme = Seq(CompleteSchemeData(1, "INST001", "123P", "123", "ABC456", Some("1234567890"), Some("Test Co"), None)),
        monthlyReturn = Seq(CompleteMonthlyReturnData(100L, 2024, 6, Some("N"), None, None, Some("SUBMITTED"), None, None, None)),
        subcontractors = Seq(CompleteSubcontractorData(200L, Some("2345678901"), Some("John"), Some("Smith"), None, None, None, None, None, None)),
        monthlyReturnItems = Seq(CompleteMonthlyReturnItemData(100L, 300L, Some("5000.00"), Some("1000.00"), Some("800.00"), Some(200L), Some("John Smith"), None)),
        submission = Seq(CompleteSubmissionData(400L, "Original", Some(100L), Some("Accepted"), Some("HMRC-123"), None, None, Some("2024-07-01T10:30:00")))
      )
      Json.toJson(model).as[MonthlyReturnCompleteResponse] mustBe model
    }

    "round-trip with empty sequences" in {
      val model = MonthlyReturnCompleteResponse(
        scheme = Seq.empty,
        monthlyReturn = Seq.empty,
        subcontractors = Seq.empty,
        monthlyReturnItems = Seq.empty,
        submission = Seq.empty
      )
      Json.toJson(model).as[MonthlyReturnCompleteResponse] mustBe model
    }
  }
}
