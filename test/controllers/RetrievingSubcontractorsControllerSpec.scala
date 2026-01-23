/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import base.SpecBase
import models.Scheme
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.PrepopService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.RetrievingSubcontractorsView

import scala.concurrent.Future

class RetrievingSubcontractorsControllerSpec extends SpecBase {

  val mockPrepopService: PrepopService = mock[PrepopService]

  val taxOfficeNumber: String    = "101"
  val taxOfficeReference: String = "AB0001"
  val instanceId: String         = "900001"
  val targetKey: String          = "subcontractors"

  "RetrievingSubcontractors Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.RetrievingSubcontractorsController
            .onPageLoad(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)
            .url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[RetrievingSubcontractorsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(
          request,
          messages(application)
        ).toString
      }
    }

    "start must redirect to SuccessfulAutomaticSubcontractorUpdateController when scheme has prePopSuccessful 'Y' and subcontractors" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[PrepopService].toInstance(mockPrepopService)
        )
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.RetrievingSubcontractorsController
            .start(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)
            .url
        )

        when(
          mockPrepopService.prepopulate(eqTo(taxOfficeNumber), eqTo(taxOfficeReference), eqTo(instanceId))(
            any[HeaderCarrier]
          )
        )
          .thenReturn(Future.successful(true))

        when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])).thenReturn(
          Future.successful(
            Some(
              Scheme(
                schemeId = 1,
                instanceId = instanceId,
                utr = Some("ABC123"),
                name = Some("John"),
                prePopSuccessful = Some("Y"),
                subcontractorCounter = Some(5)
              )
            )
          )
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SuccessfulAutomaticSubcontractorUpdateController
          .onPageLoad(instanceId, targetKey)
          .url
      }
    }

    "start must redirect to SuccessfulNoRecordsFoundController when scheme has prePopSuccessful 'Y' and no subcontractors" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[PrepopService].toInstance(mockPrepopService)
        )
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.RetrievingSubcontractorsController
            .start(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)
            .url
        )

        when(
          mockPrepopService.prepopulate(eqTo(taxOfficeNumber), eqTo(taxOfficeReference), eqTo(instanceId))(
            any[HeaderCarrier]
          )
        )
          .thenReturn(Future.successful(true))

        when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])).thenReturn(
          Future.successful(
            Some(
              Scheme(
                schemeId = 1,
                instanceId = instanceId,
                utr = Some("ABC123"),
                name = Some("John"),
                prePopSuccessful = Some("Y"),
                subcontractorCounter = Some(0)
              )
            )
          )
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SuccessfulNoRecordsFoundController
          .onPageLoad(instanceId, targetKey)
          .url
      }
    }

    "start must redirect to UnsuccessfulAutomaticSubcontractorUpdateController when scheme has prePopSuccessful 'N'" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[PrepopService].toInstance(mockPrepopService)
        )
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.RetrievingSubcontractorsController
            .start(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)
            .url
        )

        when(
          mockPrepopService.prepopulate(eqTo(taxOfficeNumber), eqTo(taxOfficeReference), eqTo(instanceId))(
            any[HeaderCarrier]
          )
        )
          .thenReturn(Future.successful(true))

        when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])).thenReturn(
          Future.successful(
            Some(
              Scheme(
                schemeId = 1,
                instanceId = instanceId,
                utr = Some("ABC123"),
                name = Some("John"),
                prePopSuccessful = Some("N"),
                subcontractorCounter = Some(1)
              )
            )
          )
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.UnsuccessfulAutomaticSubcontractorUpdateController
          .onPageLoad(instanceId)
          .url
      }
    }

    "start must redirect to UnsuccessfulAutomaticSubcontractorUpdateController when scheme has no prePopSuccessful value" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[PrepopService].toInstance(mockPrepopService)
        )
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.RetrievingSubcontractorsController
            .start(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)
            .url
        )

        when(
          mockPrepopService.prepopulate(eqTo(taxOfficeNumber), eqTo(taxOfficeReference), eqTo(instanceId))(
            any[HeaderCarrier]
          )
        )
          .thenReturn(Future.successful(true))

        when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])).thenReturn(
          Future.successful(
            Some(
              Scheme(
                schemeId = 1,
                instanceId = instanceId,
                utr = Some("ABC123"),
                name = Some("John"),
                prePopSuccessful = None,
                subcontractorCounter = Some(1)
              )
            )
          )
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.UnsuccessfulAutomaticSubcontractorUpdateController
          .onPageLoad(instanceId)
          .url
      }
    }

    "start must redirect to UnsuccessfulAutomaticSubcontractorUpdateController when there is no scheme" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[PrepopService].toInstance(mockPrepopService)
        )
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.RetrievingSubcontractorsController
            .start(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)
            .url
        )

        when(
          mockPrepopService.prepopulate(eqTo(taxOfficeNumber), eqTo(taxOfficeReference), eqTo(instanceId))(
            any[HeaderCarrier]
          )
        )
          .thenReturn(Future.successful(true))

        when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])).thenReturn(
          Future.successful(None)
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.UnsuccessfulAutomaticSubcontractorUpdateController
          .onPageLoad(instanceId)
          .url
      }
    }

    "start must redirect to UnsuccessfulAutomaticSubcontractorUpdateController when prepopulate fails" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[PrepopService].toInstance(mockPrepopService)
        )
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.RetrievingSubcontractorsController
            .start(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)
            .url
        )

        when(
          mockPrepopService.prepopulate(eqTo(taxOfficeNumber), eqTo(taxOfficeReference), eqTo(instanceId))(
            any[HeaderCarrier]
          )
        )
          .thenReturn(Future.successful(false))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.UnsuccessfulAutomaticSubcontractorUpdateController
          .onPageLoad(instanceId)
          .url
      }
    }
  }
}
