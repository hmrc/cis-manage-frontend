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

package services

import connectors.ConstructionIndustrySchemeConnector
import models.requests.GetSubmittedVerificationsRequest
import models.verify.VerificationHistoryData
import play.api.Logging
import repositories.VerificationHistoryCache
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@Singleton
class VerificationService @Inject() (
  cache: VerificationHistoryCache,
  connector: ConstructionIndustrySchemeConnector,
  verificationHistoryBuilder: VerificationHistoryService
)(using ExecutionContext)
    extends Logging {

  def getSubmittedVerifications(instanceId: String)(implicit hc: HeaderCarrier): Future[VerificationHistoryData] =
    cache
      .getFromCache(instanceId)
      .flatMap {
        case Some(verificationHistory) =>
          logger.info(s"Fetching from cache; found history for CIS ID <$instanceId>")
          Future.successful(verificationHistory)
        case None                      =>
          logger.info(s"Fetching with connector; cache miss for CIS ID <$instanceId>")
          fetchHistoryAndPutInCache(instanceId)
      }
      .recoverWith { ex =>
        logger.warn(s"Falling back to connector; cache threw exception for CIS ID <$instanceId>: $ex")
        fetchHistoryAndPutInCache(instanceId)
      }

  private def fetchHistoryAndPutInCache(instanceId: String)(using HeaderCarrier) =
    connector
      .getSubmittedVerifications(GetSubmittedVerificationsRequest(instanceId))
      .map { historyResponse =>
        val historyData = verificationHistoryBuilder.toVerificationHistoryData(historyResponse)
        cache
          .putCache(instanceId)(historyData)
          .andThen { case Failure(ex) => logger.warn(s"Failed to cache history for CIS ID <$instanceId>: $ex") }
        historyData
      }
      .andThen { case Failure(ex) => logger.error(s"Failed to fetch history for CIS ID <$instanceId>: $ex") }
}
