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

case class SubmittedReturnsPageViewModel(
  taxYears: Seq[TaxYearHistoryViewModel],
  selectedTaxYear: Option[String]
)

case class TaxYearHistoryViewModel(
  taxYearCaption: String,
  rows: Seq[SubmittedReturnsRowViewModel]
)

case class SubmittedReturnsRowViewModel(
  returnPeriodEnd: String,
  dateSubmitted: String,
  monthlyReturn: LinkViewModel,
  submissionReceipt: LinkViewModel,
  status: StatusViewModel
)

case class LinkViewModel(
  text: String,
  url: String,
  hiddenText: String
)

sealed trait StatusViewModel

object StatusViewModel {
  case class Text(value: String) extends StatusViewModel
  case class Link(value: LinkViewModel) extends StatusViewModel
}
