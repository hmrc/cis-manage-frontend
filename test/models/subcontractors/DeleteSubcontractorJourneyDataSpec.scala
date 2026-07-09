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

package models.subcontractors

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class DeleteSubcontractorJourneyDataSpec extends AnyFreeSpec with Matchers {

  "DeleteSubcontractorJourneyData" - {

    "must serialise to JSON correctly" in {

      val model =
        DeleteSubcontractorJourneyData(
          subcontractorName = "Gamma Builders",
          subbieResourceRef = 10L,
          subcontractorCanBeDeleted = true
        )

      Json.toJson(model) mustBe Json.obj(
        "subcontractorName"         -> "Gamma Builders",
        "subbieResourceRef"         -> 10L,
        "subcontractorCanBeDeleted" -> true
      )
    }

    "must deserialise from JSON correctly" in {

      val json = Json.obj(
        "subcontractorName"         -> "Gamma Builders",
        "subbieResourceRef"         -> 10L,
        "subcontractorCanBeDeleted" -> false
      )

      json.as[DeleteSubcontractorJourneyData] mustBe
        DeleteSubcontractorJourneyData(
          subcontractorName = "Gamma Builders",
          subbieResourceRef = 10L,
          subcontractorCanBeDeleted = false
        )
    }

    "must support round-trip JSON conversion" in {

      val model =
        DeleteSubcontractorJourneyData(
          subcontractorName = "Gamma Builders",
          subbieResourceRef = 10L,
          subcontractorCanBeDeleted = true
        )

      Json
        .toJson(model)
        .as[DeleteSubcontractorJourneyData] mustBe model
    }

    "must fail to deserialise when subcontractorName is missing" in {

      val json = Json.obj(
        "subbieResourceRef"         -> 10L,
        "subcontractorCanBeDeleted" -> true
      )

      json.validate[DeleteSubcontractorJourneyData].isError mustBe true
    }

    "must fail to deserialise when subbieResourceRef is missing" in {

      val json = Json.obj(
        "subcontractorName"         -> "Gamma Builders",
        "subcontractorCanBeDeleted" -> true
      )

      json.validate[DeleteSubcontractorJourneyData].isError mustBe true
    }

    "must fail to deserialise when subcontractorCanBeDeleted is missing" in {

      val json = Json.obj(
        "subcontractorName" -> "Gamma Builders",
        "subbieResourceRef" -> 10L
      )

      json.validate[DeleteSubcontractorJourneyData].isError mustBe true
    }
  }
}
