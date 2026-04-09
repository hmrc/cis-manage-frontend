package models

import base.SpecBase
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.Json

import java.time.Instant
import UnsubmittedMonthlyReturn.given

class UnsubmittedMonthlyReturnSpec extends SpecBase {

  val now: Instant = Instant.parse("2026-04-09T12:34:56.789Z")

  "UnsubmittedMonthlyReturn serializes to JSON" in {

    val data = UnsubmittedMonthlyReturn(
      instanceId = "1",
      monthlyReturnId = 3000L,
      taxYear = 2026,
      taxMonth = 4,
      returnType = "Nil",
      status = "In Progress",
      amendment = Some("Y"),
      deletable = true,
      lastUpdated = now
    )

    val json = Json.toJson(data)

    (json \ "instanceId").as[String]    shouldBe "1"
    (json \ "monthlyReturnId").as[Long] shouldBe 3000L
    (json \ "taxYear").as[Int]          shouldBe 2026
    (json \ "taxMonth").as[Int]         shouldBe 4
    (json \ "returnType").as[String]    shouldBe "Nil"
    (json \ "status").as[String]        shouldBe "In Progress"
    (json \ "amendment").asOpt[String]  shouldBe Some("Y")
    (json \ "deletable").as[Boolean]    shouldBe true
    (json \ "lastUpdated").as[Instant]  shouldBe now
  }

  "UnsubmittedMonthlyReturn deserializes from JSON" in {

    val json = Json.obj(
      "instanceId"      -> "1",
      "monthlyReturnId" -> 3000L,
      "taxYear"         -> 2026,
      "taxMonth"        -> 4,
      "returnType"      -> "Nil",
      "status"          -> "In Progress",
      "amendment"       -> "Y",
      "deletable"       -> true,
      "lastUpdated"     -> now
    )

    val result = json.validate[UnsubmittedMonthlyReturn]

    result.isSuccess shouldBe true
    result.get       shouldBe UnsubmittedMonthlyReturn(
      instanceId = "1",
      monthlyReturnId = 3000L,
      taxYear = 2026,
      taxMonth = 4,
      returnType = "Nil",
      status = "In Progress",
      amendment = Some("Y"),
      deletable = true,
      lastUpdated = now
    )
  }

  "Format is symmetrical (round-trip)" in {

    val data = UnsubmittedMonthlyReturn(
      instanceId = "1",
      monthlyReturnId = 3000L,
      taxYear = 2026,
      taxMonth = 4,
      returnType = "Nil",
      status = "In Progress",
      amendment = Some("Y"),
      deletable = true,
      lastUpdated = now
    )

    val json   = Json.toJson(data)
    val parsed = Json.fromJson[UnsubmittedMonthlyReturn](json).get

    parsed shouldBe data
  }
}
