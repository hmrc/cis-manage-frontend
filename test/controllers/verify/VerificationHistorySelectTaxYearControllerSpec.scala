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
import forms.verify.TaxYearFormProvider
import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear, TaxYearPeriod}
import models.verify.{VerificationHistoryData, VerificationRequestData}
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{VerificationHistoryService, VerificationService}
import views.html.verify.VerificationHistorySelectTaxYearView

import java.time.LocalDate
import scala.concurrent.Future

class VerificationHistorySelectTaxYearControllerSpec extends UnitSpec with BeforeAndAfterEach {
  import org.mockito.ArgumentMatchers.{any, eq as eqTo}
  import org.mockito.Mockito.{clearInvocations, verify, verifyNoMoreInteractions, when}
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
      taxYear = taxYear,
      status = "SUBMITTED",
      acceptedDateTime = dateSubmitted.atStartOfDay(),
      contractorName = "Test Scheme",
      employerReference = "123PA000001",
      receiptReferenceNumber = "Pyy1LRJh053AE+nuyp0GJR7oESw=",
      subcontractorsToVerify = Seq.empty
    )

  private val formProvider = new TaxYearFormProvider()

  private val verificationHistoryData =
    VerificationHistoryData(
      verificationRequests = Seq(
        verificationRequestData("V001", LocalDate.of(2026, 4, 6), 2026),
        verificationRequestData("V002", LocalDate.of(2025, 4, 6), 2025),
        verificationRequestData("V003", LocalDate.of(2025, 4, 5), 2024)
      )
    )

  private val mockVerificationService        = mock[VerificationService]
  private val mockVerificationHistoryService = mock[VerificationHistoryService]
  private val mockView                       = mock[VerificationHistorySelectTaxYearView]
  private val givenViewContent               = "Hello, world!"
  when(mockView(any, any)(any, any)) thenReturn Html(givenViewContent)

  when(mockVerificationService.getSubmittedVerifications(any)(any)) thenReturn
    Future.successful(verificationHistoryData)
  when(mockVerificationHistoryService.toVerificationHistoryData(any)) thenReturn verificationHistoryData

  private val controllerUnderTest = new VerificationHistorySelectTaxYearController(
    new FakeIdentifierAction(isAgent = true)(parsers),
    new DataRetrievalActionImpl(mockSessionRepository),
    new DataRequiredActionImpl(),
    new CisIdRequiredActionImpl(),
    formProvider,
    mockVerificationService,
    mockVerificationHistoryService,
    stubMessagesControllerComponents(),
    mockView
  )

  "VerificationHistorySelectTaxYear Controller" - {

    "must redirect to verification history page when history has 0 tax years" in {
      givenSessionWithData(Some(userAnswersWithCisId))
      mockVerificationTaxYears(Seq.empty)

      val result = controllerUnderTest.onPageLoad()(FakeRequest())

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual routes.VerificationHistoryController.onPageLoad(AllTaxYears.toPath).url
      contentAsString(result) mustBe empty

      verify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
      verifyNoMoreInteractions(mockVerificationService)
    }

    "must redirect to verification history page when history has 1 tax year" in {
      givenSessionWithData(Some(userAnswersWithCisId))
      mockVerificationTaxYears(Seq(TaxYearPeriod(1999)))

      val result = controllerUnderTest.onPageLoad()(FakeRequest())

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.VerificationHistoryController.onPageLoad(AllTaxYears.toPath).url
      contentAsString(result) mustBe empty

      verify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
    }

    "must show select tax year page when history has 2 tax years" in {
      val givenTaxYears = Seq(TaxYearPeriod(1999), TaxYearPeriod(2000))

      givenSessionWithData(Some(userAnswersWithCisId))
      mockVerificationTaxYears(givenTaxYears)

      val result = controllerUnderTest.onPageLoad()(FakeRequest())

      status(result) mustEqual OK
      redirectLocation(result) mustBe empty
      contentAsString(result) mustBe givenViewContent

      verify(mockVerificationService).getSubmittedVerifications(eqTo(cisId))(any)
    }

    "must redirect to Journey Recovery when no user answers exist and form submitted" in {
      givenSessionWithData(None)

      val request = FakeRequest().withFormUrlEncodedBody("value" -> "all")
      val result  = controllerUnderTest.onSubmit()(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }

    "must treat 'all' as AllTaxYears and redirect to all tax years verification history" in {
      givenSessionWithData(Some(userAnswersWithCisId))

      val request = FakeRequest().withFormUrlEncodedBody("value" -> "all")
      val result  = controllerUnderTest.onSubmit()(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.VerificationHistoryController.onPageLoad(AllTaxYears.toPath).url
    }

    "must redirect to single year verification history when a tax year is submitted" in {
      givenSessionWithData(Some(userAnswersWithCisId))
      mockVerificationTaxYears(Seq(TaxYearPeriod(2026)))

      val request = FakeRequest().withFormUrlEncodedBody("value" -> "2026")
      val result  = controllerUnderTest.onSubmit()(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.VerificationHistoryController.onPageLoad(TaxYear(2026).toPath).url
    }

    "must return BAD_REQUEST when invalid data is submitted" in {
      givenSessionWithData(Some(userAnswersWithCisId))
      mockVerificationTaxYears(2024 to 2026 map TaxYearPeriod.apply)

      val request = FakeRequest().withFormUrlEncodedBody("value" -> "invalid")
      val result  = controllerUnderTest.onSubmit()(request)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual givenViewContent
    }

    "must redirect to Journey Recovery for a GET when no existing data is found" in {
      givenSessionWithData(None)

      val result = controllerUnderTest.onPageLoad()(FakeRequest())

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {
      givenSessionWithData(None)

      val request = FakeRequest().withFormUrlEncodedBody("value" -> "all")
      val result  = controllerUnderTest.onPageLoad()(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }
  }

  override def afterEach(): Unit =
    clearInvocations(mockVerificationService, mockVerificationHistoryService)
    verifyNoMoreInteractions(mockVerificationService, mockVerificationHistoryService)

  private def mockVerificationTaxYears(taxYears: Seq[TaxYearPeriod]) =
    when(mockVerificationHistoryService.getSubmittedVerificationTaxYears(any)) thenReturn taxYears
}
