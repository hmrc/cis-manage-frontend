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

package controllers.verify

import base.SpecBase
import models.UserAnswers
import models.verify.{SubcontractorVerificationData, VerificationRequestDetailData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify as mockVerify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.CisIdPage
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{ManageService, VerificationRequestService}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.*
import views.html.verify.VerificationRequestView

import java.time.LocalDateTime
import scala.concurrent.Future

class VerificationRequestControllerSpec extends SpecBase with MockitoSugar {

  private val cisId              = "900063"
  private val verificationNumber = "V0004528765"

  private val detailData = VerificationRequestDetailData(
    verificationNumber = verificationNumber,
    dateTimeSubmitted = LocalDateTime.of(2027, 2, 6, 14, 30),
    subcontractorsToVerify = Seq(
      SubcontractorVerificationData("Amity Marine Contractors", "V0004528765"),
      SubcontractorVerificationData("Brody, Martin", "V0004528765")
    ),
    subcontractorsToReverify = Seq(
      SubcontractorVerificationData("Orca Industrial", "V0004528765/L")
    )
  )

  private val viewModel = VerificationRequestPageViewModel(
    submittedTime = "14:30",
    submittedDate = "6 February 2027",
    verificationNumber = verificationNumber,
    totalSubcontractors = 3,
    subcontractorsToVerify = Seq(
      SubcontractorRowViewModel("Amity Marine Contractors", "V0004528765"),
      SubcontractorRowViewModel("Brody, Martin", "V0004528765")
    ),
    subcontractorsToReverify = Seq(
      SubcontractorRowViewModel("Orca Industrial", "V0004528765/L")
    ),
    manageSubcontractorsUrl = s"/manage-subcontractors/$cisId"
  )

  trait Setup {

    val mockVerificationRequestService: VerificationRequestService = mock[VerificationRequestService]
    val mockManageService: ManageService                           = mock[ManageService]

    def application(userAnswers: UserAnswers): Application =
      applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[VerificationRequestService].toInstance(mockVerificationRequestService),
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

    def userAnswersWithCisId: UserAnswers =
      emptyUserAnswers
        .set(CisIdPage, cisId)
        .success
        .value

    def journeyRecoveryUrl: String =
      controllers.routes.JourneyRecoveryController.onPageLoad().url

    def unauthorisedUrl: String =
      controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad().url
  }

  "VerificationRequestController" - {

    "onPageLoad must return OK when data is available" in new Setup {
      val userAnswers = userAnswersWithCisId

      when(mockManageService.getVerificationRequestDetail(any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(detailData))
      when(mockVerificationRequestService.buildViewModel(any[VerificationRequestDetailData], any[String]))
        .thenReturn(viewModel)

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationRequestController.onPageLoad(verificationNumber).url)
        val result  = route(app, request).value
        val view    = app.injector.instanceOf[VerificationRequestView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(app)).toString

        mockVerify(mockManageService).getVerificationRequestDetail(any[String], any[String])(any[HeaderCarrier])
        mockVerify(mockVerificationRequestService).buildViewModel(any[VerificationRequestDetailData], any[String])
      }
    }

    "onPageLoad must redirect to JourneyRecovery when service fails" in new Setup {
      val userAnswers = userAnswersWithCisId

      when(mockManageService.getVerificationRequestDetail(any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val app = application(userAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationRequestController.onPageLoad(verificationNumber).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl
      }
    }

    "onPageLoad must redirect when CisIdPage is missing" in new Setup {
      val app = application(emptyUserAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.VerificationRequestController.onPageLoad(verificationNumber).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual unauthorisedUrl
      }
    }
  }
}
