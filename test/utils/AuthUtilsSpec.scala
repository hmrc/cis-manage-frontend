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

import base.SpecBase
import models.EmployerReference
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.auth.core.EnrolmentIdentifier

class AuthUtilsSpec extends SpecBase {

  class AuthUtilsSpec extends AnyWordSpec with Matchers {
    "AuthUtils.hasCisOrgEnrolment"   should {
      "return EmployerReference when valid HMRC-CIS-ORG enrolment is found and activated" in {
        val enrolment = Enrolment(
          key = "HMRC-CIS-ORG",
          identifiers = Seq(
            EnrolmentIdentifier("TaxOfficeNumber", "123"),
            EnrolmentIdentifier("TaxOfficeReference", "AB12345")
          ),
          state = "Activated"
        )
        val result    = AuthUtils.hasCisOrgEnrolment(Set(enrolment))
        result shouldBe Some(EmployerReference("123", "AB12345"))
      }
      "return None when HMRC-CIS-ORG enrolment is not activated" in {
        val enrolment = Enrolment(
          key = "HMRC-CIS-ORG",
          identifiers = Seq(
            EnrolmentIdentifier("TaxOfficeNumber", "123"),
            EnrolmentIdentifier("TaxOfficeReference", "AB12345")
          ),
          state = "NotYetActivated"
        )
        val result    = AuthUtils.hasCisOrgEnrolment(Set(enrolment))
        result shouldBe None
      }
      "return None when TaxOfficeNumber is invalid" in {
        val enrolment = Enrolment(
          key = "HMRC-CIS-ORG",
          identifiers = Seq(
            EnrolmentIdentifier("TaxOfficeNumber", "12A"),
            EnrolmentIdentifier("TaxOfficeReference", "AB12345")
          ),
          state = "Activated"
        )
        val result    = AuthUtils.hasCisOrgEnrolment(Set(enrolment))
        result shouldBe None
      }
      "return None when enrolment key does not match" in {
        val enrolment = Enrolment(
          key = "OTHER-ENROLMENT",
          identifiers = Seq(
            EnrolmentIdentifier("TaxOfficeNumber", "123"),
            EnrolmentIdentifier("TaxOfficeReference", "AB12345")
          ),
          state = "Activated"
        )
        val result    = AuthUtils.hasCisOrgEnrolment(Set(enrolment))
        result shouldBe None
      }
    }
    "AuthUtils.hasCisAgentEnrolment" should {
      "return agent reference when valid IR-PAYE-AGENT enrolment is found and activated" in {
        val enrolment = Enrolment(
          key = "IR-PAYE-AGENT",
          identifiers = Seq(
            EnrolmentIdentifier("IRAgentReference", "ABC123")
          ),
          state = "Activated"
        )
        val result    = AuthUtils.hasCisAgentEnrolment(Set(enrolment))
        result shouldBe Some("ABC123")
      }
      "return None when agent reference is invalid" in {
        val enrolment = Enrolment(
          key = "IR-PAYE-AGENT",
          identifiers = Seq(
            EnrolmentIdentifier("IRAgentReference", "123!")
          ),
          state = "Activated"
        )
        val result    = AuthUtils.hasCisAgentEnrolment(Set(enrolment))
        result shouldBe None
      }
      "return None when enrolment is not activated" in {
        val enrolment = Enrolment(
          key = "IR-PAYE-AGENT",
          identifiers = Seq(
            EnrolmentIdentifier("IRAgentReference", "ABC123")
          ),
          state = "NotYetActivated"
        )
        val result    = AuthUtils.hasCisAgentEnrolment(Set(enrolment))
        result shouldBe None
      }
      "return None when enrolment key does not match" in {
        val enrolment = Enrolment(
          key = "SOME-OTHER-ENROLMENT",
          identifiers = Seq(
            EnrolmentIdentifier("IRAgentReference", "ABC123")
          ),
          state = "Activated"
        )
        val result    = AuthUtils.hasCisAgentEnrolment(Set(enrolment))
        result shouldBe None
      }
    }
    "AuthUtils validation helpers"   should {
      "validate tax office number correctly" in {
        AuthUtils.isValidTaxOfficeNumber("123") shouldBe true
        AuthUtils.isValidTaxOfficeNumber("12A") shouldBe false
        AuthUtils.isValidTaxOfficeNumber("")    shouldBe false
      }
      "validate agent reference correctly" in {
        AuthUtils.isValidAgentReference("ABC123") shouldBe true
        AuthUtils.isValidAgentReference("123!")   shouldBe false
        AuthUtils.isValidAgentReference("")       shouldBe false
      }
    }
  }

}
