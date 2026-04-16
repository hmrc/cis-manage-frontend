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

package controllers.actions

import base.SpecBase
import controllers.Execution.trampoline
import models.requests.DataRequest
import models.UnsubmittedMonthlyReturn
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.FakeRequest
import queries.delete.UnsubmittedMonthlyReturnToDeleteQuery

import java.time.Instant
import scala.concurrent.Future

class DeletableReturnActionSpec extends SpecBase with MockitoSugar {

  object Harness extends DeletableReturnActionImpl {
    def callRefine[A](request: DataRequest[A]): Future[Either[Result, DeletableReturnRequest[A]]] =
      refine(request)
  }

  "DeletableReturnAction" - {

    val now: Instant = Instant.parse("2026-04-09T12:34:56.789Z")

    "when return exists and is deletable" - {
      "must return Right with DeletableReturnRequest" in {

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

        val userAnswers = emptyUserAnswers
          .set(UnsubmittedMonthlyReturnToDeleteQuery, deletableReturn)
          .success
          .value

        val result = Harness.callRefine(DataRequest(FakeRequest(), "id", userAnswers))

        whenReady(result) { result =>
          result.isRight mustBe true

          val request = result.toOption.value
          request.returnToDelete mustBe deletableReturn
        }
      }
    }

    "when return exists but is NOT deletable" - {
      "must return Left and redirect to JourneyRecovery" in {

        val nonDeletableReturn = UnsubmittedMonthlyReturn(
          instanceId = "1",
          monthlyReturnId = 3000L,
          taxYear = 2025,
          taxMonth = 1,
          returnType = "Nil",
          status = "STARTED",
          amendment = Some("Y"),
          deletable = false,
          lastUpdated = now
        )

        val userAnswers = emptyUserAnswers
          .set(UnsubmittedMonthlyReturnToDeleteQuery, nonDeletableReturn)
          .success
          .value

        val result = Harness.callRefine(DataRequest(FakeRequest(), "id", userAnswers))

        whenReady(result) { result =>
          result.isLeft mustBe true
        }
      }
    }

    "when return is missing" - {
      "must return Left and redirect to JourneyRecovery" in {

        val result = Harness.callRefine(DataRequest(FakeRequest(), "id", emptyUserAnswers))

        whenReady(result) { result =>
          result.isLeft mustBe true
        }
      }
    }
  }
}
