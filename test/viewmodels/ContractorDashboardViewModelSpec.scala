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
        schemeName = "ABC Construction Ltd",
        employerReference = "123/AB45678",
        whatIsUrl = "#",
        guidanceUrl = "#",
        penaltiesUrl = "#"
      )

      viewModel.employerReference shouldBe "123/AB45678"
      viewModel.schemeName        shouldBe "ABC Construction Ltd"
    }

    "should support case class copy" in {
      val original = ContractorLandingViewModel(
        schemeName = "ABC Construction Ltd",
        employerReference = "123/AB45678",
        whatIsUrl = "#",
        guidanceUrl = "#",
        penaltiesUrl = "#"
      )

      val modified = original.copy(schemeName = "NEW SCHEME NAME")

      modified.employerReference shouldBe "123/AB45678"
      modified.schemeName        shouldBe "NEW SCHEME NAME"
    }

    "should support equality comparison" in {
      val viewModel1 = ContractorLandingViewModel(
        schemeName = "ABC Construction Ltd",
        employerReference = "123/AB45678",
        whatIsUrl = "#",
        guidanceUrl = "#",
        penaltiesUrl = "#"
      )

      val viewModel2 = ContractorLandingViewModel(
        schemeName = "ABC Construction Ltd",
        employerReference = "123/AB45678",
        whatIsUrl = "#",
        guidanceUrl = "#",
        penaltiesUrl = "#"
      )

      viewModel1 shouldBe viewModel2
    }

    "should handle different values correctly" in {
      val viewModel1 = ContractorLandingViewModel(
        schemeName = "ABC Construction Ltd 1",
        employerReference = "123/AB45678",
        whatIsUrl = "#",
        guidanceUrl = "#",
        penaltiesUrl = "#"
      )

      val viewModel2 = ContractorLandingViewModel(
        schemeName = "ABC Construction Ltd 2",
        employerReference = "456/CD12345",
        whatIsUrl = "#",
        guidanceUrl = "#",
        penaltiesUrl = "#"
      )

      viewModel1 should not be viewModel2
    }
  }
}
