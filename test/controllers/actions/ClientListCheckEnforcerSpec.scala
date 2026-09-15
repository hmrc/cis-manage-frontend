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

package controllers.actions

import base.SpecBase
import models.agent.ClientListCheckPolicy
import models.requests.IdentifierRequest
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.mvc.Results.{BadRequest, Forbidden, Ok}
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, FORBIDDEN, OK}

import scala.concurrent.{ExecutionContext, Future}

class ClientListCheckEnforcerSpec extends SpecBase with MockitoSugar {

  private given ExecutionContext = ExecutionContext.global

  private val policyResolver        = mock[ClientListCheckPolicyResolver]
  private val clientListStatusGuard = mock[ClientListStatusGuard]
  private val hasClientGuard        = mock[HasClientGuard]

  private val enforcer =
    new ClientListCheckEnforcer(
      policyResolver,
      clientListStatusGuard,
      hasClientGuard
    )

  private def request(isAgent: Boolean): IdentifierRequest[AnyContent] =
    IdentifierRequest(
      FakeRequest(),
      "user-id",
      None,
      if isAgent then Some("agent-ref") else None,
      isAgent
    )

  "ClientListCheckEnforcer" - {

    "must bypass checks for a non-agent" in {
      val req = request(isAgent = false)

      val result =
        enforcer(req)(_ => Future.successful(Ok)).futureValue

      result.header.status mustBe OK

      verifyNoInteractions(policyResolver)
      verifyNoInteractions(clientListStatusGuard)
      verifyNoInteractions(hasClientGuard)
    }

    "must bypass checks for GroupB or Exempt" in {
      Seq(
        ClientListCheckPolicy.GroupB,
        ClientListCheckPolicy.Exempt
      ).foreach { policy =>
        reset(policyResolver, clientListStatusGuard, hasClientGuard)

        val req = request(isAgent = true)

        when(policyResolver.resolve(req))
          .thenReturn(policy)

        val result =
          enforcer(req)(_ => Future.successful(Ok)).futureValue

        result.header.status mustBe OK

        verifyNoInteractions(clientListStatusGuard)
        verifyNoInteractions(hasClientGuard)
      }
    }

    "must return the F8 result when the client list check blocks" in {
      val req = request(isAgent = true)

      when(policyResolver.resolve(req))
        .thenReturn(ClientListCheckPolicy.GroupA)

      when(clientListStatusGuard.checkGroupA(req))
        .thenReturn(Future.successful(Some(BadRequest)))

      val result =
        enforcer(req)(_ => Future.successful(Ok)).futureValue

      result.header.status mustBe BAD_REQUEST

      verifyNoInteractions(hasClientGuard)
    }

    "must continue without F7 when central hasClient is not required" in {
      val req = request(isAgent = true)

      when(policyResolver.resolve(req))
        .thenReturn(ClientListCheckPolicy.GroupA)

      when(clientListStatusGuard.checkGroupA(req))
        .thenReturn(Future.successful(None))

      when(policyResolver.shouldRunCentralHasClient(req))
        .thenReturn(false)

      val result =
        enforcer(req)(_ => Future.successful(Ok)).futureValue

      result.header.status mustBe OK

      verifyNoInteractions(hasClientGuard)
    }

    "must return the F7 result when central hasClient blocks" in {
      val req = request(isAgent = true)

      when(policyResolver.resolve(req))
        .thenReturn(ClientListCheckPolicy.GroupA)

      when(clientListStatusGuard.checkGroupA(req))
        .thenReturn(Future.successful(None))

      when(policyResolver.shouldRunCentralHasClient(req))
        .thenReturn(true)

      when(hasClientGuard.check(req))
        .thenReturn(Future.successful(Some(Forbidden)))

      val result =
        enforcer(req)(_ => Future.successful(Ok)).futureValue

      result.header.status mustBe FORBIDDEN
    }

    "must continue when F8 and F7 both pass" in {
      val req = request(isAgent = true)

      when(policyResolver.resolve(req))
        .thenReturn(ClientListCheckPolicy.GroupA)

      when(clientListStatusGuard.checkGroupA(req))
        .thenReturn(Future.successful(None))

      when(policyResolver.shouldRunCentralHasClient(req))
        .thenReturn(true)

      when(hasClientGuard.check(req))
        .thenReturn(Future.successful(None))

      val result =
        enforcer(req)(_ => Future.successful(Ok)).futureValue

      result.header.status mustBe OK
    }
  }
}
