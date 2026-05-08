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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

object VerificationHistorySelectTaxYear {

  val taxYears: Seq[String] = Seq(
    "2026 to 2027 (current tax year)",
    "2025 to 2026",
    "2024 to 2025",
    "2023 to 2024"
  )

  def options(taxYears: Seq[String])(implicit messages: Messages): Seq[RadioItem] = {

    val yearItems = taxYears.zipWithIndex.map { case (year, index) =>
      RadioItem(
        content = Text(year),
        value = Some(year),
        id = Some(s"value_$index")
      )
    }

    val divider = Seq(
      RadioItem(divider = Some(messages("site.or")))
    )

    val viewAll = Seq(
      RadioItem(
        content = Text(messages("verify.verificationHistorySelectTaxYear.viewAll")),
        value = Some("all"),
        id = Some("value_all")
      )
    )

    yearItems ++ divider ++ viewAll
  }
}
