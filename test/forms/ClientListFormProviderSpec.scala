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

package forms

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.FormError
import viewmodels.agent.SearchByList

class ClientListFormProviderSpec extends StringFieldBehaviours {

  val form = new ClientListFormProvider()()

  ".searchBy" - {

    val fieldName   = "searchBy"
    val requiredKey = "agent.clientListSearch.searchBy.error.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(SearchByList.searchByOptions.map(_.value))
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      arbitrary[String].suchThat(_.nonEmpty).suchThat(!SearchByList.searchByOptions.map(_.value).contains(_)),
      FormError(fieldName, requiredKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

  }

  ".searchFilter" - {

    val requiredKey = "agent.clientListSearch.searchFilter.error.required"
    val lengthKey   = "agent.clientListSearch.searchFilter.error.length"
    val invalidKey  = "agent.clientListSearch.searchFilter.error.format"
    val maxLength   = 35
    val fieldName   = "searchFilter"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like checkForMaxLengthAndInvalid(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      invalidError = FormError(fieldName, invalidKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

  }

}
