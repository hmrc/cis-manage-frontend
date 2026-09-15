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

package controllers

import com.google.inject.{Inject, Singleton}
import controllers.actions.*
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{DefaultActionBuilder, MessagesActionBuilder, MessagesControllerComponents, PlayBodyParsers}

import scala.concurrent.ExecutionContext

/** Serves the same purpose as [[MessagesControllerComponents]] but also supplies extra Actions for CIS. */
@Singleton
class CisControllerComponents @Inject() (
  val messagesActionBuilder: MessagesActionBuilder,
  val actionBuilder: DefaultActionBuilder,
  val parsers: PlayBodyParsers,
  val messagesApi: MessagesApi,
  val langs: Langs,
  val fileMimeTypes: FileMimeTypes,
  val identify: IdentifierAction,
  val getData: DataRetrievalAction,
  val requireData: DataRequiredAction,
  val requiredCisId: CisIdRequiredAction,
  val hasClientGuard: HasClientGuard
)(using val executionContext: ExecutionContext)
    extends MessagesControllerComponents
