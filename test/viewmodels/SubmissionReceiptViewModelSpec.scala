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

package viewmodels

import models.history.SubcontractorPayment
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class SubmissionReceiptViewModelSpec extends AnyWordSpec with Matchers {

  "SubmissionReceiptViewModel" should {
    "round-trip to/from JSON with all fields populated" in {
      val model = SubmissionReceiptViewModel(
        contractorName = "Test Contractor",
        payeReference = "123/ABC456",
        taxYear = 2024,
        taxMonth = 6,
        returnPeriodEnd = "June 2024",
        returnType = "submissionConfirmation.returnType.monthly",
        submissionType = "Monthly return",
        hmrcMark = Some("HMRC-123"),
        submittedAt = Some("11:30am on 1 July 2024"),
        emailRecipient = Some("user@example.com"),
        instanceId = "INST001",
        items = Seq(
          SubcontractorPayment("John Smith", "5000.00", "1000.00", "800.00")
        )
      )

      Json.toJson(model).as[SubmissionReceiptViewModel] shouldBe model
    }

    "round-trip to/from JSON when optional fields are absent" in {
      val model = SubmissionReceiptViewModel(
        contractorName = "Nil Co",
        payeReference = "123/ABC456",
        taxYear = 2024,
        taxMonth = 2,
        returnPeriodEnd = "February 2024",
        returnType = "submissionConfirmation.returnType.nil",
        submissionType = "Nil return",
        hmrcMark = None,
        submittedAt = None,
        emailRecipient = None,
        instanceId = "INST001",
        items = Seq.empty
      )

      Json.toJson(model).as[SubmissionReceiptViewModel] shouldBe model
    }
  }
}
