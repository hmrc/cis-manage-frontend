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

import connectors.ConstructionIndustrySchemeConnector
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.JsValue
import play.api.http.Status.{BAD_GATEWAY, CONFLICT}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

sealed trait ClientListResult

object ClientListResult {
  case object Success extends ClientListResult
  case object InProgress extends ClientListResult
  case object Failed extends ClientListResult
  case object SystemError extends ClientListResult
}

@Singleton
class ClientListService @Inject() (
  connector: ConstructionIndustrySchemeConnector
)(implicit ec: ExecutionContext) {

  import ClientListResult.*

  def start()(implicit hc: HeaderCarrier): Future[ClientListResult] =
    connector
      .start()
      .map { json =>
        (json \ "result").asOpt[String] match {
          case Some("succeeded")   => Success
          case Some("in-progress") => InProgress
          case Some("failed")      => Failed
          case _                   => SystemError
        }
      }
      .recover { case _ =>
        SystemError
      }
}
