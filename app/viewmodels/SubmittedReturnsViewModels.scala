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
  selectedTaxYear: Option[String],
  showReturnToTaxYearsLink: Boolean = false
) {
  val showTaxYearHeadings: Boolean = taxYears.size > 1
}

case class TaxYearHistoryViewModel(
  fromYear: Int,
  toYear: Int,
  rows: Seq[SubmittedReturnsRowViewModel]
)

case class SubmittedReturnsRowViewModel(
  returnPeriodEnd: String,
  returnType: ReturnTypeViewModel,
  dateSubmitted: String,
  monthlyReturn: LinkViewModel,
  submissionReceipt: StatusViewModel,
  status: StatusViewModel
)

case class LinkViewModel(
  url: String,
  hiddenText: String
)

sealed trait ReturnTypeViewModel

object ReturnTypeViewModel {
  case object Nil extends ReturnTypeViewModel
  case object Standard extends ReturnTypeViewModel
  case object Unknown extends ReturnTypeViewModel
}

sealed trait StatusViewModel

object StatusViewModel {
  case class Text(messageKey: String) extends StatusViewModel
  case class Link(link: LinkViewModel, textKey: String, hiddenTextKey: String) extends StatusViewModel
}
