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

package controllers.agent

import base.SpecBase
import models.{CisTaxpayer, CisTaxpayerSearchResult}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{mock, when}
import org.scalatest.matchers.should.Matchers.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ManageService
import views.html.agent.AgentLandingView
import config.FrontendAppConfig
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{LocalDate, YearMonth}
import scala.concurrent.Future

class AgentLandingControllerSpec extends SpecBase {

  "AgentLanding Controller" - {

    "must return OK and the correct view for a GET with valid client" in {

      val mockService = mock(classOf[ManageService])

      val clientUniqueId = "client-uid-123"
      val client         = CisTaxpayerSearchResult(
        uniqueId = clientUniqueId,
        taxOfficeNumber = "123",
        taxOfficeRef = "AB45678",
        agentOwnRef = Some("AOR-001"),
        schemeName = Some("ABC Construction Ltd")
      )

      val cisTaxpayer = CisTaxpayer(
        uniqueId = clientUniqueId,
        taxOfficeNumber = "123",
        taxOfficeRef = "AB45678",
        aoDistrict = None,
        aoPayType = None,
        aoCheckCode = None,
        aoReference = None,
        validBusinessAddr = None,
        correlation = None,
        ggAgentId = None,
        employerName1 = None,
        employerName2 = None,
        agentOwnRef = Some("AOR-001"),
        schemeName = Some("ABC Construction Ltd"),
        utr = Some("1234567890"),
        enrolledSig = None
      )

      when(mockService.resolveAndStoreAgentClients(any())(using any))
        .thenReturn(Future.successful((List(client), emptyUserAnswers)))
      when(mockService.getClientDetails(eqTo("123"), eqTo("AB45678"))(using any))
        .thenReturn(Future.successful(cisTaxpayer))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ManageService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentLandingController.onPageLoad(clientUniqueId).url)

        val result = route(application, request).value

        status(result)        shouldBe OK
        contentType(result)     should contain(HTML)
        contentAsString(result) should include("ABC Construction Ltd")
        contentAsString(result) should include("123/AB45678")
        contentAsString(result) should include("1234567890")
      }
    }

    "must redirect to failed retrieval page when client not found" in {

      val mockService = mock(classOf[ManageService])

      val clientUniqueId = "non-existent-uid"

      when(mockService.resolveAndStoreAgentClients(any())(using any))
        .thenReturn(Future.successful((List.empty, emptyUserAnswers)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ManageService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentLandingController.onPageLoad(clientUniqueId).url)

        val result = route(application, request).value

        status(result)           shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(controllers.routes.SystemErrorController.onPageLoad().url)
      }
    }
  }
}
