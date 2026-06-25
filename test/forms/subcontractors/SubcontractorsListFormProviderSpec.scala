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

import forms.Validation
import forms.behaviours.StringFieldBehaviours

class SubcontractorsListFormProviderSpec extends StringFieldBehaviours {

  private val form      = new SubcontractorsListFormProvider()()
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

    s"not bind when length exceeds ${Validation.subcontractorSearchMaxLength} characters" in {
      val invalidValue = "a" * (Validation.subcontractorSearchMaxLength + 1)

      val result = form.bind(Map(fieldName -> invalidValue))

      result.errors must have length 1
      result.errors.head.key mustBe fieldName
      result.errors.head.message mustBe "subcontractors.subcontractorsList.search.error.length"
    }

    "bind when length is exactly 35 characters" in {
      val validValue = "a" * Validation.subcontractorSearchMaxLength

      val result = form.bind(Map(fieldName -> validValue))

      result.errors mustBe empty
      result.value mustBe Some(validValue)
    }

    "fill and unbind correctly" in {
      val filledForm = form.fill("Test search")

      filledForm(fieldName).value mustBe Some("Test search")
    }

    "bind valid characters allowed by regex" in {
      val result =
        form.bind(Map(fieldName -> "ABC Contractor 123, Ltd"))

      result.errors mustBe empty
      result.value mustBe Some("ABC Contractor 123, Ltd")
    }

    "not bind when search term contains invalid characters" in {
      val result =
        form.bind(Map(fieldName -> "ABC@Contractor"))

      result.errors must have length 1
      result.errors.head.key mustBe fieldName
      result.errors.head.message mustBe
        "subcontractors.subcontractorsList.search.error.invalid"
    }

    "not bind when search term contains special characters" in {
      val result =
        form.bind(Map(fieldName -> "ABC#Contractor"))

      result.errors must have length 1
      result.errors.head.message mustBe
        "subcontractors.subcontractorsList.search.error.invalid"
    }
  }
}
