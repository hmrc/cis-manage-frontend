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
import models.response.GetSubmittedVerificationsResponse
import models.verify.{VerificationHistoryData, VerificationRequestData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify as mockVerify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.CisIdPage
import pages.verify.VerificationHistoryDataPage
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{VerificationHistoryService, VerificationService}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.SubcontractorSubmissionReceiptViewModel
import views.html.verify.SubcontractorSubmissionReceiptView

import java.time.LocalDate
import scala.concurrent.Future

class SubcontractorSubmissionReceiptControllerSpec extends SpecBase with MockitoSugar {

  private val cisId              = "900063"
  private val verificationNumber = "V0004528765"

  private def verificationRequestData(
    verificationNumber: String,
    dateSubmitted: LocalDate,
    taxYear: Int
  ): VerificationRequestData =
    VerificationRequestData(
      verificationNumber = verificationNumber,
      dateSubmitted = dateSubmitted,
      taxYear = taxYear,
      acceptedDateTime = dateSubmitted.atStartOfDay(),
      contractorName = "Test Scheme",
      employerReference = "123PA000001",
      receiptReferenceNumber = "Pyy1LRJh053AE+nuyp0GJR7oESw=",
      subcontractorsToVerify = Seq.empty,
      subcontractorsToReverify = Seq.empty
    )

  private val verificationHistoryData = VerificationHistoryData(
    verificationRequests = Seq(
      verificationRequestData(verificationNumber, LocalDate.of(2027, 2, 6), 2026)
    )
  )

  private val submittedVerificationsResponse =
    GetSubmittedVerificationsResponse(
      scheme = Seq.empty,
      subcontractors = Seq.empty,
      verificationBatches = Seq.empty,
      verifications = Seq.empty,
      submissions = Seq.empty
    )

  private val viewModel =
    SubcontractorSubmissionReceiptViewModel(
      submissionTime = "14:30",
      submissionDate = "6 February 2027",
      contractorName = "Gary Construction Ltd",
      employerReference = "123PA000001",
      receiptReferenceNumber = "Pyy1LRJh053AE+nuyp0GJR7oESw=",
      verificationNumber = verificationNumber,
      cisId = cisId
    )

  trait Setup {
    val mockVerificationHistoryService: VerificationHistoryService = mock[VerificationHistoryService]
    val mockVerificationService: VerificationService               = mock[VerificationService]

    def application(userAnswers: UserAnswers): Application =
      applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[VerificationHistoryService].toInstance(mockVerificationHistoryService),
          bind[VerificationService].toInstance(mockVerificationService)
        )
        .build()

    def userAnswersWithCisId: UserAnswers =
      emptyUserAnswers
        .set(CisIdPage, cisId)
        .success
        .value

    def userAnswersWithVerificationHistoryData: UserAnswers =
      userAnswersWithCisId
        .set(VerificationHistoryDataPage, verificationHistoryData)
        .success
        .value

    def journeyRecoveryUrl: String =
      controllers.routes.JourneyRecoveryController.onPageLoad().url

    def unauthorisedUrl: String =
      controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad().url
  }

  "SubcontractorSubmissionReceipt Controller" - {

    "must return OK using VerificationHistoryDataPage when data is available" in new Setup {
      when(
        mockVerificationHistoryService.buildSubmissionReceiptViewModel(
          verificationHistoryData,
          verificationNumber,
          cisId
        )
      ).thenReturn(Some(viewModel))

      val app = application(userAnswersWithVerificationHistoryData)

      running(app) {
        val request =
          FakeRequest(
            GET,
            controllers.verify.routes.SubcontractorSubmissionReceiptController.onPageLoad(verificationNumber).url
          )

        val result = route(app, request).value
        val view   = app.injector.instanceOf[SubcontractorSubmissionReceiptView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(app)).toString

        mockVerify(mockVerificationHistoryService)
          .buildSubmissionReceiptViewModel(verificationHistoryData, verificationNumber, cisId)
        verifyNoInteractions(mockVerificationService)
      }
    }

    "must retrieve submitted verifications when VerificationHistoryDataPage is missing" in new Setup {
      when(mockVerificationService.getSubmittedVerifications(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(submittedVerificationsResponse))
      when(mockVerificationHistoryService.toVerificationHistoryData(submittedVerificationsResponse))
        .thenReturn(verificationHistoryData)
      when(
        mockVerificationHistoryService.buildSubmissionReceiptViewModel(
          verificationHistoryData,
          verificationNumber,
          cisId
        )
      ).thenReturn(Some(viewModel))

      val app = application(userAnswersWithCisId)

      running(app) {
        val request =
          FakeRequest(
            GET,
            controllers.verify.routes.SubcontractorSubmissionReceiptController.onPageLoad(verificationNumber).url
          )

        val result = route(app, request).value

        status(result) mustEqual OK
        mockVerify(mockVerificationService).getSubmittedVerifications(any[String])(any[HeaderCarrier])
        mockVerify(mockVerificationHistoryService).toVerificationHistoryData(submittedVerificationsResponse)
      }
    }

    "must redirect to JourneyRecovery when verification number is not found" in new Setup {
      when(
        mockVerificationHistoryService.buildSubmissionReceiptViewModel(
          verificationHistoryData,
          verificationNumber,
          cisId
        )
      ).thenReturn(None)

      val app = application(userAnswersWithVerificationHistoryData)

      running(app) {
        val request =
          FakeRequest(
            GET,
            controllers.verify.routes.SubcontractorSubmissionReceiptController.onPageLoad(verificationNumber).url
          )

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl
      }
    }

    "must redirect when CisIdPage is missing" in new Setup {
      val app = application(emptyUserAnswers)

      running(app) {
        val request =
          FakeRequest(
            GET,
            controllers.verify.routes.SubcontractorSubmissionReceiptController.onPageLoad(verificationNumber).url
          )

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual unauthorisedUrl
      }
    }
  }
}
