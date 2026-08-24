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
import controllers.actions.{FakeIdentifierAction, IdentifierAction}
import forms.verify.TaxYearFormProvider
import models.UserAnswers
import models.response.GetSubmittedVerificationsResponse
import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear, TaxYearPeriod}
import models.verify.{VerificationHistoryData, VerificationRequestData}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.VerificationHistoryDataPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{VerificationHistoryService, VerificationService}
import views.html.verify.VerificationHistorySelectTaxYearView

import java.time.LocalDate
import scala.concurrent.Future

class VerificationHistorySelectTaxYearControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {
  import org.mockito.ArgumentMatchers.{any, eq as eqTo}
  import org.mockito.Mockito.{clearInvocations, verify, verifyNoMoreInteractions, when}
  import play.api.Application
  import play.api.inject.guice.GuiceApplicationBuilder

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

  lazy val verificationHistorySelectTaxYearRoute =
    controllers.verify.routes.VerificationHistorySelectTaxYearController.onPageLoad().url

  private val formProvider = new TaxYearFormProvider()

  private val verificationHistoryData =
    VerificationHistoryData(
      verificationRequests = Seq(
        verificationRequestData("V001", LocalDate.of(2026, 4, 6), 2026),
        verificationRequestData("V002", LocalDate.of(2025, 4, 6), 2025),
        verificationRequestData("V003", LocalDate.of(2025, 4, 5), 2024)
      )
    )

  private val userAnswersWithVerificationHistoryData =
    userAnswersWithCisId
      .set(VerificationHistoryDataPage, verificationHistoryData)
      .success
      .value

  private val stubSubmittedVerificationsResponse = GetSubmittedVerificationsResponse(
    scheme = Seq.empty,
    subcontractors = Seq.empty,
    verificationBatches = Seq.empty,
    verifications = Seq.empty,
    submissions = Seq.empty
  )

  private val mockSessionRepository          = mock[SessionRepository]
  private val mockVerificationService        = mock[VerificationService]
  private val mockVerificationHistoryService = mock[VerificationHistoryService]

  when(mockSessionRepository.set(any)) thenReturn Future.successful(true)
  when(mockVerificationService.getSubmittedVerifications(any)(any)) thenReturn Future.successful(
    stubSubmittedVerificationsResponse
  )
  when(mockVerificationHistoryService.toVerificationHistoryData(any)) thenReturn verificationHistoryData

  private lazy val view = app.injector.instanceOf[VerificationHistorySelectTaxYearView]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[IdentifierAction] toInstance FakeIdentifierAction(isAgent = false)(parsers),
      bind[SessionRepository] toInstance mockSessionRepository,
      bind[VerificationService] toInstance mockVerificationService,
      bind[VerificationHistoryService] toInstance mockVerificationHistoryService
    )
    .build()

  "VerificationHistorySelectTaxYear Controller" - {

    "must redirect to verification history page when history has 0 tax years" in {
      mockSessionData(Some(userAnswersWithVerificationHistoryData))
      mockVerificationTaxYears(Seq.empty)

      val request = FakeRequest(GET, verificationHistorySelectTaxYearRoute)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.VerificationHistoryController.onPageLoad(AllTaxYears.toPath).url
      contentAsString(result) mustBe empty

      verify(mockVerificationService).getSubmittedVerifications(eqTo(givenCisId))(any)
      verifyNoMoreInteractions(mockVerificationService)
    }

    "must redirect to verification history page when history has 1 tax year" in {
      mockSessionData(Some(userAnswersWithVerificationHistoryData))
      mockVerificationTaxYears(Seq(TaxYearPeriod(1999)))

      val request = FakeRequest(GET, verificationHistorySelectTaxYearRoute)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.VerificationHistoryController.onPageLoad(AllTaxYears.toPath).url
      contentAsString(result) mustBe empty

      verify(mockVerificationService).getSubmittedVerifications(eqTo(givenCisId))(any)
      verifyNoMoreInteractions(mockVerificationService)
    }

    "must show select tax year page when history has 2 tax years" in {
      val givenTaxYears = Seq(TaxYearPeriod(1999), TaxYearPeriod(2000))

      mockSessionData(Some(userAnswersWithVerificationHistoryData))
      mockVerificationTaxYears(givenTaxYears)

      val request = FakeRequest(GET, verificationHistorySelectTaxYearRoute)
      val result  = route(app, request).value

      status(result) mustEqual OK
      redirectLocation(result) mustBe empty
      contentAsString(result) mustBe view(formProvider(givenTaxYears), givenTaxYears)(request, messages(app)).toString

      verify(mockVerificationService).getSubmittedVerifications(eqTo(givenCisId))(any)
      verifyNoMoreInteractions(mockVerificationService)
    }

    "must redirect to Journey Recovery when no user answers exist and form submitted" in {
      mockSessionData(None)

      val request = FakeRequest(POST, verificationHistorySelectTaxYearRoute).withFormUrlEncodedBody("value" -> "all")
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }

    "must treat 'all' as AllTaxYears and redirect to all tax years verification history" in {
      mockSessionData(Some(userAnswersWithVerificationHistoryData))

      val request = FakeRequest(POST, verificationHistorySelectTaxYearRoute).withFormUrlEncodedBody("value" -> "all")
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.VerificationHistoryController.onPageLoad(AllTaxYears.toPath).url
    }

    "must redirect to single year verification history when a tax year is submitted" in {
      mockSessionData(Some(userAnswersWithVerificationHistoryData))
      mockVerificationTaxYears(Seq(TaxYearPeriod(2026)))

      val request = FakeRequest(POST, verificationHistorySelectTaxYearRoute).withFormUrlEncodedBody("value" -> "2026")
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.VerificationHistoryController.onPageLoad(TaxYear(2026).toPath).url
    }

    "must return BAD_REQUEST when invalid data is submitted" in {
      val taxYears = 2024 to 2026 map TaxYearPeriod.apply
      val form     = formProvider(taxYears)

      mockSessionData(Some(userAnswersWithVerificationHistoryData))
      mockVerificationTaxYears(taxYears)

      val invalidFormData = Map("value" -> "invalid")
      val request         =
        FakeRequest(POST, verificationHistorySelectTaxYearRoute).withFormUrlEncodedBody(invalidFormData.toSeq*)
      val boundForm       = form.bind(invalidFormData)

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm, taxYears)(request, messages(app)).toString
    }

    "must redirect to Journey Recovery for a GET when no existing data is found" in {
      mockSessionData(None)

      val request = FakeRequest(GET, verificationHistorySelectTaxYearRoute)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {
      mockSessionData(None)

      val request = FakeRequest(POST, verificationHistorySelectTaxYearRoute).withFormUrlEncodedBody("value" -> "all")
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }
  }

  override def afterEach(): Unit = clearInvocations(
    mockSessionRepository,
    mockVerificationService,
    mockVerificationHistoryService
  )

  private def mockSessionData(userAnswersOpt: Option[UserAnswers]) =
    when(mockSessionRepository.get(any)) thenReturn Future.successful(userAnswersOpt)

  private def mockVerificationTaxYears(taxYears: Seq[TaxYearPeriod]) =
    when(mockVerificationHistoryService.getSubmittedVerificationTaxYears(any)) thenReturn taxYears
}
