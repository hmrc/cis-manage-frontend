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
import models.agent.ClientListCheckPolicy.{Exempt, GroupA, GroupB}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.routing.{HandlerDef, Router}
import play.api.test.FakeRequest

class ClientListCheckPolicyResolverSpec extends SpecBase {

  private val resolver = new ClientListCheckPolicyResolver()

  private def request(
    method: String,
    controller: String,
    action: String
  ) = {
    val handlerDef = mock[HandlerDef]

    when(handlerDef.controller).thenReturn(controller)
    when(handlerDef.method).thenReturn(action)

    FakeRequest(method, "/test")
      .addAttr(Router.Attrs.HandlerDef, handlerDef)
  }

  "ClientListCheckPolicyResolver" - {

    "must return GroupB for an explicit GroupB route" in {
      resolver.resolve(
        request(
          "GET",
          "controllers.agent.AgentLandingController",
          "onPageLoad"
        )
      ) mustBe GroupB
    }

    "must return GroupA for an explicit GroupA route" in {
      resolver.resolve(
        request(
          "GET",
          "controllers.history.SubmittedReturnsController",
          "onPageLoadSingleYear"
        )
      ) mustBe GroupA
    }

    "must return GroupA for a generic GET onPageLoad route" in {
      resolver.resolve(
        request(
          "GET",
          "controllers.SomeController",
          "onPageLoad"
        )
      ) mustBe GroupA
    }

    "must return Exempt for an exempt controller" in {
      resolver.resolve(
        request(
          "GET",
          "controllers.SystemErrorController",
          "onPageLoad"
        )
      ) mustBe Exempt
    }

    "must return Exempt for a non-GET request" in {
      resolver.resolve(
        request(
          "POST",
          "controllers.SomeController",
          "onPageLoad"
        )
      ) mustBe Exempt
    }

    "must return Exempt for an unmatched GET route" in {
      resolver.resolve(
        request(
          "GET",
          "controllers.SomeController",
          "onSubmit"
        )
      ) mustBe Exempt
    }

    "must not run central hasClient for an exempt route" in {
      resolver.shouldRunCentralHasClient(
        request(
          "GET",
          "controllers.CheckSubcontractorRecordsController",
          "onPageLoad"
        )
      ) mustBe false
    }

    "must run central hasClient for a normal route" in {
      resolver.shouldRunCentralHasClient(
        request(
          "GET",
          "controllers.SomeController",
          "onPageLoad"
        )
      ) mustBe true
    }
  }
}
