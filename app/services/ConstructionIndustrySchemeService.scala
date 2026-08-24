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

package services

import com.google.inject.{Inject, Singleton}
import connectors.ConstructionIndustrySchemeConnector
import models.agent.ClientListStatus
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConstructionIndustrySchemeService @Inject() (cisConnector: ConstructionIndustrySchemeConnector)(using
  ExecutionContext
) extends Logging {

  def startClientListRetrieval(using HeaderCarrier): Future[ClientListStatus] =
    cisConnector.startClientList
      .map(_.result)

  def getClientListStatus(using HeaderCarrier): Future[ClientListStatus] =
    cisConnector.getClientListStatus
      .map(_.result)

  def hasClient(
    taxOfficeNumber: String,
    taxOfficeReference: String
  )(using HeaderCarrier): Future[Boolean] =
    cisConnector
      .hasClient(taxOfficeNumber, taxOfficeReference)
      .map(_.hasClient)
}
