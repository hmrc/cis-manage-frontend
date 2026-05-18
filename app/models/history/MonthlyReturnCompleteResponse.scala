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

import play.api.libs.json.{Json, OFormat, Reads, Writes}

import java.time.LocalDateTime

case class MonthlyReturnCompleteResponse(
  scheme: Seq[CompleteSchemeData],
  monthlyReturn: Seq[CompleteMonthlyReturnData],
  subcontractors: Seq[CompleteSubcontractorData],
  monthlyReturnItems: Seq[CompleteMonthlyReturnItemData],
  submission: Seq[CompleteSubmissionData]
)

object MonthlyReturnCompleteResponse {
  given format: OFormat[MonthlyReturnCompleteResponse] = Json.format[MonthlyReturnCompleteResponse]
}

case class CompleteSchemeData(
  schemeId: Int,
  instanceId: String,
  accountsOfficeReference: String,
  taxOfficeNumber: String,
  taxOfficeReference: String,
  utr: Option[String] = None,
  name: Option[String] = None,
  emailAddress: Option[String] = None
)

object CompleteSchemeData {
  given format: OFormat[CompleteSchemeData] = Json.format[CompleteSchemeData]
}

case class CompleteMonthlyReturnData(
  monthlyReturnId: Long,
  taxYear: Int,
  taxMonth: Int,
  nilReturnIndicator: Option[String] = None,
  decInformationCorrect: Option[String] = None,
  decNilReturnNoPayments: Option[String] = None,
  status: Option[String] = None,
  lastUpdate: Option[LocalDateTime] = None,
  amendment: Option[String] = None,
  supersededBy: Option[Long] = None
)

object CompleteMonthlyReturnData {
  given format: OFormat[CompleteMonthlyReturnData] = Json.format[CompleteMonthlyReturnData]
}

case class CompleteSubcontractorData(
  subcontractorId: Long,
  utr: Option[String] = None,
  firstName: Option[String] = None,
  surname: Option[String] = None,
  tradingName: Option[String] = None,
  partnershipTradingName: Option[String] = None,
  subcontractorType: Option[String] = None,
  verificationNumber: Option[String] = None,
  taxTreatment: Option[String] = None,
  displayName: Option[String] = None
)

object CompleteSubcontractorData {
  given format: OFormat[CompleteSubcontractorData] = Json.format[CompleteSubcontractorData]
}

case class CompleteMonthlyReturnItemData(
  monthlyReturnId: Long,
  monthlyReturnItemId: Long,
  totalPayments: Option[String] = None,
  costOfMaterials: Option[String] = None,
  totalDeducted: Option[String] = None,
  subcontractorId: Option[Long] = None,
  subcontractorName: Option[String] = None,
  verificationNumber: Option[String] = None
)

object CompleteMonthlyReturnItemData {
  given format: OFormat[CompleteMonthlyReturnItemData] = Json.format[CompleteMonthlyReturnItemData]
}

case class CompleteSubmissionData(
  submissionId: Long,
  submissionType: String,
  activeObjectId: Option[Long] = None,
  status: Option[String] = None,
  hmrcMarkGenerated: Option[String] = None,
  hmrcMarkGgis: Option[String] = None,
  emailRecipient: Option[String] = None,
  acceptedTime: Option[String] = None
)

object CompleteSubmissionData {
  given format: OFormat[CompleteSubmissionData] = Json.format[CompleteSubmissionData]
}
