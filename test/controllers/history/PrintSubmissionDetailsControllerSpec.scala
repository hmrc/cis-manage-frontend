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
import models.history.*
import models.response.GetSubmittedMonthlyReturnsDataResponse
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{ManageService, SubmittedReturnsService}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.history.PrintSubmissionDetailsView

import java.time.Instant
import scala.concurrent.Future

class PrintSubmissionDetailsControllerSpec extends SpecBase with MockitoSugar {

  lazy val printSubmissionDetailsRoute: String =
    controllers.history.routes.PrintSubmissionDetailsController.onPageLoad(2026, 4, "N").url

  "PrintSubmissionDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockManageService       = mock[ManageService]
      val submittedReturnsService = mock[SubmittedReturnsService]

      val mockResponse = GetSubmittedMonthlyReturnsDataResponse(
        scheme = SubmittedSchemeData("Scheme Name", "163", "AB0063"),
        monthlyReturnId = 3000L,
        taxYear = 2026,
        taxMonth = 4,
        nilReturnIndicator = "Y",
        monthlyReturnItems = Seq.empty,
        submission = SubmittedSubmissionData(
          submissionId = 10L,
          submissionType = Some("MONTHLY_RETURN"),
          activeObjectId = Some(20L),
          status = "Accepted",
          hmrcMarkGenerated = Some("mark1"),
          hmrcMarkGgis = Some("ggis1"),
          emailRecipient = Some("test@example.com"),
          acceptedTime = Some(Instant.now())
        )
      )

      val model = SubmittedReturnPrintViewModel(
        monthYear = "April 2026",
        submittedTime = "8:46am",
        submittedDate = "16 March 2025",
        receiptReferenceNumber = "6QEDAHDREBY455GDNCPMDCNDFBDBJSJSJDNDDHDJDZ5",
        submissionType = "standard",
        contractorName = "PAL 355 Scheme",
        payeReference = "123/AB456",
        totalPaymentsMade = "£1900",
        totalCostOfMaterials = "£616",
        totalTaxDeducted = "£380",
        subcontractors = Seq.empty
      )

      when(
        mockManageService.getSubmittedMonthlyReturnsData(eqTo("1"), eqTo(2026), eqTo(4), eqTo("N"))(
          any[HeaderCarrier]
        )
      ).thenReturn(Future.successful(mockResponse))

      when(submittedReturnsService.buildSubmittedReturnPrintViewModel(any(), any()))
        .thenReturn(model)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCisId))
        .overrides(
          bind[ManageService].toInstance(mockManageService),
          bind[SubmittedReturnsService].toInstance(submittedReturnsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, printSubmissionDetailsRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[PrintSubmissionDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          model,
          controllers.history.routes.SubmittedReturnsController.onPageLoadAllYears().url
        )(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when from is single" in {

      val routeWithSingleFrom =
        controllers.history.routes.PrintSubmissionDetailsController
          .onPageLoad(2026, 4, "N", "single")
          .url

      val mockManageService       = mock[ManageService]
      val submittedReturnsService = mock[SubmittedReturnsService]

      val mockResponse = GetSubmittedMonthlyReturnsDataResponse(
        scheme = SubmittedSchemeData("Scheme Name", "163", "AB0063"),
        monthlyReturnId = 3000L,
        taxYear = 2026,
        taxMonth = 4,
        nilReturnIndicator = "Y",
        monthlyReturnItems = Seq.empty,
        submission = SubmittedSubmissionData(
          submissionId = 10L,
          submissionType = Some("MONTHLY_RETURN"),
          activeObjectId = Some(20L),
          status = "Accepted",
          hmrcMarkGenerated = Some("mark1"),
          hmrcMarkGgis = Some("ggis1"),
          emailRecipient = Some("test@example.com"),
          acceptedTime = Some(Instant.now())
        )
      )

      val model = SubmittedReturnPrintViewModel(
        monthYear = "April 2026",
        submittedTime = "8:46am",
        submittedDate = "16 March 2025",
        receiptReferenceNumber = "6QEDAHDREBY455GDNCPMDCNDFBDBJSJSJDNDDHDJDZ5",
        submissionType = "standard",
        contractorName = "PAL 355 Scheme",
        payeReference = "123/AB456",
        totalPaymentsMade = "£1900",
        totalCostOfMaterials = "£616",
        totalTaxDeducted = "£380",
        subcontractors = Seq.empty
      )

      when(
        mockManageService.getSubmittedMonthlyReturnsData(eqTo("1"), eqTo(2026), eqTo(4), eqTo("N"))(
          any[HeaderCarrier]
        )
      ).thenReturn(Future.successful(mockResponse))

      when(submittedReturnsService.buildSubmittedReturnPrintViewModel(any(), any()))
        .thenReturn(model)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCisId))
        .overrides(
          bind[ManageService].toInstance(mockManageService),
          bind[SubmittedReturnsService].toInstance(submittedReturnsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routeWithSingleFrom)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[PrintSubmissionDetailsView]

        val historyUrl =
          controllers.history.routes.SubmittedReturnsController
            .onPageLoadSingleYear("2026")
            .url

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(model, historyUrl)(request, messages(application)).toString
      }
    }

    "must redirect to SystemErrorController when ManageService fails" in {

      val submittedReturnsService = mock[SubmittedReturnsService]
      val mockManageService       = mock[ManageService]

      when(mockManageService.getSubmittedMonthlyReturnsData(any(), any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCisId))
        .overrides(
          bind[ManageService].toInstance(mockManageService),
          bind[SubmittedReturnsService].toInstance(submittedReturnsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, printSubmissionDetailsRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      verifyNoInteractions(submittedReturnsService)
    }

    "must redirect to Journey Recovery for a GET if CisId missing in user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, printSubmissionDetailsRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
