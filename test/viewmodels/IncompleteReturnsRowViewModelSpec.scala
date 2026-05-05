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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class IncompleteReturnsRowViewModelSpec extends AnyWordSpec with Matchers {

  "IncompleteReturnsRowViewModel" should {
    "create a row view model" in {
      val action = ActionLinkViewModel(
        textKey = "incompleteReturns.action.continue",
        href = "/continue",
        hiddenTextKey = Some("Continue return")
      )

      val model = IncompleteReturnsRowViewModel(
        returnPeriodEnd = "Jan 2025",
        returnType = "Nil",
        lastUpdate = "01 Jan 2025",
        status = "In progress",
        action = Seq(action),
        amendment = Some("N")
      )

      model.returnPeriodEnd mustBe "Jan 2025"
      model.returnType mustBe "Nil"
      model.lastUpdate mustBe "01 Jan 2025"
      model.status mustBe "In progress"
      model.action mustBe Seq(action)
      model.amendment mustBe Some("N")
    }
  }

  "ActionLinkViewModel" should {
    "create an action link view model" in {
      val model = ActionLinkViewModel(
        textKey = "incompleteReturns.action.delete",
        href = "/delete",
        hiddenTextKey = Some("Delete return")
      )

      model.textKey mustBe "incompleteReturns.action.delete"
      model.href mustBe "/delete"
      model.hiddenTextKey mustBe Some("Delete return")
    }

    "default hiddenTextKey to None" in {
      val model = ActionLinkViewModel(
        textKey = "incompleteReturns.action.view",
        href = "/view"
      )

      model.hiddenTextKey mustBe None
    }
  }
}
