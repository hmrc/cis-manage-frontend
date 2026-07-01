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

import play.api.libs.json.{JsObject, JsString, Json, OFormat, Reads, Writes}
import java.time.LocalDateTime
import java.time.Instant

final case class GetSubmittedVerificationsResponse(
  scheme: Seq[GetSubmittedContractorScheme],
  subcontractors: Seq[GetSubmittedSubcontractor],
  verificationBatches: Seq[GetSubmittedVerificationBatch],
  verifications: Seq[GetSubmittedVerification],
  submissions: Seq[GetSubmittedSubmission]
)

object GetSubmittedVerificationsResponse {
  given format: OFormat[GetSubmittedVerificationsResponse] =
    Json.format[GetSubmittedVerificationsResponse]
}

final case class GetSubmittedVerificationBatch(
  verificationBatchId: Long,
  schemeId: Long,
  verificationsCounter: Option[Long],
  verifBatchResourceRef: Option[Long],
  proceedSession: Option[String],
  confirmArrangement: Option[String],
  confirmCorrect: Option[String],
  status: Option[String],
  verificationNumber: Option[String],
  createDate: Option[LocalDateTime],
  lastUpdate: Option[LocalDateTime],
  version: Option[Int]
)

object GetSubmittedVerificationBatch {
  given format: OFormat[GetSubmittedVerificationBatch] = Json.format[GetSubmittedVerificationBatch]
}

case class GetSubmittedVerification(
  verificationId: Long,
  matched: Option[String],
  verificationNumber: Option[String],
  taxTreatment: Option[String],
  actionIndicator: Option[String],
  verificationBatchId: Option[Long],
  schemeId: Option[Long],
  subcontractorId: Option[Long],
  subcontractorName: Option[String],
  verificationResourceRef: Option[Long],
  proceed: Option[String],
  createDate: Option[LocalDateTime],
  lastUpdate: Option[LocalDateTime],
  version: Option[Int]
)

object GetSubmittedVerification {
  given format: OFormat[GetSubmittedVerification] = Json.format[GetSubmittedVerification]
}

case class GetSubmittedSubcontractor(
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
  def displayName: String =
    (
      subcontractorType.map(_.toLowerCase),
      firstName,
      surname,
      tradingName
    ) match {
      case (Some("soletrader"), Some(firstName), Some(surname), _)             => s"$firstName $surname"
      case (Some("soletrader"), None, Some(surname), _)                        => surname
      case (Some("soletrader" | "company" | "trust"), _, _, Some(tradingName)) => tradingName
      case (Some("partnership"), _, _, Some(tradingName))                      =>
        partnershipTradingName
          .getOrElse(tradingName)
      case _                                                                   => "No name provided"
    }
}

object GetSubmittedSubcontractor {
  given reads: Reads[GetSubmittedSubcontractor] = Json.reads[GetSubmittedSubcontractor]

  given writes: Writes[GetSubmittedSubcontractor] = s =>
    Json
      .writes[GetSubmittedSubcontractor]
      .writes(s) + ("displayName", JsString(s.displayName))
}

case class GetSubmittedContractorScheme(
  schemeId: Int,
  instanceId: String,
  accountsOfficeReference: String,
  taxOfficeNumber: String,
  taxOfficeReference: String,
  utr: Option[String] = None,
  name: Option[String] = None,
  emailAddress: Option[String] = None,
  displayWelcomePage: Option[String] = None,
  prePopCount: Option[Int] = None,
  prePopSuccessful: Option[String] = None,
  subcontractorCounter: Option[Int] = None,
  verificationBatchCounter: Option[Int] = None,
  createDate: Option[Instant] = None,
  lastUpdate: Option[Instant] = None,
  version: Option[Int] = None
)

object GetSubmittedContractorScheme {
  given OFormat[GetSubmittedContractorScheme] = Json.format[GetSubmittedContractorScheme]
}

case class GetSubmittedSubmission(
  submissionId: Long,
  submissionType: String,
  activeObjectId: Option[Long],
  status: Option[String],
  hmrcMarkGenerated: Option[String],
  hmrcMarkGgis: Option[String],
  emailRecipient: Option[String],
  acceptedTime: Option[String],
  createDate: Option[LocalDateTime],
  lastUpdate: Option[LocalDateTime],
  schemeId: Long,
  agentId: Option[String],
  l_Migrated: Option[Long],
  submissionRequestDate: Option[LocalDateTime],
  govTalkErrorCode: Option[String],
  govTalkErrorType: Option[String],
  govTalkErrorMessage: Option[String]
)

object GetSubmittedSubmission {
  given format: OFormat[GetSubmittedSubmission] = Json.format[GetSubmittedSubmission]
}
