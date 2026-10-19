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

package controllers.splitter

import config.FrontendAppConfig
import connectors.RateLimitedAllowListConnector
import models.requests.IdentifierRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait SplitterAllowList {

  protected def config: FrontendAppConfig
  protected def rateLimitedAllowListConnector: RateLimitedAllowListConnector

  protected def checkAllowList[A](request: IdentifierRequest[A])(using HeaderCarrier): Future[Boolean] =
    if (config.useRateLimitedAllowList) {
      val identifier =
        if (request.isAgent) request.agentReference
        else request.employerReference.map(ref => s"${ref.taxOfficeNumber}/${ref.taxOfficeReference}")

      identifier match {
        case Some(id) =>
          rateLimitedAllowListConnector.checkAllowList(config.splitterServiceName, config.splitterAllowListName, id)
        case None     => Future.successful(false)
      }
    } else Future.successful(false)
}
