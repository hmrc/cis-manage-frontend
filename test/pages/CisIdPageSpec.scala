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

package pages

import base.SpecBase
import models.UserAnswers
import org.scalatest.OptionValues.*
import org.scalatest.TryValues.*
import play.api.libs.json.JsPath

class CisIdPageSpec extends SpecBase {

  "CisIdPage" - {

    "have the correct path" in {
      CisIdPage.path mustBe (JsPath \ "cisId")
    }

    "have the correct toString" in {
      CisIdPage.toString mustBe "cisId"
    }

    "set, get, and remove a value in UserAnswers" in {
      val ua1 = UserAnswers("test").set(CisIdPage, "cisId").success.value
      ua1.get(CisIdPage).value mustBe "cisId"
      val ua2 = ua1.remove(CisIdPage).success.value
      ua2.get(CisIdPage) mustBe None
    }
  }
}
