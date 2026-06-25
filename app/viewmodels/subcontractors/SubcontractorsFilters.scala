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

package viewmodels.subcontractors

enum VerificationStatusFilter(val value: String):
  case All extends VerificationStatusFilter("all")
  case Verified extends VerificationStatusFilter("verified")
  case NotVerified extends VerificationStatusFilter("notVerified")

object VerificationStatusFilter {
  def fromString(value: String): VerificationStatusFilter =
    VerificationStatusFilter.values.find(_.value == value).getOrElse(VerificationStatusFilter.All)
}

enum TaxTreatmentFilter(val value: String):
  case All extends TaxTreatmentFilter("all")
  case Gross extends TaxTreatmentFilter("gross")
  case HigherRate extends TaxTreatmentFilter("higherRate")
  case StandardRate extends TaxTreatmentFilter("standardRate")
  case Unknown extends TaxTreatmentFilter("unknown")

object TaxTreatmentFilter {
  def fromString(value: String): TaxTreatmentFilter =
    TaxTreatmentFilter.values.find(_.value == value).getOrElse(TaxTreatmentFilter.All)
}
