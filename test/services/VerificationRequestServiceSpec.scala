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

package services

import models.verify.{SubcontractorVerificationData, VerificationRequestDetailData}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import java.time.LocalDateTime

class VerificationRequestServiceSpec extends AnyFreeSpec with Matchers {

  private val service    = new VerificationRequestService()
  private val instanceId = "900063"

  private val data = VerificationRequestDetailData(
    verificationNumber = "V0004528765",
    dateTimeSubmitted = LocalDateTime.of(2027, 2, 6, 14, 30),
    subcontractorsToVerify = Seq(
      SubcontractorVerificationData("Amity Marine Contractors", "V0004528765"),
      SubcontractorVerificationData("Brody, Martin", "V0004528765")
    ),
    subcontractorsToReverify = Seq(
      SubcontractorVerificationData("Orca Industrial", "V0004528765/L")
    )
  )

  private val dataNoReverify = data.copy(subcontractorsToReverify = Seq.empty)

  "VerificationRequestService" - {

    "buildViewModel" - {

      "must format submitted time correctly" in {
        val result = service.buildViewModel(data, instanceId)

        result.submittedTime mustBe "14:30"
      }

      "must format submitted date correctly" in {
        val result = service.buildViewModel(data, instanceId)

        result.submittedDate mustBe "6 February 2027"
      }

      "must set the verification number" in {
        val result = service.buildViewModel(data, instanceId)

        result.verificationNumber mustBe "V0004528765"
      }

      "must calculate total subcontractors as sum of verify and reverify lists" in {
        val result = service.buildViewModel(data, instanceId)

        result.totalSubcontractors mustBe 3
      }

      "must map subcontractors to verify correctly" in {
        val result = service.buildViewModel(data, instanceId)

        result.subcontractorsToVerify.size mustBe 2
        result.subcontractorsToVerify.head.name mustBe "Amity Marine Contractors"
        result.subcontractorsToVerify.head.verificationNumber mustBe "V0004528765"
        result.subcontractorsToVerify(1).name mustBe "Brody, Martin"
      }

      "must map subcontractors to reverify correctly" in {
        val result = service.buildViewModel(data, instanceId)

        result.subcontractorsToReverify.size mustBe 1
        result.subcontractorsToReverify.head.name mustBe "Orca Industrial"
        result.subcontractorsToReverify.head.verificationNumber mustBe "V0004528765/L"
      }

      "must set hasReverifications to true when reverify list is non-empty" in {
        val result = service.buildViewModel(data, instanceId)

        result.hasReverifications mustBe true
      }

      "must set hasReverifications to false when reverify list is empty" in {
        val result = service.buildViewModel(dataNoReverify, instanceId)

        result.hasReverifications mustBe false
      }

      "must calculate total subcontractors correctly when no reverifications" in {
        val result = service.buildViewModel(dataNoReverify, instanceId)

        result.totalSubcontractors mustBe 2
      }

      "must build the manage subcontractors URL" in {
        val result = service.buildViewModel(data, instanceId)

        result.manageSubcontractorsUrl mustBe controllers.routes.SubcontractorsLandingPageController
          .onPageLoad(instanceId)
          .url
      }
    }
  }
}
