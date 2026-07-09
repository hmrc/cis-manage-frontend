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
import models.UserAnswers
import models.subcontractors.DeleteSubcontractorJourneyData
import play.api.libs.json.JsPath

class DeleteSubcontractorJourneyPageSpec extends SpecBase {

  "DeleteSubcontractorJourneyPage" - {

    "must have the correct path" in {
      DeleteSubcontractorJourneyPage.path mustBe
        (JsPath \ "deleteSubcontractorJourney")
    }

    "must allow a value to be set and retrieved" in {

      val journeyData =
        DeleteSubcontractorJourneyData(
          subcontractorName = "Gamma Builders",
          subbieResourceRef = 10L,
          subcontractorCanBeDeleted = true
        )

      val answers =
        UserAnswers(userAnswersId)
          .set(DeleteSubcontractorJourneyPage, journeyData)
          .success
          .value

      answers.get(DeleteSubcontractorJourneyPage) mustBe Some(journeyData)
    }

    "must return None when no value has been set" in {

      val answers = UserAnswers(userAnswersId)

      answers.get(DeleteSubcontractorJourneyPage) mustBe None
    }

    "must overwrite an existing value" in {

      val original =
        DeleteSubcontractorJourneyData(
          subcontractorName = "Gamma Builders",
          subbieResourceRef = 10L,
          subcontractorCanBeDeleted = true
        )

      val updated =
        DeleteSubcontractorJourneyData(
          subcontractorName = "Delta Group",
          subbieResourceRef = 20L,
          subcontractorCanBeDeleted = false
        )

      val answers =
        UserAnswers(userAnswersId)
          .set(DeleteSubcontractorJourneyPage, original)
          .success
          .value
          .set(DeleteSubcontractorJourneyPage, updated)
          .success
          .value

      answers.get(DeleteSubcontractorJourneyPage) mustBe Some(updated)
    }
  }
}
