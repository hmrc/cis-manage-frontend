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
final class MockCisControllerComponents private (
  mcc: MessagesControllerComponents,
  mockIdentifierAction: MockIdentifierAction,
  dataRetrievalAction: DataRetrievalAction,
  hasClientGuard: HasClientGuard
)(using ExecutionContext)
    extends CisControllerComponents(
      mcc.messagesActionBuilder,
      mcc.actionBuilder.asInstanceOf[DefaultActionBuilder],
      mcc.parsers,
      mcc.messagesApi,
      mcc.langs,
      mcc.fileMimeTypes,
      mockIdentifierAction,
      dataRetrievalAction,
      new DataRequiredActionImpl(),
      new CisIdRequiredActionImpl(),
      hasClientGuard
    ) {
  def loginAsOrg(ton: String = "123", tor: String = "AB12345"): Unit =
    mockIdentifierAction.setUser(isAgent = false, agentRef = "", ton, tor)

  def loginAsAgent(ref: String = "123456"): Unit =
    mockIdentifierAction.setUser(isAgent = true, agentRef = ref, ton = "", tor = "")
}
object MockCisControllerComponents extends MockitoSugar {
  def apply(sessionRepo: SessionRepository)(using ExecutionContext): MockCisControllerComponents =
    val mcc                  = stubMessagesControllerComponents()
    val mockIdentifierAction = new MockIdentifierAction(mcc.parsers.default)

    val mockAuditService = mock[AuditService]
    when(mockAuditService.sendEvent(any)(any, any, any)) thenReturn Future.successful(Success)

    val mockCisService = mock[ConstructionIndustrySchemeService]
    when(mockCisService.hasClient(any, any)(any)) thenReturn Future.successful(true)

    val mockDataRetrievalAction = new DataRetrievalActionImpl(sessionRepo)
    val mockHasClientGuard      = new HasClientGuard(mockCisService, sessionRepo, mockAuditService)

    new MockCisControllerComponents(mcc, mockIdentifierAction, mockDataRetrievalAction, mockHasClientGuard)
}
