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
import controllers.actions.{CisIdRequiredActionImpl, DataRequiredActionImpl, DataRetrievalActionImpl, FakeIdentifierAction}
import models.verify.{VerificationHistoryData, VerificationRequestData}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify as mockVerify, verifyNoInteractions, when}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{VerificationHistoryService, VerificationService}
import viewmodels.*
import views.html.verify.VerificationRequestView

import java.time.LocalDate
import scala.concurrent.Future

class VerificationRequestControllerSpec extends UnitSpec {
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
      taxYear = taxYear,
      status = "SUBMITTED",
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

  private val viewModel = VerificationRequestPageViewModel(
    submittedTime = "14:30",
    submittedDate = "06 February 2027",
    verificationNumber = verificationNumber,
    contractorName = "Gary Construction Ltd",
    employerReference = "123",
    receiptReferenceNumber = "Pyy1LRJh053AE+nuyp0GJR7oESw=",
    subcontractorsToVerify = Seq(
      SubcontractorRowViewModel("Amity Marine Contractors", "V0004528765"),
      SubcontractorRowViewModel("Brody, Martin", "V0004528765"),
      SubcontractorRowViewModel("Orca Industrial", "V0004528765/L")
    ),
    manageSubcontractorsUrl = s"/manage-subcontractors/$cisId"
  )

  trait Setup {
    protected val mockVerificationService: VerificationService               = mock[VerificationService]
    protected val mockVerificationHistoryService: VerificationHistoryService = mock[VerificationHistoryService]
    protected val mockView: VerificationRequestView                          = mock[VerificationRequestView]

    val controllerUnderTest = new VerificationRequestController(
      FakeIdentifierAction(isAgent = false)(parsers),
      new DataRetrievalActionImpl(mockSessionRepository),
      new DataRequiredActionImpl,
      new CisIdRequiredActionImpl(),
      stubMessagesControllerComponents(),
      mockView,
      mockVerificationHistoryService,
      mockVerificationService
    )
  }

  "onPageLoad must" - {

    "return OK when Verification Service returns history data" in new Setup {
      givenSessionWithData(Some(userAnswersWithCisId))
      when(mockVerificationService.getSubmittedVerifications(any)(any)) thenReturn
        Future.successful(verificationHistoryData)
      when(mockVerificationHistoryService.buildVerificationRequestViewModel(any, any, any)) thenReturn Some(viewModel)
      val givenViewContent = "Hello, world!"
      when(mockView.apply(any)(any, any)) thenReturn Html(givenViewContent)

      private val result = controllerUnderTest.onPageLoad(verificationBatchId)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual givenViewContent

      mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
      mockVerify(mockVerificationHistoryService)
        .buildVerificationRequestViewModel(verificationHistoryData, verificationBatchId, cisId)
    }

    "redirect to JourneyRecovery when the verification number is not found" in new Setup {
      givenSessionWithData(Some(userAnswersWithCisId))
      when(mockVerificationService.getSubmittedVerifications(any)(any)) thenReturn Future.successful(
        verificationHistoryData
      )
      when(mockVerificationHistoryService.buildVerificationRequestViewModel(any, any, any)) thenReturn None

      private val result = controllerUnderTest.onPageLoad(verificationBatchId)(FakeRequest())

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual journeyRecoveryUrl

      mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
      mockVerify(mockVerificationHistoryService)
        .buildVerificationRequestViewModel(verificationHistoryData, verificationBatchId, cisId)
    }

    "redirect when CisIdPage is missing" in new Setup {
      givenSessionWithData(Some(emptyUserAnswers))

      private val result = controllerUnderTest.onPageLoad(verificationBatchId)(FakeRequest())

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual unauthorisedUrl

      verifyNoInteractions(mockVerificationService, mockVerificationHistoryService)
    }
  }
}
