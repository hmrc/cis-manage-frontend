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

import play.api.libs.json.*

sealed trait TaxYearSelection

object TaxYearSelection {
  case class TaxYear(start: Int, end: Int) extends TaxYearSelection:
    override def toString: String = s"$start to $end"

  case object AllTaxYears extends TaxYearSelection:
    override def toString: String = "all"

  given OFormat[TaxYearSelection] = Json.format[TaxYearSelection]
  given OFormat[TaxYear]          = Json.format[TaxYear]
  given OFormat[AllTaxYears.type] = new OFormat[AllTaxYears.type] {
    override def reads(json: JsValue): JsResult[AllTaxYears.type] = (json \ "all").validate
    override def writes(o: AllTaxYears.type): JsObject            = JsObject(Seq("all" -> JsBoolean(true)))
  }
}
