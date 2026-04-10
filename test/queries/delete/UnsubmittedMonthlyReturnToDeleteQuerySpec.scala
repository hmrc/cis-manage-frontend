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
