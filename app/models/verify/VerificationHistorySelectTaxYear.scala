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

import models.verify.VerificationTaxYearSelection.TaxYearPeriod
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import java.time.LocalDate

object VerificationHistorySelectTaxYear {

  def options(taxYears: Seq[TaxYearPeriod], currentDate: LocalDate = LocalDate.now())(implicit
    messages: Messages
  ): Seq[RadioItem] = {

    val currentTaxYearStart = taxYearStart(currentDate)

    val yearItems = taxYears.zipWithIndex.map { case (taxYear, index) =>
      RadioItem(
        content = Text(labelFor(taxYear, currentTaxYearStart)),
        value = Some(taxYear.startYear.toString),
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

  private def labelFor(taxYear: TaxYearPeriod, currentTaxYearStart: Int)(implicit messages: Messages): String =
    if (taxYear.startYear == currentTaxYearStart) {
      s"${taxYear.toString} ${messages("verify.verificationHistorySelectTaxYear.currentTaxYear")}"
    } else {
      taxYear.toString
    }

  private def taxYearStart(date: LocalDate): Int = {
    val taxYearStartDate = LocalDate.of(date.getYear, 4, 6)

    if (date.isBefore(taxYearStartDate)) {
      date.getYear - 1
    } else {
      date.getYear
    }
  }
}
