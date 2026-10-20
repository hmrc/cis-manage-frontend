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

import models.EmployerReference
import models.requests.{IdentifierRequest, OptionalIdentifierRequest}
import play.api.mvc.*

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FakeSplitterAction @Inject() (
  isAgent: Boolean,
  agentCode: Option[String] = Some("agentCode"),
  itmpName: Option[String] = None
)(
  bodyParsers: PlayBodyParsers
) extends OptionalIdentifierAction {

  override def invokeBlock[A](
    request: Request[A],
    block: OptionalIdentifierRequest[A] => Future[Result]
  ): Future[Result] =
    if (isAgent) {
      block(
        OptionalIdentifierRequest(
          request,
          Some(IdentifierRequest(request, "id", None, Some("agentReferenceNumber"), true, agentCode, itmpName))
        )
      )
    } else {
      block(
        OptionalIdentifierRequest(
          request,
          Some(IdentifierRequest(request, "id", Some(EmployerReference("taxOfficeNumber", "taxOfficeReference")), None))
        )
      )
    }

  override def parser: BodyParser[AnyContent] =
    bodyParsers.default

  override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
