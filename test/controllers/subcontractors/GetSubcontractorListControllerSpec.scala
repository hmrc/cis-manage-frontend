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

package controllers.subcontractors

import base.SpecBase
import models.UserAnswers
import models.response.{GetSubcontractor, GetSubcontractorListResponse}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.subcontractors.SubcontractorListPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class GetSubcontractorListControllerSpec extends SpecBase with MockitoSugar {

  private val instanceId = "test-instance-id"

  private val subcontractor =
    GetSubcontractor(
      subcontractorId = 1L,
      utr = Some("1234567890"),
      pageVisited = None,
      partnerUtr = None,
      crn = None,
      firstName = Some("Alan"),
      nino = None,
      secondName = None,
      surname = Some("Smith"),
      partnershipTradingName = None,
      tradingName = None,
      subcontractorType = Some("soleTrader"),
      addressLine1 = None,
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      country = None,
      postcode = None,
      emailAddress = None,
      phoneNumber = None,
      mobilePhoneNumber = None,
      worksReferenceNumber = None,
      createDate = None,
      lastUpdate = None,
      subbieResourceRef = Some(10L),
      matched = None,
      autoVerified = None,
      verified = Some("Y"),
      verificationNumber = Some("V000001"),
      taxTreatment = Some("Gross"),
      verificationDate = None,
      version = None,
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = None
    )

  private def request =
    FakeRequest(
      GET,
      routes.GetSubcontractorListController.onPageLoad().url
    )

  private def answersWithInstanceId: UserAnswers =
    emptyUserAnswers.copy(
      data = Json.obj(
        "cisId" -> instanceId
      )
    )

  "GetSubcontractorListController" - {

    "must retrieve subcontractors, save them and redirect to the subcontractors list" in {
      val subcontractorService = mock[SubcontractorService]
      val sessionRepository    = mock[SessionRepository]

      val response =
        GetSubcontractorListResponse(
          subcontractors = Seq(subcontractor)
        )

      when(
        subcontractorService
          .getSubcontractorList(eqTo(instanceId))(any[HeaderCarrier])
      ).thenReturn(Future.successful(response))

      when(sessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(
          userAnswers = Some(answersWithInstanceId)
        ).overrides(
          bind[SubcontractorService].toInstance(subcontractorService),
          bind[SessionRepository].toInstance(sessionRepository)
        ).build()

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SubcontractorsListController
            .onPageLoad(instanceId, models.NormalMode)
            .url

        verify(subcontractorService)
          .getSubcontractorList(eqTo(instanceId))(any[HeaderCarrier])

        val userAnswersCaptor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        verify(sessionRepository).set(userAnswersCaptor.capture())

        userAnswersCaptor.getValue
          .get(SubcontractorListPage) mustEqual Some(response)
      }
    }

    "must retrieve an empty list, save it and redirect to no subcontractors exist" in {
      val subcontractorService = mock[SubcontractorService]
      val sessionRepository    = mock[SessionRepository]

      val response =
        GetSubcontractorListResponse(
          subcontractors = Seq.empty
        )

      when(
        subcontractorService
          .getSubcontractorList(eqTo(instanceId))(any[HeaderCarrier])
      ).thenReturn(Future.successful(response))

      when(sessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(
          userAnswers = Some(answersWithInstanceId)
        ).overrides(
          bind[SubcontractorService].toInstance(subcontractorService),
          bind[SessionRepository].toInstance(sessionRepository)
        ).build()

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.NoSubcontractorsExistController.onPageLoad().url

        val userAnswersCaptor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        verify(sessionRepository).set(userAnswersCaptor.capture())

        userAnswersCaptor.getValue
          .get(SubcontractorListPage) mustEqual Some(response)
      }
    }

    "must redirect to Journey Recovery when retrieving subcontractors fails" in {
      val subcontractorService = mock[SubcontractorService]
      val sessionRepository    = mock[SessionRepository]

      when(
        subcontractorService
          .getSubcontractorList(eqTo(instanceId))(any[HeaderCarrier])
      ).thenReturn(
        Future.failed(
          new RuntimeException("Unable to retrieve subcontractors")
        )
      )

      val application =
        applicationBuilder(
          userAnswers = Some(answersWithInstanceId)
        ).overrides(
          bind[SubcontractorService].toInstance(subcontractorService),
          bind[SessionRepository].toInstance(sessionRepository)
        ).build()

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url

        verifyNoInteractions(sessionRepository)
      }
    }

    "must redirect to Journey Recovery when saving subcontractors fails" in {
      val subcontractorService = mock[SubcontractorService]
      val sessionRepository    = mock[SessionRepository]

      val response =
        GetSubcontractorListResponse(
          subcontractors = Seq(subcontractor)
        )

      when(
        subcontractorService
          .getSubcontractorList(eqTo(instanceId))(any[HeaderCarrier])
      ).thenReturn(Future.successful(response))

      when(sessionRepository.set(any[UserAnswers]))
        .thenReturn(
          Future.failed(
            new RuntimeException("Unable to save subcontractors")
          )
        )

      val application =
        applicationBuilder(
          userAnswers = Some(answersWithInstanceId)
        ).overrides(
          bind[SubcontractorService].toInstance(subcontractorService),
          bind[SessionRepository].toInstance(sessionRepository)
        ).build()

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(sessionRepository).set(any[UserAnswers])
      }
    }
  }
}
