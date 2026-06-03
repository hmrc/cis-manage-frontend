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

package models.verify

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class VerificationHistoryData(
  verificationRequests: Seq[VerificationRequestData]
)

object VerificationHistoryData {
  given format: OFormat[VerificationHistoryData] = Json.format[VerificationHistoryData]
}

case class VerificationRequestData(
  verificationNumber: String,
  dateSubmitted: LocalDate,
  taxYear: Int
)

object VerificationRequestData {
  given format: OFormat[VerificationRequestData] = Json.format[VerificationRequestData]
}
