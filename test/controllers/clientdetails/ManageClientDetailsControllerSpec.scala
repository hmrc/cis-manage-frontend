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
import models.CisTaxpayerSearchResult
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.AgentService
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import views.html.clientdetails.ManageClientDetailsView

import scala.concurrent.Future

class ManageClientDetailsControllerSpec extends SpecBase with MockitoSugar {

  val employerRef = "123456"

  val okResponse =
    CisTaxpayerSearchResult(
      uniqueId = "123",
      taxOfficeNumber = "111",
      taxOfficeRef = "111/test111",
      agentOwnRef = Option("TEST LTD"),
      schemeName = Option("ABCD"),
      utr = Option("ABCD")
    )

  "ManageClientDetails Controller" - {

    "must return OK and the correct view for a GET" in {
      val mockService = mock[AgentService]
      when(
        mockService.getClientsByEmployersReference(
          eqTo(employerRef)
        )(any())
      ).thenReturn(
        Future.successful(List(okResponse))
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AgentService].toInstance(mockService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.clientdetails.routes.ManageClientDetailsController.onPageLoad(employerRef).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ManageClientDetailsView]

        val fakeUniqueId    = "123"
        val fakeClientName  = Some("ABCD")
        val fakeEmployerRef = "123456"
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
