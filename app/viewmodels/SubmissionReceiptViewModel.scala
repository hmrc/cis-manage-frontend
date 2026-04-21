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

package viewmodels

import play.api.libs.json.{Json, OFormat}

case class SubmissionReceiptViewModel(
  contractorName: String,
  payeReference: String,
  taxYear: Int,
  taxMonth: Int,
  returnPeriodEnd: String,
  returnType: String,
  submissionType: String,
  hmrcMark: Option[String],
  submittedAt: Option[String],
  emailRecipient: Option[String],
  instanceId: String,
  items: Seq[SubmissionReceiptItemViewModel]
)

object SubmissionReceiptViewModel {
  given format: OFormat[SubmissionReceiptViewModel] = Json.format[SubmissionReceiptViewModel]
}

case class SubmissionReceiptItemViewModel(
  subcontractorName: String,
  totalPayments: String,
  costOfMaterials: String,
  totalDeducted: String
)

object SubmissionReceiptItemViewModel {
  given format: OFormat[SubmissionReceiptItemViewModel] = Json.format[SubmissionReceiptItemViewModel]
}
