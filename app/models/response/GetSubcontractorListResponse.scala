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

import play.api.libs.json.{JsString, Json, OFormat, Reads, Writes}

import java.time.LocalDateTime

final case class GetSubcontractorListResponse(
  subcontractors: Seq[GetSubcontractor]
)

object GetSubcontractorListResponse {
  given format: OFormat[GetSubcontractorListResponse] =
    Json.format[GetSubcontractorListResponse]
}

final case class GetSubcontractor(
  subcontractorId: Long,
  utr: Option[String],
  pageVisited: Option[Int],
  partnerUtr: Option[String],
  crn: Option[String],
  firstName: Option[String],
  nino: Option[String],
  secondName: Option[String],
  surname: Option[String],
  partnershipTradingName: Option[String],
  tradingName: Option[String],
  subcontractorType: Option[String],
  addressLine1: Option[String],
  addressLine2: Option[String],
  addressLine3: Option[String],
  addressLine4: Option[String],
  country: Option[String],
  postcode: Option[String],
  emailAddress: Option[String],
  phoneNumber: Option[String],
  mobilePhoneNumber: Option[String],
  worksReferenceNumber: Option[String],
  createDate: Option[LocalDateTime],
  lastUpdate: Option[LocalDateTime],
  subbieResourceRef: Option[Long],
  matched: Option[String],
  autoVerified: Option[String],
  verified: Option[String],
  verificationNumber: Option[String],
  taxTreatment: Option[String],
  verificationDate: Option[LocalDateTime],
  version: Option[Int],
  updatedTaxTreatment: Option[String],
  lastMonthlyReturnDate: Option[LocalDateTime],
  pendingVerifications: Option[Int]
) {

  private def normalisedType: Option[String] =
    subcontractorType.map(_.trim.toLowerCase.replace(" ", ""))

  def displayName: Option[String] = {
    val personalName =
      Seq(firstName, secondName, surname).flatten
        .map(_.trim)
        .filter(_.nonEmpty)
        .mkString(" ")

    normalisedType match {
      case Some("soletrader" | "individual") if personalName.nonEmpty =>
        Some(personalName)

      case Some("partnership") =>
        partnershipTradingName
          .orElse(tradingName)
          .filter(_.trim.nonEmpty)
          .orElse(Option.when(personalName.nonEmpty)(personalName))

      case Some("company" | "trust" | "soletrader" | "individual") =>
        tradingName
          .filter(_.trim.nonEmpty)
          .orElse(Option.when(personalName.nonEmpty)(personalName))

      case _ =>
        tradingName
          .orElse(partnershipTradingName)
          .filter(_.trim.nonEmpty)
          .orElse(Option.when(personalName.nonEmpty)(personalName))
    }
  }

}

object GetSubcontractor {
  given reads: Reads[GetSubcontractor] =
    Json.reads[GetSubcontractor]

  given writes: Writes[GetSubcontractor] = subcontractor =>
    Json
      .writes[GetSubcontractor]
      .writes(subcontractor) + ("displayName" -> JsString(
      subcontractor.displayName.getOrElse("")
    ))
}
