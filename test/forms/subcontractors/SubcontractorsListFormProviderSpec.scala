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

package forms.subcontractors

import forms.behaviours.StringFieldBehaviours

class SubcontractorsListFormProviderSpec extends StringFieldBehaviours {

  private val form = new SubcontractorsListFormProvider()()
  private val fieldName = "searchTerm"

  ".searchTerm" - {

    "bind valid text" in {
      val result = form.bind(Map(fieldName -> "ABC Contractor"))

      result.errors mustBe empty
      result.value mustBe Some("ABC Contractor")
    }

    "bind empty string" in {
      val result = form.bind(Map(fieldName -> ""))

      result.errors mustBe empty
      result.value mustBe Some("")
    }

    "fill and unbind correctly" in {
      val filledForm = form.fill("Test search")

      filledForm(fieldName).value mustBe Some("Test search")
    }
  }
}
