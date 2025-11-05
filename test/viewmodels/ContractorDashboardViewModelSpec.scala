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

package viewmodels

import base.SpecBase
import org.scalatest.matchers.should.Matchers.*
import viewmodels.contractor.ContractorLandingViewModel

class ContractorDashboardViewModelSpec extends SpecBase {

  "ContractorDashboardViewModel" - {

    "should create a valid instance with all required fields" in {
      val viewModel = ContractorLandingViewModel(
        employerReference = "123/AB45678",
        utr = "1234567890",
        returnCount = 1,
        returnDueDate = "19 October 2025",
        noticeCount = 2,
        lastSubmittedDate = "19 September 2025",
        lastSubmittedTaxMonthYear = "August 2025",
        whatIsUrl = "#",
        guidanceUrl = "#",
        penaltiesUrl = "#"
      )

      viewModel.employerReference         shouldBe "123/AB45678"
      viewModel.utr                       shouldBe "1234567890"
      viewModel.returnCount               shouldBe 1
      viewModel.returnDueDate             shouldBe "19 October 2025"
      viewModel.noticeCount               shouldBe 2
      viewModel.lastSubmittedDate         shouldBe "19 September 2025"
      viewModel.lastSubmittedTaxMonthYear shouldBe "August 2025"
    }

    "should support case class copy" in {
      val original = ContractorLandingViewModel(
        employerReference = "123/AB45678",
        utr = "1234567890",
        returnCount = 1,
        returnDueDate = "19 October 2025",
        noticeCount = 2,
        lastSubmittedDate = "19 September 2025",
        lastSubmittedTaxMonthYear = "August 2025",
        whatIsUrl = "#",
        guidanceUrl = "#",
        penaltiesUrl = "#"
      )

      val modified = original.copy(returnCount = 3, noticeCount = 5)

      modified.employerReference         shouldBe "123/AB45678"
      modified.utr                       shouldBe "1234567890"
      modified.returnCount               shouldBe 3
      modified.returnDueDate             shouldBe "19 October 2025"
      modified.noticeCount               shouldBe 5
      modified.lastSubmittedDate         shouldBe "19 September 2025"
      modified.lastSubmittedTaxMonthYear shouldBe "August 2025"
    }

    "should support equality comparison" in {
      val viewModel1 = ContractorLandingViewModel(
        employerReference = "123/AB45678",
        utr = "1234567890",
        returnCount = 1,
        returnDueDate = "19 October 2025",
        noticeCount = 2,
        lastSubmittedDate = "19 September 2025",
        lastSubmittedTaxMonthYear = "August 2025",
        whatIsUrl = "#",
        guidanceUrl = "#",
        penaltiesUrl = "#"
      )

      val viewModel2 = ContractorLandingViewModel(
        employerReference = "123/AB45678",
        utr = "1234567890",
        returnCount = 1,
        returnDueDate = "19 October 2025",
        noticeCount = 2,
        lastSubmittedDate = "19 September 2025",
        lastSubmittedTaxMonthYear = "August 2025",
        whatIsUrl = "#",
        guidanceUrl = "#",
        penaltiesUrl = "#"
      )

      viewModel1 shouldBe viewModel2
    }

    "should handle different values correctly" in {
      val viewModel1 = ContractorLandingViewModel(
        employerReference = "123/AB45678",
        utr = "1234567890",
        returnCount = 1,
        returnDueDate = "19 October 2025",
        noticeCount = 2,
        lastSubmittedDate = "19 September 2025",
        lastSubmittedTaxMonthYear = "August 2025",
        whatIsUrl = "#",
        guidanceUrl = "#",
        penaltiesUrl = "#"
      )

      val viewModel2 = ContractorLandingViewModel(
        employerReference = "456/CD12345",
        utr = "0987654321",
        returnCount = 5,
        returnDueDate = "20 November 2025",
        noticeCount = 3,
        lastSubmittedDate = "15 October 2025",
        lastSubmittedTaxMonthYear = "September 2025",
        whatIsUrl = "#",
        guidanceUrl = "#",
        penaltiesUrl = "#"
      )

      viewModel1 should not be viewModel2
    }
  }
}
