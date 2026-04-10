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
import models.UnsubmittedMonthlyReturn
import play.api.libs.json.JsPath

import java.time.Instant

class UnsubmittedMonthlyReturnToDeleteQuerySpec extends SpecBase {
  "UnsubmittedMonthlyReturnToDeleteQuery" - {

    "have the correct path" in {
      UnsubmittedMonthlyReturnToDeleteQuery.path mustBe (JsPath \ "unsubmittedMonthlyReturnToDelete")
    }

    "have the correct toString" in {
      UnsubmittedMonthlyReturnToDeleteQuery.toString mustBe "UnsubmittedMonthlyReturnToDeleteQuery"
    }

    "set, get, and remove a value in UserAnswers" in {
      val now: Instant = Instant.parse("2026-04-09T12:34:56.789Z")

      val deletableReturn = UnsubmittedMonthlyReturn(
        instanceId = "1",
        monthlyReturnId = 3000L,
        taxYear = 2025,
        taxMonth = 1,
        returnType = "Nil",
        status = "STARTED",
        amendment = Some("Y"),
        deletable = true,
        lastUpdated = now
      )

      val ua1 = emptyUserAnswers.set(UnsubmittedMonthlyReturnToDeleteQuery, deletableReturn).success.value
      ua1.get(UnsubmittedMonthlyReturnToDeleteQuery).value mustBe deletableReturn
      val ua2 = ua1.remove(UnsubmittedMonthlyReturnToDeleteQuery).success.value
      ua2.get(UnsubmittedMonthlyReturnToDeleteQuery) mustBe None
    }
  }
}
