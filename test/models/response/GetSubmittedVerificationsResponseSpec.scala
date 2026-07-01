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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.{Instant, LocalDateTime}

class GetSubmittedVerificationsResponseSpec extends AnyFreeSpec with Matchers {

  "GetSubmittedVerificationsResponse JSON format" - {

    "must write and read every field in the submitted verifications response" in {
      val response = GetSubmittedVerificationsResponse(
        scheme = Seq(
          GetSubmittedContractorScheme(
            schemeId = 1,
            instanceId = "900063",
            accountsOfficeReference = "123PA000001",
            taxOfficeNumber = "123",
            taxOfficeReference = "AB123456",
            utr = Some("1234567890"),
            name = Some("Gary Construction Ltd"),
            emailAddress = Some("gary@example.com"),
            displayWelcomePage = Some("Y"),
            prePopCount = Some(10),
            prePopSuccessful = Some("Y"),
            subcontractorCounter = Some(15),
            verificationBatchCounter = Some(5),
            createDate = Some(Instant.parse("2026-06-01T10:15:30Z")),
            lastUpdate = Some(Instant.parse("2026-06-02T11:20:40Z")),
            version = Some(3)
          )
        ),
        subcontractors = Seq(
          GetSubmittedSubcontractor(
            subcontractorId = 2L,
            utr = Some("1234567890"),
            pageVisited = Some(4),
            partnerUtr = Some("0987654321"),
            crn = Some("CRN123456"),
            firstName = Some("Gary"),
            nino = Some("QQ123456C"),
            secondName = Some("John"),
            surname = Some("Aghedo"),
            partnershipTradingName = Some("Gary and Sons"),
            tradingName = Some("Gary Construction Ltd"),
            subcontractorType = Some("soleTrader"),
            addressLine1 = Some("1 Test Street"),
            addressLine2 = Some("Test Area"),
            addressLine3 = Some("Test Town"),
            addressLine4 = Some("Test County"),
            country = Some("GB"),
            postcode = Some("AB1 2CD"),
            emailAddress = Some("subcontractor@example.com"),
            phoneNumber = Some("01234567890"),
            mobilePhoneNumber = Some("07123456789"),
            worksReferenceNumber = Some("WORKS-001"),
            createDate = Some(LocalDateTime.of(2026, 6, 1, 10, 15, 30)),
            lastUpdate = Some(LocalDateTime.of(2026, 6, 2, 11, 20, 40)),
            subbieResourceRef = Some(100L),
            matched = Some("Y"),
            autoVerified = Some("N"),
            verified = Some("Y"),
            verificationNumber = Some("V0004528765"),
            taxTreatment = Some("Gross"),
            verificationDate = Some(LocalDateTime.of(2026, 6, 3, 12, 25, 50)),
            version = Some(4),
            updatedTaxTreatment = Some("Net"),
            lastMonthlyReturnDate = Some(LocalDateTime.of(2026, 5, 6, 9, 10, 11)),
            pendingVerifications = Some(2)
          )
        ),
        verificationBatches = Seq(
          GetSubmittedVerificationBatch(
            verificationBatchId = 3L,
            schemeId = 1L,
            verificationsCounter = Some(8L),
            verifBatchResourceRef = Some(200L),
            proceedSession = Some("Y"),
            confirmArrangement = Some("Y"),
            confirmCorrect = Some("Y"),
            status = Some("Submitted"),
            verificationNumber = Some("V0004528765"),
            createDate = Some(LocalDateTime.of(2026, 6, 4, 10, 15, 30)),
            lastUpdate = Some(LocalDateTime.of(2026, 6, 5, 11, 20, 40)),
            version = Some(5)
          )
        ),
        verifications = Seq(
          GetSubmittedVerification(
            verificationId = 4L,
            matched = Some("Y"),
            verificationNumber = Some("V0004528765"),
            taxTreatment = Some("Gross"),
            actionIndicator = Some("A"),
            verificationBatchId = Some(3L),
            schemeId = Some(1L),
            subcontractorId = Some(2L),
            subcontractorName = Some("Gary Aghedo"),
            verificationResourceRef = Some(300L),
            proceed = Some("Y"),
            createDate = Some(LocalDateTime.of(2026, 6, 6, 10, 15, 30)),
            lastUpdate = Some(LocalDateTime.of(2026, 6, 7, 11, 20, 40)),
            version = Some(6)
          )
        ),
        submissions = Seq(
          GetSubmittedSubmission(
            submissionId = 5L,
            submissionType = "Verification",
            activeObjectId = Some(3L),
            status = Some("Submitted"),
            hmrcMarkGenerated = Some("Y"),
            hmrcMarkGgis = Some("GGIS-MARK"),
            emailRecipient = Some("recipient@example.com"),
            acceptedTime = Some("2026-06-08T10:15:30"),
            createDate = Some(LocalDateTime.of(2026, 6, 8, 10, 15, 30)),
            lastUpdate = Some(LocalDateTime.of(2026, 6, 9, 11, 20, 40)),
            schemeId = 1L,
            agentId = Some("AGENT-001"),
            l_Migrated = Some(1L),
            submissionRequestDate = Some(LocalDateTime.of(2026, 6, 10, 12, 25, 50)),
            govTalkErrorCode = Some("5001"),
            govTalkErrorType = Some("fatal"),
            govTalkErrorMessage = Some("GovTalk processing failed")
          )
        )
      )

      val expectedJson = Json.obj(
        "scheme"              -> Json.arr(
          Json.obj(
            "schemeId"                 -> 1,
            "instanceId"               -> "900063",
            "accountsOfficeReference"  -> "123PA000001",
            "taxOfficeNumber"          -> "123",
            "taxOfficeReference"       -> "AB123456",
            "utr"                      -> "1234567890",
            "name"                     -> "Gary Construction Ltd",
            "emailAddress"             -> "gary@example.com",
            "displayWelcomePage"       -> "Y",
            "prePopCount"              -> 10,
            "prePopSuccessful"         -> "Y",
            "subcontractorCounter"     -> 15,
            "verificationBatchCounter" -> 5,
            "createDate"               -> "2026-06-01T10:15:30Z",
            "lastUpdate"               -> "2026-06-02T11:20:40Z",
            "version"                  -> 3
          )
        ),
        "subcontractors"      -> Json.arr(
          Json.obj(
            "subcontractorId"        -> 2,
            "utr"                    -> "1234567890",
            "pageVisited"            -> 4,
            "partnerUtr"             -> "0987654321",
            "crn"                    -> "CRN123456",
            "firstName"              -> "Gary",
            "nino"                   -> "QQ123456C",
            "secondName"             -> "John",
            "surname"                -> "Aghedo",
            "partnershipTradingName" -> "Gary and Sons",
            "tradingName"            -> "Gary Construction Ltd",
            "subcontractorType"      -> "soleTrader",
            "addressLine1"           -> "1 Test Street",
            "addressLine2"           -> "Test Area",
            "addressLine3"           -> "Test Town",
            "addressLine4"           -> "Test County",
            "country"                -> "GB",
            "postcode"               -> "AB1 2CD",
            "emailAddress"           -> "subcontractor@example.com",
            "phoneNumber"            -> "01234567890",
            "mobilePhoneNumber"      -> "07123456789",
            "worksReferenceNumber"   -> "WORKS-001",
            "createDate"             -> "2026-06-01T10:15:30",
            "lastUpdate"             -> "2026-06-02T11:20:40",
            "subbieResourceRef"      -> 100,
            "matched"                -> "Y",
            "autoVerified"           -> "N",
            "verified"               -> "Y",
            "verificationNumber"     -> "V0004528765",
            "taxTreatment"           -> "Gross",
            "verificationDate"       -> "2026-06-03T12:25:50",
            "version"                -> 4,
            "updatedTaxTreatment"    -> "Net",
            "lastMonthlyReturnDate"  -> "2026-05-06T09:10:11",
            "pendingVerifications"   -> 2,
            "displayName"            -> "Gary Aghedo"
          )
        ),
        "verificationBatches" -> Json.arr(
          Json.obj(
            "verificationBatchId"   -> 3,
            "schemeId"              -> 1,
            "verificationsCounter"  -> 8,
            "verifBatchResourceRef" -> 200,
            "proceedSession"        -> "Y",
            "confirmArrangement"    -> "Y",
            "confirmCorrect"        -> "Y",
            "status"                -> "Submitted",
            "verificationNumber"    -> "V0004528765",
            "createDate"            -> "2026-06-04T10:15:30",
            "lastUpdate"            -> "2026-06-05T11:20:40",
            "version"               -> 5
          )
        ),
        "verifications"       -> Json.arr(
          Json.obj(
            "verificationId"          -> 4,
            "matched"                 -> "Y",
            "verificationNumber"      -> "V0004528765",
            "taxTreatment"            -> "Gross",
            "actionIndicator"         -> "A",
            "verificationBatchId"     -> 3,
            "schemeId"                -> 1,
            "subcontractorId"         -> 2,
            "subcontractorName"       -> "Gary Aghedo",
            "verificationResourceRef" -> 300,
            "proceed"                 -> "Y",
            "createDate"              -> "2026-06-06T10:15:30",
            "lastUpdate"              -> "2026-06-07T11:20:40",
            "version"                 -> 6
          )
        ),
        "submissions"         -> Json.arr(
          Json.obj(
            "submissionId"          -> 5,
            "submissionType"        -> "Verification",
            "activeObjectId"        -> 3,
            "status"                -> "Submitted",
            "hmrcMarkGenerated"     -> "Y",
            "hmrcMarkGgis"          -> "GGIS-MARK",
            "emailRecipient"        -> "recipient@example.com",
            "acceptedTime"          -> "2026-06-08T10:15:30",
            "createDate"            -> "2026-06-08T10:15:30",
            "lastUpdate"            -> "2026-06-09T11:20:40",
            "schemeId"              -> 1,
            "agentId"               -> "AGENT-001",
            "l_Migrated"            -> 1,
            "submissionRequestDate" -> "2026-06-10T12:25:50",
            "govTalkErrorCode"      -> "5001",
            "govTalkErrorType"      -> "fatal",
            "govTalkErrorMessage"   -> "GovTalk processing failed"
          )
        )
      )

      val json = Json.toJson(response)

      json mustBe expectedJson
      json.as[GetSubmittedVerificationsResponse] mustBe response
    }
  }
}
