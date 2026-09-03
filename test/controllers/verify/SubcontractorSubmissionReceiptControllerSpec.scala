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

import base.UnitSpec
import controllers.actions.{CisIdRequiredActionImpl, DataRequiredActionImpl, DataRetrievalActionImpl}
import models.verify.{VerificationHistoryData, VerificationRequestData}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify as mockVerify, verifyNoMoreInteractions, when}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{VerificationHistoryService, VerificationService}
import viewmodels.SubcontractorSubmissionReceiptViewModel
import views.html.verify.SubcontractorSubmissionReceiptView

import java.time.LocalDate
import scala.concurrent.Future

class SubcontractorSubmissionReceiptControllerSpec extends UnitSpec {
  import controllers.actions.FakeIdentifierAction
  import play.twirl.api.Html

  private val verificationNumber  = "V0004528765"
  private val verificationBatchId = 1L

  private def verificationRequestData(
    verificationNumber: String,
    dateSubmitted: LocalDate,
    taxYear: Int
  ): VerificationRequestData =
    VerificationRequestData(
      verificationBatchId = verificationBatchId,
      verificationNumber = verificationNumber,
      dateSubmitted = dateSubmitted,
      status = "SUBMITTED",
      taxYear = taxYear,
      acceptedDateTime = dateSubmitted.atStartOfDay(),
      contractorName = "Test Scheme",
      employerReference = "123PA000001",
      receiptReferenceNumber = "Pyy1LRJh053AE+nuyp0GJR7oESw=",
      subcontractorsToVerify = Seq.empty
    )

  private val verificationHistoryData = VerificationHistoryData(
    verificationRequests = Seq(
      verificationRequestData(verificationNumber, LocalDate.of(2027, 2, 6), 2026)
    )
  )

  private val viewModel =
    SubcontractorSubmissionReceiptViewModel(
      submissionTime = "14:30",
      submissionDate = "06 February 2027",
      contractorName = "Gary Construction Ltd",
      employerReference = "123",
      receiptReferenceNumber = "Pyy1LRJh053AE+nuyp0GJR7oESw=",
      verificationNumber = verificationNumber,
      cisId = cisId
    )

  trait Setup {
    val mockVerificationHistoryService: VerificationHistoryService = mock[VerificationHistoryService]
    val mockVerificationService: VerificationService               = mock[VerificationService]

    val mockView: SubcontractorSubmissionReceiptView = mock[SubcontractorSubmissionReceiptView]
    val givenViewContent                             = "Hello, World!"
    when(mockView(any)(any, any)) thenReturn Html(givenViewContent)

    val controllerUnderTest = new SubcontractorSubmissionReceiptController(
      new FakeIdentifierAction(isAgent = true)(parsers),
      new DataRetrievalActionImpl(mockSessionRepository),
      new DataRequiredActionImpl(),
      new CisIdRequiredActionImpl(),
      stubMessagesControllerComponents(),
      mockView,
      mockVerificationHistoryService,
      mockVerificationService
    )
  }

  "SubcontractorSubmissionReceipt Controller" - {

    "must return OK when VerificationService retrieves history data and VerificationHistoryService builds view model" in new Setup {
      givenSessionWithData(Some(userAnswersWithCisId))
      when(mockVerificationService.getSubmittedVerifications(any)(any)) thenReturn
        Future.successful(verificationHistoryData)
      when(mockVerificationHistoryService.buildSubmissionReceiptViewModel(any, any, any)) thenReturn Some(viewModel)

      private val result = controllerUnderTest.onPageLoad(verificationBatchId)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual givenViewContent

      mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
      mockVerify(mockVerificationHistoryService)
        .buildSubmissionReceiptViewModel(verificationHistoryData, verificationBatchId, cisId)
      verifyNoMoreInteractions(mockVerificationService, mockVerificationHistoryService)
    }

    "must redirect to JourneyRecovery when verification number is not found" in new Setup {
      givenSessionWithData(Some(userAnswersWithCisId))
      when(mockVerificationService.getSubmittedVerifications(any)(any)) thenReturn
        Future.successful(verificationHistoryData)
      when(mockVerificationHistoryService.buildSubmissionReceiptViewModel(any, any, any)) thenReturn None

      private val result = controllerUnderTest.onPageLoad(verificationBatchId)(FakeRequest())

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual journeyRecoveryUrl

      mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
      mockVerify(mockVerificationHistoryService)
        .buildSubmissionReceiptViewModel(verificationHistoryData, verificationBatchId, cisId)
      verifyNoMoreInteractions(mockVerificationService, mockVerificationHistoryService)
    }

    "must redirect when CisIdPage is missing" in new Setup {
      givenSessionWithData(Some(emptyUserAnswers))

      private val result = controllerUnderTest.onPageLoad(verificationBatchId)(FakeRequest())

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual unauthorisedUrl

      verifyNoMoreInteractions(mockVerificationService, mockVerificationHistoryService)
    }
  }
}
