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
import models.verify.VerificationTaxYearSelection.{TaxYear, TaxYearPeriod}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.VerificationHistorySelectTaxYearPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{VerificationHistoryService, VerificationService}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class GetSubmittedVerificationsControllerSpec extends SpecBase with MockitoSugar {

  private val cisId = "1"

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
      contractorName = "",
      employerReference = "",
      receiptReferenceNumber = "",
      subcontractorsToVerify = Seq.empty,
      subcontractorsToReverify = Seq.empty
    )

  private val response =
    GetSubmittedVerificationsResponse(
      scheme = Seq.empty,
      subcontractors = Seq.empty,
      verificationBatches = Seq.empty,
      verifications = Seq.empty,
      submissions = Seq.empty
    )

  private val singleYearData =
    VerificationHistoryData(
      verificationRequests = Seq(
        verificationRequestData("V001", LocalDate.of(2025, 4, 5), 2024)
      )
    )

  private val multiYearData =
    VerificationHistoryData(
      verificationRequests = Seq(
        verificationRequestData("V001", LocalDate.of(2026, 4, 6), 2026),
        verificationRequestData("V002", LocalDate.of(2025, 4, 5), 2024)
      )
    )

  private val routeUrl = routes.GetSubmittedVerificationsController.onPageLoad().url

  trait Setup {
    val mockSessionRepository: SessionRepository                   = mock[SessionRepository]
    val mockVerificationService: VerificationService               = mock[VerificationService]
    val mockVerificationHistoryService: VerificationHistoryService = mock[VerificationHistoryService]

    when(mockSessionRepository.set(any[UserAnswers])) thenReturn Future.successful(true)
    when(mockVerificationService.getSubmittedVerifications(any[String])(any[HeaderCarrier]))
      .thenReturn(Future.successful(response))

    def application =
      applicationBuilder(userAnswers = Some(userAnswersWithCisId))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[VerificationService].toInstance(mockVerificationService),
          bind[VerificationHistoryService].toInstance(mockVerificationHistoryService)
        )
        .build()
  }

  "GetSubmittedVerificationsController" - {

    "must redirect to the select tax year page when there is more than one tax year" in new Setup {
      when(mockVerificationHistoryService.toVerificationHistoryData(response)).thenReturn(multiYearData)
      when(mockVerificationHistoryService.getSubmittedVerificationTaxYears(multiYearData))
        .thenReturn(Seq(TaxYearPeriod(2026), TaxYearPeriod(2024)))

      val app = application

      running(app) {
        val result = route(app, FakeRequest(GET, routeUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.VerificationHistorySelectTaxYearController.onPageLoad().url
      }
    }

    "must redirect to single year history and store the only tax year when there is one tax year" in new Setup {
      when(mockVerificationHistoryService.toVerificationHistoryData(response)).thenReturn(singleYearData)
      when(mockVerificationHistoryService.getSubmittedVerificationTaxYears(singleYearData))
        .thenReturn(Seq(TaxYearPeriod(2024)))

      val app = application

      running(app) {
        val result = route(app, FakeRequest(GET, routeUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.VerificationHistoryController.onPageLoadSingleYear().url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())
        captor.getValue.get(VerificationHistorySelectTaxYearPage).value mustEqual TaxYear(2024)
      }
    }

    "must redirect to Journey Recovery when history data conversion fails" in new Setup {
      when(mockVerificationHistoryService.toVerificationHistoryData(response))
        .thenThrow(new IllegalStateException("Submitted verification is missing accepted date"))

      val app = application

      running(app) {
        val result = route(app, FakeRequest(GET, routeUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
