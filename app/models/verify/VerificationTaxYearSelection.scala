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

import play.api.libs.json.*
import models.verify.VerificationTaxYearSelection.TaxYear

sealed trait VerificationTaxYearSelection

object VerificationTaxYearSelection {

  case class TaxYear(value: String) extends VerificationTaxYearSelection {
    override def toString: String = value
  }

  case object AllTaxYears extends VerificationTaxYearSelection {
    override def toString: String = "all"
  }

  // 🔹 Stub data
  val taxYears: Seq[TaxYear] = Seq(
    TaxYear("2026 to 2027 (current tax year)"),
    TaxYear("2025 to 2026"),
    TaxYear("2024 to 2025"),
    TaxYear("2023 to 2024")
  )

  // ✅ ADD IT HERE (inside the object)
  def fromString(value: String): VerificationTaxYearSelection =
    if (value == "all") AllTaxYears
    else TaxYear(value)

  // JSON formats
  given OFormat[TaxYear] = Json.format[TaxYear]

  given OFormat[AllTaxYears.type] = new OFormat[AllTaxYears.type] {
    def reads(json: JsValue): JsResult[AllTaxYears.type] =
      (json \ "all").validate[Boolean].collect(JsonValidationError("invalid")) { case true =>
        AllTaxYears
      }

    def writes(o: AllTaxYears.type): JsObject =
      Json.obj("all" -> true)
  }

  given OFormat[VerificationTaxYearSelection] = new OFormat[VerificationTaxYearSelection] {

    def reads(json: JsValue): JsResult[VerificationTaxYearSelection] =
      (json \ "all").validateOpt[Boolean].flatMap {
        case Some(true)  => JsSuccess(AllTaxYears)
        case Some(false) => JsError("invalid")
        case None        => json.validate[TaxYear]
      }

    def writes(o: VerificationTaxYearSelection): JsObject =
      o match {
        case t: TaxYear  => Json.toJsObject(t)
        case AllTaxYears => Json.toJsObject(AllTaxYears)
      }
  }
}
