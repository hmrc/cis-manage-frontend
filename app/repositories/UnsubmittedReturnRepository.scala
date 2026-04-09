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

package repositories

import config.FrontendAppConfig
import models.UnsubmittedReturn
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.*
import play.api.libs.json.Format
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UnsubmittedReturnRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: FrontendAppConfig,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[UnsubmittedReturn](
      collectionName = "unsubmitted-returns",
      mongoComponent = mongoComponent,
      domainFormat = UnsubmittedReturn.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
        )
      )
    ) {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private def byMonthlyReturnId(id: Long): Bson = Filters.eq("_id", id)

  def get(monthlyReturnId: Long): Future[Option[UnsubmittedReturn]] =
    collection.find(byMonthlyReturnId(monthlyReturnId)).headOption()

  def upsert(returnItem: UnsubmittedReturn): Future[Unit] = {
    val updated = returnItem.copy(lastUpdated = Instant.now(clock))

    collection
      .replaceOne(
        filter = byMonthlyReturnId(updated.monthlyReturnId),
        replacement = updated,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => ())
  }
}
