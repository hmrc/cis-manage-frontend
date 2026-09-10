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

package views

import base.SpecBase
import play.api.data.Form
import play.api.data.Forms.text
import play.api.i18n.Messages
import views.ViewUtils.*

class ViewUtilsSpec extends SpecBase {

  "withPagination" - {

    "must append the current and total page numbers when there is more than one page" in {
      implicit val msgs: Messages = messages(app)

      withPagination("Your subcontractors", currentPage = 4, totalPages = 10) mustBe
        "Your subcontractors (page 4 of 10)"
    }

    "must not append page numbers when there is only one page" in {
      implicit val msgs: Messages = messages(app)

      withPagination("Your subcontractors", currentPage = 1, totalPages = 1) mustBe
        "Your subcontractors"
    }
  }

  "title with pagination" - {

    "must include the page numbers, service name and GOV.UK" in {
      implicit val msgs: Messages = messages(app)
      val form: Form[String]      = Form("value" -> text)

      title(
        form,
        withPagination(msgs("subcontractors.subcontractorsList.title"), currentPage = 4, totalPages = 10)
      ) must include(
        "Your subcontractors (page 4 of 10) - Construction Industry Scheme - GOV.UK"
      )
    }
  }
}
