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

package base

import base.MockCisControllerComponents.*
import controllers.CisControllerComponents
import controllers.actions.*
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.*
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{AuditService, ConstructionIndustrySchemeService}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success

import scala.concurrent.{ExecutionContext, Future}

/** Mocks up all our shared CIS controller dependencies for easy unit-testing without having to spin up an entire
  * application with [[org.scalatestplus.play.guice.GuiceOneAppPerSuite]].
  */
final class MockCisControllerComponents(using ExecutionContext)
    extends CisControllerComponents(
      mcc.messagesActionBuilder,
      mcc.actionBuilder.asInstanceOf[DefaultActionBuilder],
      mcc.parsers,
      mcc.messagesApi,
      mcc.langs,
      mcc.fileMimeTypes,
      new FakeIdentifierAction(isAgent = false)(mcc.parsers),
      new DataRetrievalActionImpl(sessionRepo),
      new DataRequiredActionImpl(),
      new CisIdRequiredActionImpl(),
      new HasClientGuard(cisService, sessionRepo, auditService)
    ) {
  def setUserAnswers(userAnswersOpt: Option[UserAnswers]): Unit =
    when(sessionRepo.get(any)) thenReturn Future.successful(userAnswersOpt)
}
object MockCisControllerComponents extends MockitoSugar {
  private val mcc = stubMessagesControllerComponents()

  private val auditService = mock[AuditService]
  when(auditService.sendEvent(any)(any, any, any)) thenReturn Future.successful(Success)

  private val sessionRepo = mock[SessionRepository]
  when(sessionRepo.set(any)) thenReturn Future.successful(true)

  private val cisService = mock[ConstructionIndustrySchemeService]
  when(cisService.hasClient(any, any)(any)) thenReturn Future.successful(true)
}
