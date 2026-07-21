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

package utils

import models.response.GetSubcontractor

import java.time.{LocalDate, Month}

object ReverificationRules {

  /** reVerification required rule
    *
    * use only when subcontractor.verified == Some("Y"):
    *   - if verificationDate is NOT present => reVerification required.
    *   - else compute startDate(currentDate).
    *     - If verificationDate is between startDate and currentDate => reverifcation not required.
    *     - If verificationDate is before startDate:
    *       - If lastMonthlyReturnDate exists AND is between startDate and currentDate (inclusive) => reverifcation not
    *         required.
    *       - else => required.
    */
  def reverifyRequired(sub: GetSubcontractor, currentDate: LocalDate): Boolean = {
    val isPreviouslyVerified = sub.verified.contains("Y")
    if (!isPreviouslyVerified) {
      false
    } else {
      val start = startDate(currentDate)

      val verificationDateOpt: Option[LocalDate] =
        sub.verificationDate.map(_.toLocalDate)

      val lastMonthlyReturnDateOpt: Option[LocalDate] =
        sub.lastMonthlyReturnDate.map(_.toLocalDate)

      verificationDateOpt match {
        case None =>
          true

        case Some(verificationDate) =>
          if (isBetweenInclusive(verificationDate, start, currentDate)) {
            false
          } else {
            lastMonthlyReturnDateOpt match {
              case Some(lmr) if isBetweenInclusive(lmr, start, currentDate) =>
                false
              case _                                                        =>
                true
            }
          }
      }
    }
  }

  def isBetweenInclusive(d: LocalDate, start: LocalDate, end: LocalDate): Boolean =
    !d.isBefore(start) && !d.isAfter(end)

  def startDate(currentDate: LocalDate): LocalDate = {
    val date24MonthsPrior = currentDate.minusYears(2)
    val sixAprilThatYear  = LocalDate.of(date24MonthsPrior.getYear, Month.APRIL, 6)

    val startYear =
      if (date24MonthsPrior.isBefore(sixAprilThatYear)) date24MonthsPrior.getYear - 1
      else date24MonthsPrior.getYear

    LocalDate.of(startYear, Month.APRIL, 6)
  }
}
