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

import base.SpecBase
import models.response.GetSubcontractorListResponse
import play.api.libs.json.JsPath

class SubcontractorListPageSpec extends SpecBase {

  "SubcontractorListPage" - {

    "must have the expected path" in {
      SubcontractorListPage.path mustEqual (JsPath \ "subcontractorList")
    }

    "must have the expected name" in {
      SubcontractorListPage.toString mustEqual "subcontractorList"
    }

    "must serialise and deserialise a subcontractor list response" in {
      val response =
        GetSubcontractorListResponse(
          subcontractors = Seq.empty
        )

      val json =
        SubcontractorListPage.format.writes(response)

      SubcontractorListPage.format.reads(json).get mustEqual response
    }
  }
}