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
import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear}
import models.verify.{VerificationHistoryData, VerificationRequestData, VerificationTaxYearSelection}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify as mockVerify, verifyNoMoreInteractions, when}
import play.api.i18n.Lang
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{VerificationHistoryService, VerificationService}
import viewmodels.*
import views.html.verify.{NoVerificationHistoryView, VerificationHistoryView}

import java.time.LocalDate
import scala.concurrent.Future

class VerificationHistoryControllerSpec extends UnitSpec {
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

  trait Setup {
    val mockVerificationHistoryService: VerificationHistoryService = mock[VerificationHistoryService]
    val mockVerificationService: VerificationService               = mock[VerificationService]

    def mockVerificationServiceReturnsData(): Unit =
      when(mockVerificationService.getSubmittedVerifications(any)(any)) thenReturn
        Future.successful(verificationHistoryData)

    def mockVerificationServiceFails(): Unit =
      when(mockVerificationService.getSubmittedVerifications(any)(any)) thenReturn
        Future.failed(new RuntimeException("boom"))

    def mockSingleYearViewModelReturns(model: Option[VerificationHistoryPageViewModel]): Unit =
      when(mockVerificationHistoryService.buildSingleYearViewModel(any, any, any)(any[Lang]())) thenReturn model

    def mockAllYearsViewModelReturns(model: Option[VerificationHistoryPageViewModel]): Unit =
      when(mockVerificationHistoryService.buildAllYearsViewModel(any, any)(any)) thenReturn model

    val stubHistoryView: VerificationHistoryView = mock[VerificationHistoryView]
    val stubHistoryContent                       = "Here is your Verification History"
    when(stubHistoryView.apply(any)(any, any)) thenReturn Html(stubHistoryContent)

    val stubNoHistoryView: NoVerificationHistoryView = mock[NoVerificationHistoryView]
    val stubNoHistoryContent                         = "No Verification History found"
    when(stubNoHistoryView.apply(any)(any, any)) thenReturn Html(stubNoHistoryContent)

    val controllerUnderTest = new VerificationHistoryController(
      mockControllerComponents,
      stubHistoryView,
      stubNoHistoryView,
      stubNotFoundView,
      mockVerificationHistoryService,
      mockVerificationService
    )
  }

  "onPageLoad must" - {
    "return 200 OK and" - {
      "show history page when CIS ID is present, a single tax year is selected, and view model is non-empty" in new Setup {
        mockControllerComponents.setUserAnswers(Some(userAnswersWithCisId))
        mockVerificationServiceReturnsData()
        mockSingleYearViewModelReturns(Some(viewModel))

        val givenTaxYear   = TaxYear(2026)
        private val result = controllerUnderTest.onPageLoad(givenTaxYear.toPath)(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual stubHistoryContent

        mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
        mockVerify(mockVerificationHistoryService)
          .buildSingleYearViewModel(eqTo(verificationHistoryData), eqTo(givenTaxYear.startYear), eqTo(cisId))(any)
        verifyNoMoreInteractions(mockVerificationService, mockVerificationHistoryService)
      }

      "show history page when CIS ID is present, all tax years are selected, and view model is non-empty" in new Setup {
        mockControllerComponents.setUserAnswers(Some(userAnswersWithCisId))

        mockVerificationServiceReturnsData()
        mockAllYearsViewModelReturns(Some(viewModel))

        private val result = controllerUnderTest.onPageLoad(AllTaxYears.toPath)(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual stubHistoryContent

        mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
        mockVerify(mockVerificationHistoryService).buildAllYearsViewModel(eqTo(verificationHistoryData), eqTo(cisId))(
          any
        )
        verifyNoMoreInteractions(mockVerificationService, mockVerificationHistoryService)
      }

      "and show no history page when CIS ID is present, all tax years are selected, but view model is empty" in new Setup {
        mockControllerComponents.setUserAnswers(Some(userAnswersWithCisId))
        mockVerificationServiceReturnsData()
        mockAllYearsViewModelReturns(None)

        private val result = controllerUnderTest.onPageLoad(AllTaxYears.toPath)(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual stubNoHistoryContent

        mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
        mockVerify(mockVerificationHistoryService).buildAllYearsViewModel(eqTo(verificationHistoryData), eqTo(cisId))(
          any
        )
        verifyNoMoreInteractions(mockVerificationService, mockVerificationHistoryService)
      }
    }

    "return 303 SEE_OTHER when" - {
      "CisIdPage is missing and redirect to unauthorised page" in new Setup {
        mockControllerComponents.setUserAnswers(Some(emptyUserAnswers))

        private val result = controllerUnderTest.onPageLoad(TaxYear(2026).toPath)(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual unauthorisedUrl
        verifyNoMoreInteractions(mockVerificationService, mockVerificationHistoryService)
      }

      "resolveVerificationHistoryData fails and redirect to journey recovery" in new Setup {
        mockControllerComponents.setUserAnswers(Some(userAnswersWithCisId))
        mockVerificationServiceFails()

        private val result = controllerUnderTest.onPageLoad(TaxYear(2026).toPath)(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryUrl

        mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
        verifyNoMoreInteractions(mockVerificationService, mockVerificationHistoryService)
      }
    }

    "return 404 NOT_FOUND when" - {
      "tax year selection is invalid" in new Setup {
        mockControllerComponents.setUserAnswers(Some(userAnswersWithCisId))

        private val result = controllerUnderTest.onPageLoad("invalid-tax-year-selection")(FakeRequest())

        status(result) mustEqual NOT_FOUND
        contentAsString(result) mustEqual stubNotFoundContent
        verifyNoMoreInteractions(mockVerificationService, mockVerificationHistoryService)
      }

      "single tax year selection is valid but no history exists for that year" in new Setup {
        mockControllerComponents.setUserAnswers(Some(userAnswersWithCisId))
        mockVerificationServiceReturnsData()
        mockSingleYearViewModelReturns(None)

        val givenTaxYear   = TaxYear(2026)
        private val result = controllerUnderTest.onPageLoad(givenTaxYear.toPath)(FakeRequest())

        status(result) mustEqual NOT_FOUND
        contentAsString(result) mustEqual stubNotFoundContent

        mockVerify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
        mockVerify(mockVerificationHistoryService)
          .buildSingleYearViewModel(eqTo(verificationHistoryData), eqTo(givenTaxYear.startYear), eqTo(cisId))(any)
        verifyNoMoreInteractions(mockVerificationService, mockVerificationHistoryService)
      }
    }
  }
}
