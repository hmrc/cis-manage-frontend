/*
 * Copyright 2025 HM Revenue & Customs
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

package utils

import play.api.i18n.Lang

import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

object Utils {
  val emptyString: String = ""

  def monthName(taxMonth: Int, locale: Locale): String =
    Month.of(taxMonth).getDisplayName(TextStyle.FULL, locale)

  def monthYear(taxYear: Int, taxMonth: Int, langCode: String): String = {
    val locale: Locale = Lang.get(langCode).map(_.locale).getOrElse(Locale.UK)
    s"${monthName(taxMonth, locale)} $taxYear"
  }

  def formatCurrency(amount: BigDecimal): String =
    f"£$amount%.2f"

  def toBigDecimal(value: Option[String]): BigDecimal =
    value.map(v => BigDecimal(v.trim)).getOrElse(BigDecimal(0))
}
