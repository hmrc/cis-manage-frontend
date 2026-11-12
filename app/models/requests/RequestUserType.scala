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

package models.requests

import controllers.actions.DeletableReturnRequest
import play.api.libs.typedmap.TypedKey
import play.api.mvc.RequestHeader

object RequestUserType {

  /** Marks a request as coming from an agent on pages that are not behind an identifier action. */
  val IsAgentKey: TypedKey[Boolean] = TypedKey[Boolean]("isAgent")

  /** Marks a request on a public page as having no signed-in user, so no account link should be shown. */
  val IsSignedOutKey: TypedKey[Boolean] = TypedKey[Boolean]("isSignedOut")

  def isSignedOut(rh: RequestHeader): Boolean = rh.attrs.get(IsSignedOutKey).contains(true)

  def isAgent(rh: RequestHeader): Boolean = rh match {
    case r: IdentifierRequest[?]      => r.isAgent
    case r: OptionalDataRequest[?]    => r.isAgent
    case r: DataRequestFields[?]      => r.isAgent
    case r: DeletableReturnRequest[?] => isAgent(r.request)
    case _                            => rh.attrs.get(IsAgentKey).contains(true)
  }
}
