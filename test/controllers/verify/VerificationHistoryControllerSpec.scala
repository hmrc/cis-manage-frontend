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
import models.UserAnswers
import models.response.GetSubmittedVerificationsResponse
import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear}
import models.verify.{VerificationHistoryData, VerificationRequestData, VerificationTaxYearSelection}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify as mockVerify, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.CisIdPage
import pages.verify.VerificationHistoryDataPage
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{VerificationHistoryService, VerificationService}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.*
import views.html.verify.{NoVerificationHistoryView, VerificationHistoryView}

import java.time.LocalDate
import scala.concurrent.Future

class VerificationHistoryControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {
  import play.api.inject.guice.GuiceApplicationBuilder

  private val cisId = "900063"

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

  private val submittedVerificationsResponse = GetSubmittedVerificationsResponse(
    scheme = Seq.empty,
    subcontractors = Seq.empty,
    verificationBatches = Seq.empty,
    verifications = Seq.empty,
    submissions = Seq.empty
  )

  private val verificationHistoryData = VerificationHistoryData(
    verificationRequests = Seq(
      verificationRequestData("V0004528765", LocalDate.of(2026, 4, 6), 2026)
    )
  )

  private val mockSessionRepository          = mock[SessionRepository]
  private val mockVerificationHistoryService = mock[VerificationHistoryService]
  private val mockVerificationService        = mock[VerificationService]

  private val userAnswersWithCisId = emptyUserAnswers.set(CisIdPage, cisId).success.value

  private val userAnswersWithCisIdAndVerificationHistoryData = userAnswersWithCisId
    .set(VerificationHistoryDataPage, verificationHistoryData)
    .success
    .value

  def mockVerificationServiceReturnsData(): Unit = {
    when(
      mockVerificationService.getSubmittedVerifications(
        any[String]
      )(any[HeaderCarrier])
    ).thenReturn(Future.successful(submittedVerificationsResponse))

    when(
      mockVerificationHistoryService.toVerificationHistoryData(
        submittedVerificationsResponse
      )
    ).thenReturn(verificationHistoryData)
  }

  def mockVerificationServiceFails(): Unit =
    when(
      mockVerificationService.getSubmittedVerifications(
        any[String]
      )(any[HeaderCarrier])
    ).thenReturn(Future.failed(new RuntimeException("boom")))

  def mockSingleYearViewModelReturns(model: Option[VerificationHistoryPageViewModel]): Unit =
    when(
      mockVerificationHistoryService.buildSingleYearViewModel(any, any, any)
    ).thenReturn(model)

  def mockAllYearsViewModelReturns(model: Option[VerificationHistoryPageViewModel]): Unit =
    when(
      mockVerificationHistoryService.buildAllYearsViewModel(
        any[VerificationHistoryData],
        any[String]
      )
    ).thenReturn(model)

  def unauthorisedUrl: String =
    controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad().url

  def journeyRecoveryUrl: String =
    controllers.routes.JourneyRecoveryController.onPageLoad().url

  private lazy val view = app.injector.instanceOf[VerificationHistoryView]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[IdentifierAction] toInstance new FakeIdentifierAction(isAgent = false)(stubControllerComponents().parsers),
      bind[SessionRepository] toInstance mockSessionRepository,
      bind[VerificationService] toInstance mockVerificationService,
      bind[VerificationHistoryService] toInstance mockVerificationHistoryService
    )
    .build()

  "VerificationHistoryController" - {
    "onPageLoad(TaxYear) must return NOT_FOUND when CIS ID and verification history data are present but tax year selection is invalid" in {
      val givenTaxYearSelection = "invalid-tax-year-selection"
      mockSessionData(Some(userAnswersWithCisIdAndVerificationHistoryData))
      mockSingleYearViewModelReturns(Some(viewModel))

      val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoad(givenTaxYearSelection).url)
      val result  = route(app, request).value

      status(result) mustEqual NOT_FOUND
      contentAsString(result) mustEqual notFoundView()(request, messages(app)).toString

      verifyNoInteractions(mockVerificationHistoryService)
      verifyNoInteractions(mockVerificationService)
    }

    "onPageLoad(TaxYear) must return OK when CIS ID and verification history data are present" in {
      val givenTaxYear = TaxYear(2026)
      mockSessionData(Some(userAnswersWithCisIdAndVerificationHistoryData))
      mockSingleYearViewModelReturns(Some(viewModel))

      val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoad(givenTaxYear.toPath).url)
      val result  = route(app, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(viewModel)(request, messages(app)).toString

      mockVerify(mockVerificationHistoryService)
        .buildSingleYearViewModel(verificationHistoryData, givenTaxYear.startYear, cisId)
      verifyNoInteractions(mockVerificationService)
    }

    "onPageLoad(AllTaxYears) must return OK when CIS ID and verification history data are present" in {
      mockSessionData(Some(userAnswersWithCisIdAndVerificationHistoryData))
      mockAllYearsViewModelReturns(Some(viewModel))

      val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoad(AllTaxYears.toPath).url)
      val result  = route(app, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(viewModel)(request, messages(app)).toString

      mockVerify(mockVerificationHistoryService).buildAllYearsViewModel(verificationHistoryData, cisId)
      verifyNoInteractions(mockVerificationService)
    }

    "onPageLoad(TaxYear) must retrieve and convert submitted verifications when CIS ID is present but VerificationHistoryDataPage is missing" in {
      mockSessionData(Some(userAnswersWithCisId))
      mockVerificationServiceReturnsData()
      mockSingleYearViewModelReturns(Some(viewModel))

      val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoad(TaxYear(2026).toPath).url)
      val result  = route(app, request).value

      status(result) mustEqual OK

      mockVerify(mockVerificationService)
        .getSubmittedVerifications(any[String])(any[HeaderCarrier])

      mockVerify(mockVerificationHistoryService)
        .toVerificationHistoryData(submittedVerificationsResponse)
      mockVerify(mockVerificationHistoryService)
        .buildSingleYearViewModel(any, any, any)
    }

    "onPageLoad(AllTaxYears) must retrieve and convert submitted verifications when CIS ID is present but VerificationHistoryDataPage is missing" in {
      mockSessionData(Some(userAnswersWithCisId))
      mockVerificationServiceReturnsData()
      mockAllYearsViewModelReturns(Some(viewModel))

      val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoad(AllTaxYears.toPath).url)
      val result  = route(app, request).value

      status(result) mustEqual OK

      mockVerify(mockVerificationService)
        .getSubmittedVerifications(any[String])(any[HeaderCarrier])

      mockVerify(mockVerificationHistoryService)
        .toVerificationHistoryData(submittedVerificationsResponse)
      mockVerify(mockVerificationHistoryService).buildAllYearsViewModel(any[VerificationHistoryData], any[String])
    }

    "onPageLoad(TaxYear) must redirect when CisIdPage is missing" in {
      mockSessionData(Some(emptyUserAnswers))

      val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoad(TaxYear(2026).toPath).url)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual unauthorisedUrl
    }

    "onPageLoad(AllTaxYears) must redirect when CisIdPage is missing" in {
      mockSessionData(Some(emptyUserAnswers))

      val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoad(AllTaxYears.toPath).url)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual unauthorisedUrl
    }

    "must return 404 Not Found when buildSingleYearViewModel returns None" in {
      val givenTaxYear = TaxYear(2026)
      mockSessionData(Some(userAnswersWithCisIdAndVerificationHistoryData))
      mockSingleYearViewModelReturns(None)

      val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoad(givenTaxYear.toPath).url)
      val result  = route(app, request).value

      status(result) mustEqual NOT_FOUND
      contentAsString(result) mustEqual notFoundView()(request, messages(app)).toString

      mockVerify(mockVerificationHistoryService)
        .buildSingleYearViewModel(verificationHistoryData, givenTaxYear.startYear, cisId)
    }

    "must show no history page when buildAllYearsViewModel returns None" in {
      mockSessionData(Some(userAnswersWithCisIdAndVerificationHistoryData))
      mockAllYearsViewModelReturns(None)

      val request       = FakeRequest(GET, routes.VerificationHistoryController.onPageLoad(AllTaxYears.toPath).url)
      val result        = route(app, request).value
      val noHistoryView = app.injector.instanceOf[NoVerificationHistoryView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual noHistoryView(cisId)(request, messages(app)).toString

      mockVerify(mockVerificationHistoryService).buildAllYearsViewModel(verificationHistoryData, cisId)
    }

    "onPageLoad(TaxYear) must redirect to JourneyRecovery when resolveVerificationHistoryData fails" in {
      mockSessionData(Some(userAnswersWithCisId))
      mockVerificationServiceFails()

      val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoad(TaxYear(2026).toPath).url)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual journeyRecoveryUrl

      mockVerify(mockVerificationService)
        .getSubmittedVerifications(any[String])(any[HeaderCarrier])
    }

    "onPageLoad(AllTaxYears) must redirect to JourneyRecovery when resolveVerificationHistoryData fails" in {
      mockSessionData(Some(userAnswersWithCisId))
      mockVerificationServiceFails()

      val request = FakeRequest(GET, routes.VerificationHistoryController.onPageLoad(AllTaxYears.toPath).url)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual journeyRecoveryUrl

      mockVerify(mockVerificationService)
        .getSubmittedVerifications(any[String])(any[HeaderCarrier])
    }
  }

  override def afterEach(): Unit =
    reset(mockVerificationService)
    reset(mockVerificationHistoryService)

  private def mockSessionData(userAnswersOpt: Option[UserAnswers]) =
    when(mockSessionRepository.get(any)) thenReturn Future.successful(userAnswersOpt)
}
