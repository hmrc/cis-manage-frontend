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

import play.api.libs.json.{Json, OFormat}
import java.time.Instant

case class SubmittedReturnsData(
  scheme: SubmittedSchemeData,
  monthlyReturn: Seq[SubmittedMonthlyReturnData],
  submissions: Seq[SubmittedSubmissionData]
)

object SubmittedReturnsData {
  given format: OFormat[SubmittedReturnsData] = Json.format[SubmittedReturnsData]
}

case class SubmittedSchemeData(
  name: String,
  taxOfficeNumber: String,
  taxOfficeReference: String
)

object SubmittedSchemeData {
  given format: OFormat[SubmittedSchemeData] = Json.format[SubmittedSchemeData]
}

case class SubmittedMonthlyReturnData(
  monthlyReturnId: Long,
  taxYear: Int,
  taxMonth: Int,
  nilReturnIndicator: String,
  status: String,
  supersededBy: Option[Long],
  amendmentStatus: Option[String],
  monthlyReturnItems: Option[String]
)

object SubmittedMonthlyReturnData {
  given format: OFormat[SubmittedMonthlyReturnData] = Json.format[SubmittedMonthlyReturnData]
}

case class SubmittedSubmissionData(
  submissionId: Long,
  submissionType: Option[String],
  activeObjectId: Long,
  status: String,
  hmrcMarkGenerated: Option[String],
  hmrcMarkGgis: Option[String],
  emailRecipient: Option[String],
  acceptedTime: Option[Instant]
)

object SubmittedSubmissionData {
  given format: OFormat[SubmittedSubmissionData] = Json.format[SubmittedSubmissionData]
}
