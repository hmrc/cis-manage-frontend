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

package queries.delete

import base.SpecBase
import models.history.SubmittedMonthlyReturnData
import play.api.libs.json.JsPath
import queries.SubmittedMonthlyReturnToPrintQuery

class SubmittedMonthlyReturnToPrintQuerySpec extends SpecBase {
  "UnsubmittedMonthlyReturnToDeleteQuery" - {

    "have the correct path" in {
      SubmittedMonthlyReturnToPrintQuery.path mustBe (JsPath \ "submittedMonthlyReturnToPrint")
    }

    "have the correct toString" in {
      SubmittedMonthlyReturnToPrintQuery.toString mustBe "SubmittedMonthlyReturnToPrintQuery"
    }

    "set, get, and remove a value in UserAnswers" in {

      val deletableReturn = SubmittedMonthlyReturnData(
        monthlyReturnId = 1L,
        taxYear = 2025,
        taxMonth = 1,
        nilReturnIndicator = "Y",
        status = "SUBMITTED",
        supersededBy = None,
        amendmentStatus = None,
        monthlyReturnItems = None
      )

      val ua1 = emptyUserAnswers.set(SubmittedMonthlyReturnToPrintQuery, deletableReturn).success.value
      ua1.get(SubmittedMonthlyReturnToPrintQuery).value mustBe deletableReturn
      val ua2 = ua1.remove(SubmittedMonthlyReturnToPrintQuery).success.value
      ua2.get(SubmittedMonthlyReturnToPrintQuery) mustBe None
    }
  }
}
