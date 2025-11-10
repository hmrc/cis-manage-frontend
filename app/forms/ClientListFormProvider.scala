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

import forms.mappings.Mappings
import models.agent.ClientListFormData
import play.api.data.Form
import play.api.data.Forms.mapping
import viewmodels.agent.SearchByList

import javax.inject.Inject

class ClientListFormProvider @Inject() extends Mappings {

  private val searchFilterMaxLength = 35

  def apply(): Form[ClientListFormData] = Form(
    mapping(
      "searchBy"     -> text("agent.clientListSearch.searchBy.error.required")
        .verifying(
          "agent.clientListSearch.searchBy.error.required",
          value => SearchByList.searchByOptions.exists(_.value == value)
        ),
      "searchFilter" -> text("agent.clientListSearch.searchFilter.error.required")
        .verifying(
          firstError(
            maxLength(searchFilterMaxLength, "agent.clientListSearch.searchFilter.error.length"),
            nonEmptyString("searchFilter", "agent.clientListSearch.searchFilter.error.required"),
            regexp(Validation.textInputPattern.toString, "agent.clientListSearch.searchFilter.error.format")
          )
        )
    )(ClientListFormData.apply)(x => Some(x.searchBy, x.searchFilter))
  )

}
