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

import base.UnitSpec
import models.Scheme
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.PrepopService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.RetrievingSubcontractorsView

import scala.concurrent.Future

class RetrievingSubcontractorsControllerSpec extends UnitSpec {
  private val stubView    = mock[RetrievingSubcontractorsView]
  private val stubContent = "RetrievingSubcontractors"
  when(stubView.apply()(any, any)) thenReturn play.twirl.api.Html(stubContent)

  private val mockPrepopService = mock[PrepopService]

  private val controllerUnderTest = new RetrievingSubcontractorsController(
    mockCisControllerComponents,
    stubView,
    mockPrepopService
  )

  val taxOfficeNumber: String    = "101"
  val taxOfficeReference: String = "AB0001"
  val instanceId: String         = "900001"
  val targetKey: String          = "subcontractors"

  "RetrievingSubcontractors Controller" - {

    "must return OK and the correct view for a GET" in {
      val result =
        controllerUnderTest.onPageLoad(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual stubContent
    }

    "start must redirect to SuccessfulAutomaticSubcontractorUpdateController when scheme has prePopSuccessful 'Y' and subcontractors" in {
      when(
        mockPrepopService.prepopulate(eqTo(taxOfficeNumber), eqTo(taxOfficeReference), eqTo(instanceId))(
          any[HeaderCarrier]
        )
      ) thenReturn Future.successful(true)

      when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])) thenReturn Future.successful(
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

      val result = controllerUnderTest.start(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)(FakeRequest())

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SuccessfulAutomaticSubcontractorUpdateController
        .onPageLoad(instanceId, targetKey)
        .url
    }

    "start must redirect to SuccessfulNoRecordsFoundController when scheme has prePopSuccessful 'Y' and no subcontractors" in {
      when(
        mockPrepopService.prepopulate(eqTo(taxOfficeNumber), eqTo(taxOfficeReference), eqTo(instanceId))(
          any[HeaderCarrier]
        )
      ) thenReturn Future.successful(true)

      when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])) thenReturn Future.successful(
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

      val result = controllerUnderTest.start(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)(FakeRequest())

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SuccessfulNoRecordsFoundController
        .onPageLoad(instanceId, targetKey)
        .url
    }

    "start must redirect to UnsuccessfulAutomaticSubcontractorUpdateController when scheme has prePopSuccessful 'N'" in {
      when(
        mockPrepopService.prepopulate(eqTo(taxOfficeNumber), eqTo(taxOfficeReference), eqTo(instanceId))(
          any[HeaderCarrier]
        )
      ) thenReturn Future.successful(true)

      when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])) thenReturn Future.successful(
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

      val result = controllerUnderTest.start(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)(FakeRequest())

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.UnsuccessfulAutomaticSubcontractorUpdateController
        .onPageLoad(instanceId)
        .url
    }

    "start must redirect to UnsuccessfulAutomaticSubcontractorUpdateController when scheme has no prePopSuccessful value" in {
      when(
        mockPrepopService.prepopulate(eqTo(taxOfficeNumber), eqTo(taxOfficeReference), eqTo(instanceId))(
          any[HeaderCarrier]
        )
      ) thenReturn Future.successful(true)

      when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])) thenReturn Future.successful(
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

      val result = controllerUnderTest.start(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)(FakeRequest())

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.UnsuccessfulAutomaticSubcontractorUpdateController
        .onPageLoad(instanceId)
        .url
    }

    "start must redirect to UnsuccessfulAutomaticSubcontractorUpdateController when there is no scheme" in {
      when(
        mockPrepopService.prepopulate(eqTo(taxOfficeNumber), eqTo(taxOfficeReference), eqTo(instanceId))(
          any[HeaderCarrier]
        )
      ) thenReturn Future.successful(true)

      when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])) thenReturn Future.successful(None)

      val result = controllerUnderTest.start(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)(FakeRequest())

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.UnsuccessfulAutomaticSubcontractorUpdateController
        .onPageLoad(instanceId)
        .url
    }

    "start must redirect to UnsuccessfulAutomaticSubcontractorUpdateController when prepopulate fails" in {
      when(
        mockPrepopService.prepopulate(eqTo(taxOfficeNumber), eqTo(taxOfficeReference), eqTo(instanceId))(
          any[HeaderCarrier]
        )
      ) thenReturn Future.successful(false)

      val result = controllerUnderTest.start(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)(FakeRequest())

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.UnsuccessfulAutomaticSubcontractorUpdateController
        .onPageLoad(instanceId)
        .url
    }
  }
}
