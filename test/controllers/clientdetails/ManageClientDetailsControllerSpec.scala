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

package controllers.clientdetails

import base.SpecBase
import models.{CisTaxpayerSearchResult, UserAnswers}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.AgentService
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{AgentClientsPage, CisIdPage}
import play.api.inject.bind
import uk.gov.hmrc.http.HeaderCarrier
import views.html.clientdetails.ManageClientDetailsView

import scala.concurrent.Future

class ManageClientDetailsControllerSpec extends SpecBase with MockitoSugar {

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
  val request    =
    FakeRequest(GET, controllers.clientdetails.routes.ManageClientDetailsController.onPageLoad().url)
  "ManageClientDetails Controller" - {

    "must return OK and the correct view for a GET" in {
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
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AgentService].toInstance(mockAgentService)
        )
        .build()

      running(application) {

        val result = route(application, request).value

        val view = application.injector.instanceOf[ManageClientDetailsView]

        val fakeUniqueId    = "123"
        val fakeClientName  = Some("ABCD")
        val fakeEmployerRef = "111/test111"
        val fakeClientRef   = Some("TEST LTD")

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          fakeUniqueId,
          fakeClientName.toString,
          fakeEmployerRef,
          fakeClientRef.toString
        )(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
