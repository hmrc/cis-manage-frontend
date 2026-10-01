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
import controllers.actions.{AuthorizedForSchemeActionProvider, FakeAuthorizedForSchemeAction}
import models.Scheme
import models.response.{GetSubcontractor, GetSubcontractorListResponse}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{PrepopService, SubcontractorService}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.SuccessfulAutomaticSubcontractorUpdateViewModel
import views.html.SuccessfulAutomaticSubcontractorUpdateView

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class SuccessfulAutomaticSubcontractorUpdateControllerSpec extends SpecBase {

  val mockPrepopService: PrepopService                            = mock[PrepopService]
  val mockSubcontractorService: SubcontractorService              = mock[SubcontractorService]
  val mockSchemeAccessProvider: AuthorizedForSchemeActionProvider = mock[AuthorizedForSchemeActionProvider]

  private lazy val view = app.injector.instanceOf[SuccessfulAutomaticSubcontractorUpdateView]

  override def fakeApplication(): Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
    .overrides(
      bind[PrepopService].toInstance(mockPrepopService),
      bind[SubcontractorService].toInstance(mockSubcontractorService),
      bind[AuthorizedForSchemeActionProvider].toInstance(mockSchemeAccessProvider)
    )
    .build()

  "SuccessfulAutomaticSubcontractorUpdate Controller" - {

    "must return OK and the correct view for a GET" in {
      val subcontractorsList: Seq[SuccessfulAutomaticSubcontractorUpdateViewModel] = Seq(
        SuccessfulAutomaticSubcontractorUpdateViewModel("Smith, Alan", "1234567890", "V000001", "6 Apr 2026"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Partners Ltd", "3333333333", "", "1 Jan 2014")
      )

      when(mockSubcontractorService.getSubcontractorList(eqTo("900001"))(any[HeaderCarrier]))
        .thenReturn(
          Future.successful(
            GetSubcontractorListResponse(
              Seq(
                subcontractor(
                  subcontractorId = 1L,
                  utr = Some("1234567890"),
                  firstName = Some("Alan"),
                  surname = Some("Smith"),
                  subcontractorType = Some("soletrader"),
                  verificationNumber = Some("V000001"),
                  createDate = Some(LocalDateTime.of(2026, 4, 6, 10, 0))
                ),
                subcontractor(
                  subcontractorId = 2L,
                  utr = Some("3333333333"),
                  partnershipTradingName = Some("Partners Ltd"),
                  subcontractorType = Some("partnership"),
                  createDate = Some(LocalDateTime.of(2014, 1, 1, 0, 0))
                )
              )
            )
          )
        )

      val instanceId = "900001"
      val targetKey  = "subcontractors"

      val request = FakeRequest(
        GET,
        routes.SuccessfulAutomaticSubcontractorUpdateController.onPageLoad(instanceId, targetKey).url
      )

      when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])).thenReturn(
        Future.successful(
          Some(
            Scheme(
              schemeId = 1,
              instanceId = instanceId,
              utr = Some("ABC123"),
              name = Some("John"),
              prePopSuccessful = Some("Y"),
              subcontractorCounter = Some(1)
            )
          )
        )
      )

      when(mockSchemeAccessProvider.apply(eqTo(instanceId))(using any[ExecutionContext]))
        .thenReturn(new FakeAuthorizedForSchemeAction)

      val result = route(app, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(subcontractorsList, instanceId, targetKey)(
        request,
        messages(app)
      ).toString
    }

    "must redirect to journey recovery if prepopSuccessful is 'N'" in {
      val instanceId = "900001"
      val targetKey  = "subcontractors"

      val request = FakeRequest(
        GET,
        routes.SuccessfulAutomaticSubcontractorUpdateController.onPageLoad(instanceId, targetKey).url
      )

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

      when(mockSchemeAccessProvider.apply(eqTo(instanceId))(using any[ExecutionContext]))
        .thenReturn(new FakeAuthorizedForSchemeAction)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).isDefined mustEqual true
      redirectLocation(result).value mustEqual "/there-is-a-problem"
    }

    "must fall back to crn or nino and show a name when trading name is missing" in {
      val subcontractorsList: Seq[SuccessfulAutomaticSubcontractorUpdateViewModel] = Seq(
        SuccessfulAutomaticSubcontractorUpdateViewModel("Acme Ltd", "12345678", "", "6 Apr 2026"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Smith, Alan", "AB123456C", "", "6 Apr 2026"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("No name provided", "", "", "6 Apr 2026")
      )

      when(mockSubcontractorService.getSubcontractorList(eqTo("900001"))(any[HeaderCarrier]))
        .thenReturn(
          Future.successful(
            GetSubcontractorListResponse(
              Seq(
                subcontractor(
                  subcontractorId = 1L,
                  crn = Some("12345678"),
                  tradingName = Some("Acme Ltd"),
                  subcontractorType = Some("company"),
                  createDate = Some(LocalDateTime.of(2026, 4, 6, 10, 0))
                ),
                subcontractor(
                  subcontractorId = 2L,
                  nino = Some("AB123456C"),
                  firstName = Some("Alan"),
                  surname = Some("Smith"),
                  subcontractorType = Some("soletrader"),
                  createDate = Some(LocalDateTime.of(2026, 4, 6, 10, 0))
                ),
                subcontractor(
                  subcontractorId = 3L,
                  subcontractorType = Some("trust"),
                  createDate = Some(LocalDateTime.of(2026, 4, 6, 10, 0))
                )
              )
            )
          )
        )

      val instanceId = "900001"
      val targetKey  = "subcontractors"

      val request = FakeRequest(
        GET,
        routes.SuccessfulAutomaticSubcontractorUpdateController.onPageLoad(instanceId, targetKey).url
      )

      when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])).thenReturn(
        Future.successful(
          Some(
            Scheme(
              schemeId = 1,
              instanceId = instanceId,
              utr = Some("ABC123"),
              name = Some("John"),
              prePopSuccessful = Some("Y"),
              subcontractorCounter = Some(1)
            )
          )
        )
      )

      when(mockSchemeAccessProvider.apply(eqTo(instanceId))(using any[ExecutionContext]))
        .thenReturn(new FakeAuthorizedForSchemeAction)

      val result = route(app, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(subcontractorsList, instanceId, targetKey)(
        request,
        messages(app)
      ).toString
    }

    "must redirect to journey recovery when the subcontractor list cannot be loaded" in {
      val instanceId = "900001"
      val targetKey  = "subcontractors"

      val request = FakeRequest(
        GET,
        routes.SuccessfulAutomaticSubcontractorUpdateController.onPageLoad(instanceId, targetKey).url
      )

      when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])).thenReturn(
        Future.successful(
          Some(
            Scheme(
              schemeId = 1,
              instanceId = instanceId,
              utr = Some("ABC123"),
              name = Some("John"),
              prePopSuccessful = Some("Y"),
              subcontractorCounter = Some(1)
            )
          )
        )
      )

      when(mockSubcontractorService.getSubcontractorList(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("list failed")))

      when(mockSchemeAccessProvider.apply(eqTo(instanceId))(using any[ExecutionContext]))
        .thenReturn(new FakeAuthorizedForSchemeAction)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual "/there-is-a-problem"
    }

    "must redirect to system error if there is no scheme" in {
      val instanceId = "900001"
      val targetKey  = "subcontractors"

      val request = FakeRequest(
        GET,
        routes.SuccessfulAutomaticSubcontractorUpdateController.onPageLoad(instanceId, targetKey).url
      )

      when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])).thenReturn(
        Future.successful(None)
      )

      when(mockSchemeAccessProvider.apply(eqTo(instanceId))(using any[ExecutionContext]))
        .thenReturn(new FakeAuthorizedForSchemeAction)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).isDefined mustEqual true
      redirectLocation(result).value mustEqual "/system-error/there-is-a-problem"
    }

    "must redirect to SubcontractorsLandingPageController on submit with subcontractors target" in {
      val instanceId = "900001"
      val targetKey  = "subcontractors"

      val request = FakeRequest(
        POST,
        routes.SuccessfulAutomaticSubcontractorUpdateController.onSubmit(instanceId, targetKey).url
      )

      when(mockSchemeAccessProvider.apply(eqTo(instanceId))(using any[ExecutionContext]))
        .thenReturn(new FakeAuthorizedForSchemeAction)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SubcontractorsLandingPageController.onPageLoad(instanceId).url
    }

    "must redirect to ReturnsLandingController on submit with returns target" in {
      val instanceId = "900001"
      val targetKey  = "returnDue"

      val request = FakeRequest(
        POST,
        routes.SuccessfulAutomaticSubcontractorUpdateController.onSubmit(instanceId, targetKey).url
      )

      when(mockSchemeAccessProvider.apply(eqTo(instanceId))(using any[ExecutionContext]))
        .thenReturn(new FakeAuthorizedForSchemeAction)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.ReturnsLandingController.onPageLoad(instanceId).url
    }

    "must redirect to JourneyRecoveryController on submit with notices target" in {
      val instanceId = "900001"
      val targetKey  = "newNotices"

      val request = FakeRequest(
        POST,
        routes.SuccessfulAutomaticSubcontractorUpdateController.onSubmit(instanceId, targetKey).url
      )

      when(mockSchemeAccessProvider.apply(eqTo(instanceId))(using any[ExecutionContext]))
        .thenReturn(new FakeAuthorizedForSchemeAction)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
    }

    "must return NotFound on submit with unknown target" in {
      val instanceId = "900001"
      val targetKey  = "unknownTarget"

      val request = FakeRequest(
        POST,
        routes.SuccessfulAutomaticSubcontractorUpdateController.onSubmit(instanceId, targetKey).url
      )

      when(mockSchemeAccessProvider.apply(eqTo(instanceId))(using any[ExecutionContext]))
        .thenReturn(new FakeAuthorizedForSchemeAction)

      val result = route(app, request).value

      status(result) mustEqual NOT_FOUND
    }
  }

  private def subcontractor(
    subcontractorId: Long,
    utr: Option[String] = None,
    crn: Option[String] = None,
    nino: Option[String] = None,
    firstName: Option[String] = None,
    surname: Option[String] = None,
    tradingName: Option[String] = None,
    partnershipTradingName: Option[String] = None,
    subcontractorType: Option[String] = None,
    verificationNumber: Option[String] = None,
    createDate: Option[LocalDateTime] = None
  ): GetSubcontractor =
    GetSubcontractor(
      subcontractorId = subcontractorId,
      utr = utr,
      pageVisited = None,
      partnerUtr = None,
      crn = crn,
      firstName = firstName,
      nino = nino,
      secondName = None,
      surname = surname,
      partnershipTradingName = partnershipTradingName,
      tradingName = tradingName,
      subcontractorType = subcontractorType,
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
      createDate = createDate,
      lastUpdate = None,
      subbieResourceRef = None,
      matched = None,
      autoVerified = None,
      verified = None,
      verificationNumber = verificationNumber,
      taxTreatment = None,
      verificationDate = None,
      version = None,
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = None
    )
}
