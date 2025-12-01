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

class UniqueTaxReferencePageSpec extends SpecBase {

  "UniqueTaxReferencePage" - {

    "have the correct path" in {
      UniqueTaxReferencePage.path mustBe (JsPath \ "utr")
    }

    "have the correct toString" in {
      UniqueTaxReferencePage.toString mustBe "utr"
    }

    "set, get, and remove a value in UserAnswers" in {
      val ua1 = UserAnswers("test")
        .set(UniqueTaxReferencePage, "1234567890")
        .success
        .value

      ua1.get(UniqueTaxReferencePage).value mustBe "1234567890"

      val ua2 = ua1.remove(UniqueTaxReferencePage).success.value
      ua2.get(UniqueTaxReferencePage) mustBe None
    }
  }
}
