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

package repositories

import base.SpecBase
import models.UnsubmittedMonthlyReturn
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Clock, Instant, ZoneOffset}

class UnsubmittedMonthlyReturnRepositorySpec
    extends SpecBase
    with DefaultPlayMongoRepositorySupport[UnsubmittedMonthlyReturn]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues {

  val fixedInstant: Instant = Instant.parse("2026-04-10T12:00:00Z")
  val fixedClock: Clock     = Clock.fixed(fixedInstant, ZoneOffset.UTC)

  override protected val repository: UnsubmittedMonthlyReturnRepository = newRepository()

  private def newRepository(): UnsubmittedMonthlyReturnRepository =
    GuiceApplicationBuilder()
      .overrides(
        bind[MongoComponent].toInstance(mongoComponent),
        bind[Clock].toInstance(fixedClock)
      )
      .build()
      .injector
      .instanceOf[UnsubmittedMonthlyReturnRepository]

  private val dbData = UnsubmittedMonthlyReturn(
    instanceId = "1",
    monthlyReturnId = 3000L,
    taxYear = 2026,
    taxMonth = 4,
    returnType = "Nil",
    status = "In Progress",
    amendment = Some("Y"),
    deletable = true,
    lastUpdated = fixedInstant
  )

  "get" - {
    "returns None if record does not exist" in {
      repository.get(1000L).futureValue shouldBe None
    }
  }

  "upsert" - {
    "successfully saves and retrieves data" in {
      repository.upsert(dbData).futureValue

      repository.get(dbData.monthlyReturnId).futureValue.value shouldBe dbData
    }

    "updates an existing record for the same monthlyReturnId" in {
      val updated = dbData.copy(
        taxYear = 2027,
        taxMonth = 10
      )

      repository.upsert(dbData).futureValue
      repository.upsert(updated).futureValue

      repository.get(dbData.monthlyReturnId).futureValue.value shouldBe updated
    }
  }
}
