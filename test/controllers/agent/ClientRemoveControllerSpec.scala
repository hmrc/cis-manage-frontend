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

package controllers.agent

import base.SpecBase
import models.{CisTaxpayerSearchResult, NormalMode, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import pages.{AgentClientsPage, CisIdPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.AgentService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ClientRemoveControllerSpec extends SpecBase with MockitoSugar {

  val employerRef = "123456"

  val okResponse =
    CisTaxpayerSearchResult(
      uniqueId = "123",
      taxOfficeNumber = "111",
      taxOfficeRef = "test111",
      agentOwnRef = Option("TEST LTD"),
      schemeName = Option("ABCD"),
      utr = Option("ABCD")
    )

  "ClientRemoveControllerSpec" - {

    "must redirect to ClientRemoveYesNoController when clientSearchResultByEmpRef size is 1" in {

      val mockAgentService = mock[AgentService]
      when(mockAgentService.getClientsByEmployersReference(any)(using any[HeaderCarrier]))
        .thenReturn(Future.successful(List(okResponse)))

      val userAnswers = UserAnswers(userAnswersId)
        .set(CisIdPage, "123")
        .success
        .value
        .set(AgentClientsPage, List(okResponse))
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AgentService].toInstance(mockAgentService)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.agent.routes.ClientRemoveController
              .onPageLoad()
              .url
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.clientdetails.routes.RemoveClientYesNoController.onPageLoad(NormalMode).url

        verify(mockAgentService)
          .getClientsByEmployersReference(
            eqTo("111/test111")
          )(any())
      }
    }

  }

}
