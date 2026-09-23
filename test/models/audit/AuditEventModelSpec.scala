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

package models.audit

import base.SpecBase
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.libs.json.{JsValue, Json}

class AuditEventModelSpec extends SpecBase {

  "AuthFailureAuditEventModel" - {
    val underTest = AuthFailureAuditEventModel()
    "must have the correct auditType" in {
      underTest.auditType mustBe "AuthoriseServiceGuardFailure"
    }
    "must have an empty detailJson" in {
      underTest.detailJson mustBe Json.obj()
    }
  }

  "ClientDetailsRetrievedAuditEventModel" - {
    val underTest = ClientDetailsRetrievedAuditEventModel(
      agentReference = "ARN123",
      taxOfficeNumber = "123",
      taxOfficeReference = "AB456"
    )
    "must have the correct auditType" in {
      underTest.auditType mustBe "ClientDetailsRetrieved"
    }
    "must serialise correctly" in {
      underTest.detailJson mustBe Json.obj(
        "agentReference"     -> "ARN123",
        "taxOfficeNumber"    -> "123",
        "taxOfficeReference" -> "AB456"
      )
    }
  }

  "DeleteSubcontractorAuditEventModel" - {
    "must have the correct auditType" in {
      DeleteSubcontractorAuditEventModel(
        cisId = "123/AB456",
        subcontractorName = "Test Subcontractor",
        subbieResourceRef = 42L
      ).auditType mustBe "DeleteSubcontractor"
    }
    "must serialise correctly without typeOfSubcontractor" in {
      DeleteSubcontractorAuditEventModel(
        cisId = "123/AB456",
        subcontractorName = "Test Subcontractor",
        subbieResourceRef = 42L
      ).detailJson mustBe Json.obj(
        "cisId"             -> "123/AB456",
        "subcontractorName" -> "Test Subcontractor",
        "subbieResourceRef" -> 42L
      )
    }
    "must serialise correctly with typeOfSubcontractor" in {
      DeleteSubcontractorAuditEventModel(
        cisId = "123/AB456",
        subcontractorName = "Test Subcontractor",
        subbieResourceRef = 42L,
        typeOfSubcontractor = Some("soletrader")
      ).detailJson mustBe Json.obj(
        "cisId"               -> "123/AB456",
        "subcontractorName"   -> "Test Subcontractor",
        "subbieResourceRef"   -> 42L,
        "typeOfSubcontractor" -> "soletrader"
      )
    }
  }

  "extendedDataEvent" - {
    val testAuditType: String   = "test-audit-type"
    val testDetailJson: JsValue = Json.toJson(testAuditType)
    "behave as expected" in {
      val event    = new AuditEventModel {
        override val auditType: String   = testAuditType
        override val detailJson: JsValue = testDetailJson
      }
      val extended = event.extendedDataEvent
      extended.auditType shouldBe testAuditType
      extended.detail    shouldBe testDetailJson
    }
  }

}
