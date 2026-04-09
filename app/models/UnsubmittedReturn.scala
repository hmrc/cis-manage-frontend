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

import play.api.libs.json.{Format, Json, OFormat}

import java.time.Instant
import java.util.Locale
import play.api.i18n.Lang
import utils.Utils.monthName
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

case class UnsubmittedReturn(
  instanceId: String,
  monthlyReturnId: Long,
  taxYear: Int,
  taxMonth: Int,
  returnType: String,
  status: String,
  amendment: Option[String],
  deletable: Boolean,
  lastUpdated: Instant
) {
  def monthYear(langCode: String): String = {
    val locale: Locale = Lang.get(langCode).map(_.locale).getOrElse(Locale.UK)
    s"${monthName(taxMonth, locale)} $taxYear"
  }
}

object UnsubmittedReturn {
  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  implicit val format: OFormat[UnsubmittedReturn] = Json.format[UnsubmittedReturn]
}
