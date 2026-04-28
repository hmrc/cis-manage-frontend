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

import play.api.libs.json.*

sealed trait TaxYearSelection

object TaxYearSelection {
  case class TaxYear(start: Int, end: Int) extends TaxYearSelection:
    override def toString: String = s"$start to $end"

  case object AllTaxYears extends TaxYearSelection:
    override def toString: String = "all"

  given OFormat[TaxYear] = Json.format[TaxYear]

  given OFormat[AllTaxYears.type] = new OFormat[AllTaxYears.type] {
    override def reads(json: JsValue): JsResult[AllTaxYears.type] =
      (json \ "all").validate[Boolean].collect(JsonValidationError("all must be true")) { case true =>
        AllTaxYears
      }

    override def writes(o: AllTaxYears.type): JsObject =
      Json.obj("all" -> true)
  }

  given OFormat[TaxYearSelection] = new OFormat[TaxYearSelection] {
    override def reads(json: JsValue): JsResult[TaxYearSelection] =
      // decide which subtype based on fields present
      (json \ "all").validateOpt[Boolean].flatMap {
        case Some(true)  => JsSuccess(AllTaxYears)
        case Some(false) => JsError("all must be true when present")
        case None        => json.validate[TaxYear]
      }

    override def writes(o: TaxYearSelection): JsObject =
      o match
        case t: TaxYear  => Json.toJsObject(t)
        case AllTaxYears => Json.toJsObject(AllTaxYears)
  }
}
