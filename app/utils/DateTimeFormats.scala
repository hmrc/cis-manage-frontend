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

import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeFormats {

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  private val timeFormatter = DateTimeFormatter.ofPattern("h:mma")

  private val localisedDateTimeFormatters = Map(
    "en" -> dateTimeFormatter,
    "cy" -> dateTimeFormatter.withLocale(Locale.forLanguageTag("cy"))
  )

  private val localisedTimeFormatters = Map(
    "en" -> timeFormatter,
    "cy" -> timeFormatter.withLocale(Locale.forLanguageTag("cy"))
  )

  def dateTimeFormat()(implicit lang: Lang): DateTimeFormatter =
    localisedDateTimeFormatters.getOrElse(lang.code, dateTimeFormatter)

  private val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

  private val localisedShortDateFormatters = Map(
    "en" -> shortDateFormatter,
    "cy" -> shortDateFormatter.withLocale(Locale.forLanguageTag("cy"))
  )

  def shortDateFormat()(implicit lang: Lang): DateTimeFormatter =
    localisedShortDateFormatters.getOrElse(lang.code, shortDateFormatter)

  private val monthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

  private val localisedMonthYearFormatters = Map(
    "en" -> monthYearFormatter,
    "cy" -> monthYearFormatter.withLocale(Locale.forLanguageTag("cy"))
  )

  def monthYearFormat()(implicit lang: Lang): DateTimeFormatter =
    localisedMonthYearFormatters.getOrElse(lang.code, monthYearFormatter)

  val dateTimeHintFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d M yyyy")

  def timeFormat()(implicit lang: Lang): DateTimeFormatter =
    localisedTimeFormatters.getOrElse(lang.code, dateTimeFormatter)
}
