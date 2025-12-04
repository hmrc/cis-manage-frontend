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

class ReturnLandingViewModelSpec extends SpecBase {

  "ReturnLandingViewModel" - {
    "should create a return landing view model with all required fields" in {
      val returnLandingViewModel = ReturnLandingViewModel(
        taxMonth = "August 2025",
        returnType = "Standard",
        dateSubmitted = "19 September 2025",
        status = "Accepted"
      )

      returnLandingViewModel.taxMonth mustEqual "August 2025"
      returnLandingViewModel.returnType mustEqual "Standard"
      returnLandingViewModel.dateSubmitted mustEqual "19 September 2025"
      returnLandingViewModel.status mustEqual "Accepted"
    }

    "should support case class copy" in {
      val original = ReturnLandingViewModel(
        taxMonth = "August 2025",
        returnType = "Standard",
        dateSubmitted = "19 September 2025",
        status = "Accepted"
      )

      val modified = original.copy(taxMonth = "September 2005", dateSubmitted = "20 October 2025")

      modified.taxMonth mustEqual "September 2005"
      modified.returnType mustEqual "Standard"
      modified.dateSubmitted mustEqual "20 October 2025"
      modified.status mustEqual "Accepted"
    }
  }
}
