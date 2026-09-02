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
import controllers.actions.*
import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear}
import models.verify.{VerificationHistoryData, VerificationRequestData, VerificationTaxYearSelection}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{clearInvocations, verify as mockVerify, verifyNoMoreInteractions, when}
import org.scalatest.BeforeAndAfterEach
import pages.CisIdPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{VerificationHistoryService, VerificationService}
import viewmodels.*
import views.html.verify.{NoVerificationHistoryView, VerificationHistoryView}

import java.time.LocalDate
import scala.concurrent.Future

class VerificationHistoryControllerSpec extends UnitSpec with BeforeAndAfterEach {
  import play.twirl.api.Html

  private def verificationRequestData(
    verificationNumber: String,
    dateSubmitted: LocalDate,
    taxYear: Int
  ): VerificationRequestData =
    VerificationRequestData(
      verificationBatchId = 1L,
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

  private val viewModel = VerificationHistoryPageViewModel(
    taxYears = Seq(
      VerificationTaxYearViewModel(
        fromYear = 2026,
        toYear = 2027,
        rows = Seq(
          VerificationHistoryRowViewModel(
            verificationNumber = "V0004528765",
            dateSubmitted = "06 Apr 2026",
            verificationRequestLink = "#",
            submissionReceiptLink = Some("#")
          )
        )
      )
    ),
    selectedTaxYear = Some("2026"),
    instanceId = cisId
  )

  private val verificationHistoryData = VerificationHistoryData(
    verificationRequests = Seq(
      verificationRequestData("V0004528765", LocalDate.of(2026, 4, 6), 2026)
    )
  )

  private val mockVerificationHistoryService = mock[VerificationHistoryService]
  private val mockVerificationService        = mock[VerificationService]

  private val userAnswersWithCisId = emptyUserAnswers.set(CisIdPage, cisId).success.value

  def mockVerificationServiceReturnsData(): Unit =
    when(mockVerificationService.getSubmittedVerifications(any)(any)) thenReturn
      Future.successful(verificationHistoryData)

  def mockVerificationServiceFails(): Unit =
    when(mockVerificationService.getSubmittedVerifications(any)(any)) thenReturn
      Future.failed(new RuntimeException("boom"))

  def mockSingleYearViewModelReturns(model: Option[VerificationHistoryPageViewModel]): Unit =
    when(mockVerificationHistoryService.buildSingleYearViewModel(any, any, any)) thenReturn model

  def mockAllYearsViewModelReturns(model: Option[VerificationHistoryPageViewModel]): Unit =
    when(mockVerificationHistoryService.buildAllYearsViewModel(any, any)) thenReturn model

  private val mockHistoryView        = mock[VerificationHistoryView]
  private val expectedHistoryContent = "Here is your Verification History"
  when(mockHistoryView.apply(any)(any, any)) thenReturn Html(expectedHistoryContent)

  private val mockNoHistoryView        = mock[NoVerificationHistoryView]
  private val expectedNoHistoryContent = "No Verification History found"
  when(mockNoHistoryView.apply(any)(any, any)) thenReturn Html(expectedNoHistoryContent)

  private val controllerUnderTest = new VerificationHistoryController(
    new FakeIdentifierAction(isAgent = false)(parsers),
    new DataRetrievalActionImpl(mockSessionRepository),
    new DataRequiredActionImpl(),
    new CisIdRequiredActionImpl(),
    stubMessagesControllerComponents(),
    mockHistoryView,
    mockNoHistoryView,
    mockNotFoundView,
    mockVerificationHistoryService,
    mockVerificationService
  )

  "onPageLoad must" - {
    "return 200 OK and" - {
      "show history page when CIS ID is present, a single tax year is selected, and view model is non-empty" in {
        givenSessionWithData(Some(userAnswersWithCisId))
        mockVerificationServiceReturnsData()
        mockSingleYearViewModelReturns(Some(viewModel))

        val givenTaxYear = TaxYear(2026)
        val result       = controllerUnderTest.onPageLoad(givenTaxYear.toPath)(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedHistoryContent

        mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
        mockVerify(mockVerificationHistoryService)
          .buildSingleYearViewModel(verificationHistoryData, givenTaxYear.startYear, cisId)
      }

      "show history page when CIS ID is present, all tax years are selected, and view model is non-empty" in {
        givenSessionWithData(Some(userAnswersWithCisId))
        mockVerificationServiceReturnsData()
        mockAllYearsViewModelReturns(Some(viewModel))

        val result = controllerUnderTest.onPageLoad(AllTaxYears.toPath)(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedHistoryContent

        mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
        mockVerify(mockVerificationHistoryService).buildAllYearsViewModel(verificationHistoryData, cisId)
      }

      "and show no history page when CIS ID is present, all tax years are selected, but view model is empty" in {
        givenSessionWithData(Some(userAnswersWithCisId))
        mockVerificationServiceReturnsData()
        mockAllYearsViewModelReturns(None)

        val result = controllerUnderTest.onPageLoad(AllTaxYears.toPath)(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedNoHistoryContent

        mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
        mockVerify(mockVerificationHistoryService).buildAllYearsViewModel(verificationHistoryData, cisId)
      }
    }

    "return 303 SEE_OTHER when" - {
      "CisIdPage is missing and redirect to unauthorised page" in {
        givenSessionWithData(Some(emptyUserAnswers))

        val result = controllerUnderTest.onPageLoad(TaxYear(2026).toPath)(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual unauthorisedUrl
      }

      "resolveVerificationHistoryData fails and redirect to journey recovery" in {
        givenSessionWithData(Some(userAnswersWithCisId))
        mockVerificationServiceFails()

        val result = controllerUnderTest.onPageLoad(TaxYear(2026).toPath)(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl

        mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
      }
    }

    "return 404 NOT_FOUND when" - {
      "tax year selection is invalid" in {
        givenSessionWithData(Some(userAnswersWithCisId))

        val result = controllerUnderTest.onPageLoad("invalid-tax-year-selection")(FakeRequest())

        status(result) mustEqual NOT_FOUND
        contentAsString(result) mustEqual expectedNotFoundContent
      }

      "single tax year selection is valid but no history exists for that year" in {
        givenSessionWithData(Some(userAnswersWithCisId))
        mockVerificationServiceReturnsData()
        mockSingleYearViewModelReturns(None)

        val givenTaxYear = TaxYear(2026)
        val result       = controllerUnderTest.onPageLoad(givenTaxYear.toPath)(FakeRequest())

        status(result) mustEqual NOT_FOUND
        contentAsString(result) mustEqual expectedNotFoundContent

        mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
        mockVerify(mockVerificationHistoryService)
          .buildSingleYearViewModel(verificationHistoryData, givenTaxYear.startYear, cisId)
      }
    }
  }

  override def afterEach(): Unit =
    verifyNoMoreInteractions(mockVerificationService, mockVerificationHistoryService)
    clearInvocations(mockVerificationService, mockVerificationHistoryService)
}
