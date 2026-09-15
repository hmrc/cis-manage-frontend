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
import models.agent.ClientListStatus
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Call}
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, SEE_OTHER}
import services.ConstructionIndustrySchemeService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ClientListStatusGuardSpec extends SpecBase with MockitoSugar {

  private given ExecutionContext = ExecutionContext.global

  private val cisService = mock[ConstructionIndustrySchemeService]

  private val guard =
    new ClientListStatusGuard(cisService)

  private def request(isAgent: Boolean): IdentifierRequest[AnyContent] =
    IdentifierRequest(
      FakeRequest(),
      "user-id",
      None,
      if isAgent then Some("agent-ref") else None,
      isAgent,
      None,
      None
    )

  "ClientListStatusGuard" - {

    "must continue for GroupA when retrieval succeeds" in {
      when(
        cisService.startClientListRetrieval(using any[HeaderCarrier])
      ).thenReturn(Future.successful(ClientListStatus.Succeeded))

      guard.checkGroupA(request(isAgent = true)).futureValue mustBe None
    }

    "must redirect to AgentLostAccess for a non-success GroupA status" in {
      Seq(
        ClientListStatus.InProgress,
        ClientListStatus.Failed,
        ClientListStatus.InitiateDownload
      ).foreach { status =>
        when(
          cisService.startClientListRetrieval(using any[HeaderCarrier])
        ).thenReturn(Future.successful(status))

        val result =
          guard.checkGroupA(request(isAgent = true)).futureValue

        result.value.header.status mustBe SEE_OTHER
        result.value.header.headers.get(LOCATION) mustBe
          Some(controllers.agent.routes.AgentLostAccessController.onPageLoad().url)
      }
    }

    "must redirect to the security check for GroupB when retrieval is in progress" in {
      val securityCheckCall = Call("GET", "/security-check")

      when(
        cisService.startClientListRetrieval(using any[HeaderCarrier])
      ).thenReturn(Future.successful(ClientListStatus.InProgress))

      val result =
        guard
          .groupB(securityCheckCall)
          .invokeBlock(
            request(isAgent = true),
            _ => Future.successful(Ok)
          )
          .futureValue

      result.header.status mustBe SEE_OTHER
      result.header.headers.get(LOCATION) mustBe Some("/security-check")
    }

    "must continue for GroupB when retrieval succeeds" in {
      when(
        cisService.startClientListRetrieval(using any[HeaderCarrier])
      ).thenReturn(Future.successful(ClientListStatus.Succeeded))

      val result =
        guard
          .groupB(Call("GET", "/security-check"))
          .invokeBlock(
            request(isAgent = true),
            _ => Future.successful(Ok)
          )
          .futureValue

      result.header.status mustBe OK
    }

    "must redirect to system error when the check fails" in {
      when(
        cisService.startClientListRetrieval(using any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val result =
        guard.checkGroupA(request(isAgent = true)).futureValue

      result.value.header.status mustBe SEE_OTHER
      result.value.header.headers.get(LOCATION) mustBe
        Some(controllers.routes.SystemErrorController.onPageLoad().url)
    }
  }
}
