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

import play.api.i18n.Lang
import play.api.libs.json.{Json, OFormat}
import utils.Utils.monthName

import java.util.Locale

case class SubmittedMonthlyReturnRow(
  instanceId: String,
  monthlyReturnId: Long,
  taxYear: Int,
  taxMonth: Int,
  amendment: Option[String]
) {
  def monthYear(langCode: String): String = {
    val locale: Locale = Lang.get(langCode).map(_.locale).getOrElse(Locale.UK)
    s"${monthName(taxMonth, locale)} $taxYear"
  }
}

object SubmittedMonthlyReturnRow {
  given format: OFormat[SubmittedMonthlyReturnRow] = Json.format[SubmittedMonthlyReturnRow]
}
