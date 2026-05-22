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

package forms.clientdetails

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ChangeClientReferenceFormProviderSpec extends StringFieldBehaviours {

  val requiredKey            = "clientdetails.changeClientReference.error.required"
  val invalidCharactersError = "clientdetails.changeClientReference.error.invalidCharacters"

  val form = new ChangeClientReferenceFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind valid data" in {
      val validChangeClientReferences: Seq[String] = Seq(
        "Alpha",
        "A&B"
      )
      validChangeClientReferences.foreach { value =>
        val result = form.bind(Map(fieldName -> value))
        result.errors must be(empty)
      }
    }
    "must not bind invalid characters" in {
      val invalidChangeClientReferences = Seq(
        "Hello!",
        "Name_123",
        "100%329424230948230948902384092384902384234",
        "Café"
      )
      invalidChangeClientReferences.foreach { value =>
        val result = form.bind(Map(fieldName -> value))
        result.errors.map(_.message) must contain(invalidCharactersError)
      }
    }
  }
}
