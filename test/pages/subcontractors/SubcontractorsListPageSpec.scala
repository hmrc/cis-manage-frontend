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

package pages.subcontractors

import models.UserAnswers
import org.scalatest.OptionValues._
import org.scalatest.TryValues._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.JsPath

class SubcontractorsListPageSpec extends AnyFreeSpec with Matchers {

  "SubcontractorsListPage" - {

    "must be retrievable" in {
      val answers =
        UserAnswers("id")
          .set(SubcontractorsListPage, "test-value")
          .success
          .value

      answers.get(SubcontractorsListPage).value mustBe "test-value"
    }

    "must be settable" in {
      val answers =
        UserAnswers("id")
          .set(SubcontractorsListPage, "another-value")
          .success
          .value

      answers.get(SubcontractorsListPage).value mustBe "another-value"
    }

    "must be removable" in {
      val answers =
        UserAnswers("id")
          .set(SubcontractorsListPage, "test-value")
          .success
          .value
          .remove(SubcontractorsListPage)
          .success
          .value

      answers.get(SubcontractorsListPage) mustBe None
    }

    "must have the correct toString value" in {
      SubcontractorsListPage.toString mustBe "manageYourSubcontractors"
    }

    "must have the correct path" in {
      SubcontractorsListPage.path mustBe (JsPath \ "manageYourSubcontractors")
    }
  }
}
