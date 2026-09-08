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
import controllers.actions.{ClientListStatusGuard, HasClientGuard}
import models.requests.{DataRequest, IdentifierRequest}
import models.{CisTaxpayer, CisTaxpayerSearchResult, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{AgentClientsPage, CisIdPage}
import play.api.inject.bind
import play.api.mvc.{ActionFilter, Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.ManageService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.clientdetails.ManageClientDetailsView

import scala.concurrent.{ExecutionContext, Future}

class ManageClientDetailsControllerSpec extends SpecBase with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global

  private val clientListStatusGuard = mock[ClientListStatusGuard]
  private val hasClientGuard        = mock[HasClientGuard]

  private val passThroughIdentifierFilter =
    new ActionFilter[IdentifierRequest] {
      override protected def executionContext: ExecutionContext = ec

      override protected def filter[A](
        request: IdentifierRequest[A]
      ): Future[Option[Result]] =
        Future.successful(None)
    }

  private val passThroughDataFilter =
    new ActionFilter[DataRequest] {
      override protected def executionContext: ExecutionContext = ec

      override protected def filter[A](
        request: DataRequest[A]
      ): Future[Option[Result]] =
        Future.successful(None)
    }

  val employerRef = "123456"

  val okResponse = CisTaxpayer(
    uniqueId = "CIS-123",
    taxOfficeNumber = "111",
    taxOfficeRef = "test111",
    aoDistrict = None,
    aoPayType = None,
    aoCheckCode = None,
    aoReference = None,
    validBusinessAddr = None,
    correlation = None,
    ggAgentId = None,
    employerName1 = Some("TEST LTD"),
    employerName2 = None,
    agentOwnRef = Some("TEST LTD"),
    schemeName = Option("ABCD"),
    utr = Some("1234567890"),
    enrolledSig = None
  )

  val clients =
    CisTaxpayerSearchResult(
      uniqueId = "CIS-123",
      taxOfficeNumber = "111",
      taxOfficeRef = "test111",
      agentOwnRef = Some("TEST LTD"),
      schemeName = Some("ABCD"),
      utr = Some("1234567890")
    )

  val request =
    FakeRequest(
      GET,
      controllers.clientdetails.routes.ManageClientDetailsController.onPageLoad().url
    )

  def onwardRoute = Call("GET", "/foo")

  "ManageClientDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      when(clientListStatusGuard.groupB(any()))
        .thenReturn(passThroughIdentifierFilter)

      when(hasClientGuard.currentClient)
        .thenReturn(passThroughDataFilter)

      val mockSessionRepository = mock[SessionRepository]
      val mockManageService     = mock[ManageService]

      when(
        mockManageService.getClientByEmployerReference(any, any)(using any[HeaderCarrier])
      ).thenReturn(Future.successful(okResponse))

      val userAnswers = UserAnswers(userAnswersId)
        .set(CisIdPage, "CIS-123")
        .success
        .value
        .set(AgentClientsPage, List(clients))
        .success
        .value

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ClientListStatusGuard].toInstance(clientListStatusGuard),
            bind[HasClientGuard].toInstance(hasClientGuard),
            bind[ManageService].toInstance(mockManageService),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      running(application) {

        val result = route(application, request).value

        val view = application.injector.instanceOf[ManageClientDetailsView]

        val fakeUniqueId    = "CIS-123"
        val fakeClientName  = "ABCD"
        val fakeEmployerRef = "111/test111"
        val fakeClientRef   = "TEST LTD"

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          fakeUniqueId,
          fakeClientName,
          fakeEmployerRef,
          fakeClientRef
        )(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
