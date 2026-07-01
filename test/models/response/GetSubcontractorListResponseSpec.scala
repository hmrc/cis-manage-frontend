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

import base.SpecBase
import play.api.libs.json.{JsArray, JsObject, JsString, Json}

import java.time.LocalDateTime

class GetSubcontractorListResponseSpec extends SpecBase {

  private val createDate =
    LocalDateTime.of(2026, 6, 1, 10, 30)

  private val verificationDate =
    LocalDateTime.of(2026, 6, 2, 11, 45)

  private val subcontractor =
    GetSubcontractor(
      subcontractorId = 123L,
      utr = Some("1234567890"),
      pageVisited = Some(2),
      partnerUtr = Some("0987654321"),
      crn = Some("CRN123456"),
      firstName = Some("Alan"),
      nino = Some("AB123456C"),
      secondName = Some("James"),
      surname = Some("Smith"),
      partnershipTradingName = Some("Alan Smith Partnership"),
      tradingName = Some("Alan Smith Builders"),
      subcontractorType = Some("soleTrader"),
      addressLine1 = Some("1 High Street"),
      addressLine2 = Some("Town Centre"),
      addressLine3 = Some("Bristol"),
      addressLine4 = Some("Avon"),
      country = Some("GB"),
      postcode = Some("BS1 1AA"),
      emailAddress = Some("alan.smith@example.com"),
      phoneNumber = Some("01171234567"),
      mobilePhoneNumber = Some("07123456789"),
      worksReferenceNumber = Some("WRN123"),
      createDate = Some(createDate),
      lastUpdate = Some(createDate),
      subbieResourceRef = Some(456L),
      matched = Some("Y"),
      autoVerified = Some("N"),
      verified = Some("Y"),
      verificationNumber = Some("V123456"),
      taxTreatment = Some("Gross"),
      verificationDate = Some(verificationDate),
      version = Some(1),
      updatedTaxTreatment = Some("Gross"),
      lastMonthlyReturnDate = Some(createDate),
      pendingVerifications = Some(0)
    )

  "GetSubcontractor" - {

    "must return first name and surname for a sole trader" in {
      subcontractor.displayName mustEqual "Alan Smith"
    }

    "must return surname when a sole trader has no first name" in {
      subcontractor
        .copy(firstName = None)
        .displayName mustEqual "Smith"
    }

    "must return trading name for a sole trader with no personal name" in {
      subcontractor
        .copy(
          firstName = None,
          surname = None
        )
        .displayName mustEqual "Alan Smith Builders"
    }

    "must return trading name for a company" in {
      subcontractor
        .copy(
          subcontractorType = Some("company"),
          firstName = None,
          surname = None
        )
        .displayName mustEqual "Alan Smith Builders"
    }

    "must return trading name for a trust" in {
      subcontractor
        .copy(
          subcontractorType = Some("trust"),
          firstName = None,
          surname = None
        )
        .displayName mustEqual "Alan Smith Builders"
    }

    "must return partnership trading name for a partnership" in {
      subcontractor
        .copy(
          subcontractorType = Some("partnership"),
          firstName = None,
          surname = None
        )
        .displayName mustEqual "Alan Smith Partnership"
    }

    "must return trading name when partnership trading name is unavailable" in {
      subcontractor
        .copy(
          subcontractorType = Some("partnership"),
          firstName = None,
          surname = None,
          partnershipTradingName = None
        )
        .displayName mustEqual "Alan Smith Builders"
    }

    "must return the default name where no applicable name is available" in {
      subcontractor
        .copy(
          subcontractorType = None,
          firstName = None,
          surname = None,
          tradingName = None
        )
        .displayName mustEqual "No name provided"
    }
  }

  "GetSubcontractorListResponse" - {

    "must write the response to JSON including displayName" in {
      val response =
        GetSubcontractorListResponse(
          subcontractors = Seq(subcontractor)
        )

      val json =
        GetSubcontractorListResponse.format.writes(response)

      val subcontractorsJson =
        (json \ "subcontractors").as[JsArray]

      val subcontractorJson =
        subcontractorsJson.value.head.as[JsObject]

      subcontractorJson.value("subcontractorId") mustEqual Json.toJson(123L)
      subcontractorJson.value("displayName") mustEqual JsString("Alan Smith")
    }

    "must read a response from JSON" in {
      val response =
        GetSubcontractorListResponse(
          subcontractors = Seq(subcontractor)
        )

      val json =
        Json.toJson(response)(
          using GetSubcontractorListResponse.format
        )

      json
        .as[GetSubcontractorListResponse](
          using GetSubcontractorListResponse.format
        ) mustEqual response
    }

    "must read an empty subcontractor list response" in {
      val json =
        Json.obj(
          "subcontractors" -> Json.arr()
        )

      json
        .as[GetSubcontractorListResponse](
          using GetSubcontractorListResponse.format
        ) mustEqual GetSubcontractorListResponse(
        subcontractors = Seq.empty
      )
    }
  }
}