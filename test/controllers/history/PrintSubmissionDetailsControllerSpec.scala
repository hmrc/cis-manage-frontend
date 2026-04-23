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

package controllers.history

import base.SpecBase
import models.UserAnswers
import models.history.*
import models.response.GetSubmittedMonthlyReturnsDataResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.SubmittedMonthlyReturnToPrintQuery
import services.{ManageService, SubmittedReturnsService}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.history.PrintSubmissionDetailsView

import java.time.Instant
import scala.concurrent.Future

class PrintSubmissionDetailsControllerSpec extends SpecBase with MockitoSugar {

  "PrintSubmissionDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockManageService = mock[ManageService]

      val mockResponse = GetSubmittedMonthlyReturnsDataResponse(
        scheme = SubmittedSchemeData("Scheme Name", "163", "AB0063"),
        monthlyReturnId = 3000L,
        taxYear = 2025,
        taxMonth = 1,
        returnType = "nil",
        monthlyReturnItems = Seq.empty,
        submission = SubmittedSubmissionData(
          submissionId = 10L,
          submissionType = Some("Original"),
          activeObjectId = Some(20L),
          status = "Accepted",
          hmrcMarkGenerated = Some("mark1"),
          hmrcMarkGgis = Some("ggis1"),
          emailRecipient = Some("test@example.com"),
          acceptedTime = Some(Instant.now())
        )
      )

      when(mockManageService.getSubmittedMonthlyReturnsData(any(), any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(mockResponse))

      val submittedReturnsService = mock[SubmittedReturnsService]

      val monthYear              = "April 2026"
      val submittedTime          = "8:46am"
      val submittedDate          = "16 March 2025"
      val receiptReferenceNumber = "6QEDAHDREBY455GDNCPMDCNDFBDBJSJSJDNDDHDJDZ5"
      val submissionType         = "standard"
      val contractorName         = "PAL 355 Scheme"
      val payeReference          = "123/AB456"
      val totalPaymentsMade      = "£1900"
      val totalCostOfMaterials   = "£616"
      val totalTaxDeducted       = "£380"

      val subcontractors = Seq(
        SubcontractorPayment("BuildRight Construction", "£165", "£95", "£95"),
        SubcontractorPayment("Northern Trades Ltd", "£75", "£55", "£55"),
        SubcontractorPayment("TyneWear Ltd", "£165", "£125", "£55")
      )

      val model = SubmittedReturnPrintViewModel(
        monthYear = monthYear,
        submittedTime = submittedTime,
        submittedDate = submittedDate,
        receiptReferenceNumber = receiptReferenceNumber,
        submissionType = submissionType,
        contractorName = contractorName,
        payeReference = payeReference,
        totalPaymentsMade = totalPaymentsMade,
        totalCostOfMaterials = totalCostOfMaterials,
        totalTaxDeducted = totalTaxDeducted,
        subcontractors = subcontractors
      )

      when(submittedReturnsService.buildSubmittedReturnPrintViewModel(any(), any())).thenReturn(model)

      val userAnswers: UserAnswers = userAnswersWithCisId
        .set(
          SubmittedMonthlyReturnToPrintQuery,
          SubmittedMonthlyReturnData(
            monthlyReturnId = 1L,
            taxYear = 2026,
            taxMonth = 4,
            nilReturnIndicator = "Y",
            status = "Submitted",
            supersededBy = None,
            amendmentStatus = Some("None"),
            monthlyReturnItems = Some("items")
          )
        )
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[ManageService].toInstance(mockManageService),
          bind[SubmittedReturnsService].toInstance(submittedReturnsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.history.routes.PrintSubmissionDetailsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PrintSubmissionDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(model)(request, messages(application)).toString
      }
    }

    "must redirect to SystemErrorController when ManageService fails" in {

      val userAnswers: UserAnswers = emptyUserAnswers
        .set(
          SubmittedMonthlyReturnToPrintQuery,
          SubmittedMonthlyReturnData(
            monthlyReturnId = 1L,
            taxYear = 2026,
            taxMonth = 4,
            nilReturnIndicator = "Y",
            status = "Submitted",
            supersededBy = None,
            amendmentStatus = Some("None"),
            monthlyReturnItems = Some("items")
          )
        )
        .success
        .value

      val submittedReturnsService = mock[SubmittedReturnsService]
      val mockManageService       = mock[ManageService]

      when(mockManageService.getSubmittedMonthlyReturnsData(any(), any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[ManageService].toInstance(mockManageService),
          bind[SubmittedReturnsService].toInstance(submittedReturnsService)
        )
        .build()

      running(application) {

        val request = FakeRequest(GET, controllers.history.routes.PrintSubmissionDetailsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      verifyNoInteractions(submittedReturnsService)
    }

    "must redirect to Journey Recovery for a GET if SubmittedMonthlyReturnToPrintQuery missing in user answers" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCisId)).build()

      running(application) {

        val request = FakeRequest(GET, controllers.history.routes.PrintSubmissionDetailsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if CisId missing in user answers" in {

      val userAnswers: UserAnswers = emptyUserAnswers
        .set(
          SubmittedMonthlyReturnToPrintQuery,
          SubmittedMonthlyReturnData(
            monthlyReturnId = 1L,
            taxYear = 2026,
            taxMonth = 4,
            nilReturnIndicator = "Y",
            status = "Submitted",
            supersededBy = None,
            amendmentStatus = Some("None"),
            monthlyReturnItems = Some("items")
          )
        )
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, controllers.history.routes.PrintSubmissionDetailsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
