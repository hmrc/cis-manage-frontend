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
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.{LocalDate, LocalDateTime}

class ReverificationRulesSpec extends AnyWordSpec with Matchers {

  private val testSubcontractorId: Long = 1L
  private val daysBeforeStart: Long     = 10L
  private val daysAfterStart: Long      = 5L
  private val oneDay: Long              = 1L

  private def sub(
    verified: Option[String] = Some("Y"),
    verificationDate: Option[LocalDateTime] = None,
    lastMonthlyReturnDate: Option[LocalDateTime] = None,
    subcontractorType: Option[String] = None,
    subbieResourceRef: Option[Long] = None,
    utr: Option[String] = None,
    partnerUtr: Option[String] = None
  ): GetSubcontractor =
    GetSubcontractor(
      subcontractorId = testSubcontractorId,
      utr = utr,
      pageVisited = None,
      partnerUtr = partnerUtr,
      crn = None,
      firstName = None,
      nino = None,
      secondName = None,
      surname = None,
      partnershipTradingName = None,
      tradingName = None,
      subcontractorType = subcontractorType,
      addressLine1 = None,
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      country = None,
      postcode = None,
      emailAddress = None,
      phoneNumber = None,
      mobilePhoneNumber = None,
      worksReferenceNumber = None,
      createDate = None,
      lastUpdate = None,
      subbieResourceRef = subbieResourceRef,
      matched = None,
      autoVerified = None,
      verified = verified,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = verificationDate,
      version = None,
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = lastMonthlyReturnDate,
      pendingVerifications = None
    )
  "ReverificationRules.startDate" should {

    "match the examples in the spec" in {
      ReverificationRules.startDate(LocalDate.of(2026, 1, 25)) mustBe LocalDate.of(2023, 4, 6)
      ReverificationRules.startDate(LocalDate.of(2026, 4, 5)) mustBe LocalDate.of(2023, 4, 6)
      ReverificationRules.startDate(LocalDate.of(2026, 4, 6)) mustBe LocalDate.of(2024, 4, 6)
      ReverificationRules.startDate(LocalDate.of(2026, 4, 25)) mustBe LocalDate.of(2024, 4, 6)
    }
  }

  "ReverificationRules.reverifyRequired" should {

    "return false when subcontractor is not previously verified (VERIFIED != Y)" in {
      val current = LocalDate.of(2026, 4, 25)
      ReverificationRules.reverifyRequired(sub(verified = Some("N")), current) mustBe false
      ReverificationRules.reverifyRequired(sub(verified = None), current) mustBe false
    }

    "AC1: require reVerification when VERIFIED == Y but verificationDate is missing" in {
      val current = LocalDate.of(2026, 1, 25)
      ReverificationRules.reverifyRequired(sub(verificationDate = None), current) mustBe true
    }

    "AC2: not require reVerification when verificationDate is between startDate and currentDate" in {
      val current = LocalDate.of(2026, 1, 25)
      val start   = ReverificationRules.startDate(current)

      val s = sub(verificationDate = Some(start.plusDays(oneDay).atStartOfDay()))
      ReverificationRules.reverifyRequired(s, current) mustBe false

      val s2 = sub(verificationDate = Some(start.atStartOfDay()))
      ReverificationRules.reverifyRequired(s2, current) mustBe false
    }

    "AC2: not require reVerification when verificationDate == currentDate (upper bound inclusive)" in {
      val current = LocalDate.of(2026, 1, 25)

      val s = sub(verificationDate = Some(current.atStartOfDay()))
      ReverificationRules.reverifyRequired(s, current) mustBe false
    }

    "AC3: not require reVerification when verificationDate is before startDate but lastMonthlyReturnDate is between startDate and currentDate" in {
      val current = LocalDate.of(2026, 1, 25)
      val start   = ReverificationRules.startDate(current)

      val s = sub(
        verificationDate = Some(start.minusDays(daysBeforeStart).atStartOfDay()),
        lastMonthlyReturnDate = Some(start.plusDays(daysAfterStart).atStartOfDay())
      )

      ReverificationRules.reverifyRequired(s, current) mustBe false
    }

    "AC3: not require reVerification when verificationDate is before startDate but lastMonthlyReturnDate == startDate (lower bound inclusive)" in {
      val current = LocalDate.of(2026, 1, 25)
      val start   = ReverificationRules.startDate(current)

      val s = sub(
        verificationDate = Some(start.minusDays(daysBeforeStart).atStartOfDay()),
        lastMonthlyReturnDate = Some(start.atStartOfDay())
      )

      ReverificationRules.reverifyRequired(s, current) mustBe false
    }

    "AC3: not require reVerification when verificationDate is before startDate but lastMonthlyReturnDate == currentDate (upper bound inclusive)" in {
      val current = LocalDate.of(2026, 1, 25)
      val start   = ReverificationRules.startDate(current)

      val s = sub(
        verificationDate = Some(start.minusDays(daysBeforeStart).atStartOfDay()),
        lastMonthlyReturnDate = Some(current.atStartOfDay())
      )

      ReverificationRules.reverifyRequired(s, current) mustBe false
    }

    "AC4: require reVerification when verificationDate is before startDate and lastMonthlyReturnDate is before startDate" in {
      val current = LocalDate.of(2026, 1, 25)
      val start   = ReverificationRules.startDate(current)

      val s = sub(
        verificationDate = Some(start.minusDays(daysBeforeStart).atStartOfDay()),
        lastMonthlyReturnDate = Some(start.minusDays(oneDay).atStartOfDay())
      )

      ReverificationRules.reverifyRequired(s, current) mustBe true
    }

    "require reVerification when verificationDate is before startDate and lastMonthlyReturnDate is missing" in {
      val current = LocalDate.of(2026, 1, 25)
      val start   = ReverificationRules.startDate(current)

      val s = sub(
        verificationDate = Some(start.minusDays(daysBeforeStart).atStartOfDay()),
        lastMonthlyReturnDate = None
      )

      ReverificationRules.reverifyRequired(s, current) mustBe true
    }

    "require reVerification when verificationDate is in the future and lastMonthlyReturnDate is missing" in {
      val current = LocalDate.of(2026, 1, 25)

      val s = sub(
        verificationDate = Some(current.plusDays(oneDay).atStartOfDay()),
        lastMonthlyReturnDate = None
      )

      ReverificationRules.reverifyRequired(s, current) mustBe true
    }
  }
}
