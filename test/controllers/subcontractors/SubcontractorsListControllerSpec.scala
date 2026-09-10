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

package controllers.subcontractors

import base.SpecBase
import config.FrontendAppConfig
import controllers.actions.HasClientGuard
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.{ActionFilter, Result}
import forms.subcontractors.SubcontractorsListFormProvider
import models.{Mode, NormalMode, UserAnswers}
import models.response.{GetSubcontractor, GetSubcontractorListResponse}
import models.{Mode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.CisIdPage
import pages.subcontractors.SubcontractorListPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import views.html.subcontractors.SubcontractorsListView

import java.time.LocalDateTime
import scala.jdk.CollectionConverters.*
import scala.concurrent.{ExecutionContext, Future}

class SubcontractorsListControllerSpec extends SpecBase with MockitoSugar {

  private val instanceId = "test-instance-id"
  private val mode: Mode = NormalMode

  implicit val ec: ExecutionContext = ExecutionContext.global

  private val hasClientGuard = mock[HasClientGuard]

  private val passThroughFilter =
    new ActionFilter[DataRequest] {
      override protected def executionContext: ExecutionContext                         = ec
      override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] =
        Future.successful(None)
    }

  when(hasClientGuard.forInstanceId(any[String])).thenReturn(passThroughFilter)

  private def stubView(mockView: SubcontractorsListView): Unit =
    when(
      mockView.apply(
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any()
      )(any(), any())
    ).thenReturn(Html("Subcontractors List View"))

  private val subcontractors = Seq(
    GetSubcontractor(
      subcontractorId = 1L,
      utr = Some("1234567890"),
      pageVisited = None,
      partnerUtr = None,
      crn = None,
      firstName = Some("Alan"),
      nino = None,
      secondName = None,
      surname = Some("Smith"),
      partnershipTradingName = None,
      tradingName = None,
      subcontractorType = Some("soleTrader"),
      addressLine1 = None,
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      country = None,
      postcode = None,
      emailAddress = None,
      phoneNumber = None,
      mobilePhoneNumber = None,
      worksReferenceNumber = None,
      createDate = Some(LocalDateTime.of(2026, 4, 6, 10, 0)),
      lastUpdate = None,
      subbieResourceRef = Some(10L),
      matched = None,
      autoVerified = None,
      verified = Some("Y"),
      verificationNumber = Some("V000001"),
      taxTreatment = Some("Gross"),
      verificationDate = None,
      version = None,
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = None
    ),
    GetSubcontractor(
      subcontractorId = 2L,
      utr = Some("9876543210"),
      pageVisited = None,
      partnerUtr = None,
      crn = None,
      firstName = Some("Brian"),
      nino = None,
      secondName = None,
      surname = Some("Jones"),
      partnershipTradingName = None,
      tradingName = None,
      subcontractorType = Some("soleTrader"),
      addressLine1 = None,
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      country = None,
      postcode = None,
      emailAddress = None,
      phoneNumber = None,
      mobilePhoneNumber = None,
      worksReferenceNumber = None,
      createDate = Some(LocalDateTime.of(2026, 5, 6, 10, 0)),
      lastUpdate = None,
      subbieResourceRef = Some(20L),
      matched = None,
      autoVerified = None,
      verified = Some("Y"),
      verificationNumber = Some("V000002"),
      taxTreatment = Some("Gross"),
      verificationDate = Some(
        LocalDateTime.of(2026, 5, 1, 0, 0)
      ),
      version = None,
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = None
    )
  )

  private val listResponse = GetSubcontractorListResponse(
    subcontractors = subcontractors
  )

  private val cisId = "test-cis-id"

  private def userAnswersWithSubcontractors: UserAnswers =
    emptyUserAnswers
      .set(CisIdPage, cisId)
      .success
      .value
      .set(SubcontractorListPage, listResponse)
      .success
      .value

  "SubcontractorsListController" - {

    "must return OK and display the subcontractors list view with default filters" in {
      val mockView =
        mock[SubcontractorsListView]

      stubView(mockView)

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithSubcontractors),
          additionalBindings = Seq(
            bind[HasClientGuard].toInstance(hasClientGuard)
          )
        ).overrides(
            bind[SubcontractorsListView].toInstance(mockView)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, mode).url
          )

        val result =
          route(application, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe "Subcontractors List View"
      }
    }

    "must return OK and display the subcontractors list view with search and filters applied" in {
      val mockView =
        mock[SubcontractorsListView]

      stubView(mockView)

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithSubcontractors),
          additionalBindings = Seq(
            bind[HasClientGuard].toInstance(hasClientGuard)
          )
        ).overrides(
            bind[SubcontractorsListView].toInstance(mockView)
          )
          .build()
      running(application) {
        val url =
          routes.SubcontractorsListController
            .onPageLoad(instanceId, mode)
            .url

        val request =
          FakeRequest(
            GET,
            s"$url?searchTerm=Brian&verificationStatus=verified&taxTreatment=gross"
          )

        val result =
          route(application, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe "Subcontractors List View"
      }
    }

    "must redirect to the selected page with filters preserved when pagination is submitted" in {
      val application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithSubcontractors),
          additionalBindings = Seq(
            bind[HasClientGuard].toInstance(hasClientGuard)
          )
        ).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.SubcontractorsListController.onSubmit(instanceId, mode).url
          ).withFormUrlEncodedBody(
            "gotoPage"           -> "2",
            "searchTerm"         -> "Alan",
            "verificationStatus" -> "verified",
            "taxTreatment"       -> "gross",
            "sortBy"             -> "name",
            "sortOrder"          -> "ascending"
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        val redirectUrl =
          redirectLocation(result).value

        redirectUrl must include(
          routes.SubcontractorsListController.onPageLoad(instanceId, mode, 2).url
        )
        redirectUrl must include("searchTerm=Alan")
        redirectUrl must include("verificationStatus=verified")
        redirectUrl must include("taxTreatment=gross")
        redirectUrl must include("sortBy=name")
        redirectUrl must include("sortOrder=ascending")
      }
    }

    "must redirect to page 1 when gotoPage is not submitted" in {
      val application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithSubcontractors),
          additionalBindings = Seq(
            bind[HasClientGuard].toInstance(hasClientGuard)
          )
        ).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.SubcontractorsListController.onSubmit(instanceId, mode).url
          ).withFormUrlEncodedBody(
            "searchTerm"         -> "Alan",
            "verificationStatus" -> "verified",
            "taxTreatment"       -> "gross",
            "sortBy"             -> "name",
            "sortOrder"          -> "ascending"
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        val redirectUrl =
          redirectLocation(result).value

        redirectUrl must include(
          routes.SubcontractorsListController.onPageLoad(instanceId, mode).url
        )
        redirectUrl must include("searchTerm=Alan")
        redirectUrl must include("verificationStatus=verified")
        redirectUrl must include("taxTreatment=gross")
        redirectUrl must include("sortBy=name")
        redirectUrl must include("sortOrder=ascending")
      }
    }

    "must redirect to Journey Recovery for a GET when subcontractor list data is missing" in {
      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, mode).url
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when subcontractor list data is missing" in {
      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.SubcontractorsListController.onSubmit(instanceId, mode).url
          ).withFormUrlEncodedBody(
            "gotoPage"   -> "2",
            "searchTerm" -> "Alan"
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to no subcontractors exist when the stored list is empty" in {
      val userAnswers =
        emptyUserAnswers
          .set(CisIdPage, cisId)
          .success
          .value
          .set(SubcontractorListPage, GetSubcontractorListResponse(Seq.empty))
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, mode).url
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.NoSubcontractorsExistController.onPageLoad().url
      }
    }

    "must throw an IllegalStateException when subbieResourceRef is missing" in {
      val response =
        GetSubcontractorListResponse(
          subcontractors = Seq(
            subcontractors.head.copy(
              subbieResourceRef = None
            )
          )
        )

      val userAnswers =
        emptyUserAnswers
          .set(CisIdPage, cisId)
          .success
          .value
          .set(SubcontractorListPage, response)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, mode).url
          )

        val result =
          route(application, request).value

        val exception =
          intercept[IllegalStateException] {
            status(result)
          }

        exception.getMessage mustEqual
          "Missing subbieResourceRef for subcontractorId 1"
      }
    }
  }
}
