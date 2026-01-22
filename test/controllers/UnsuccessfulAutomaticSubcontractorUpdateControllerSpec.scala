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
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.PrepopService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.UnsuccessfulAutomaticSubcontractorUpdateView

import scala.concurrent.{ExecutionContext, Future}

class UnsuccessfulAutomaticSubcontractorUpdateControllerSpec extends SpecBase {

  val mockPrepopService: PrepopService                            = mock[PrepopService]
  val mockSchemeAccessProvider: AuthorizedForSchemeActionProvider = mock[AuthorizedForSchemeActionProvider]

  "UnsuccessfulAutomaticSubcontractorUpdate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[PrepopService].toInstance(mockPrepopService),
          bind[AuthorizedForSchemeActionProvider].toInstance(mockSchemeAccessProvider)
        )
        .build()

      val instanceId = "900001"

      running(application) {
        val request = FakeRequest(
          GET,
          routes.UnsuccessfulAutomaticSubcontractorUpdateController.onPageLoad(instanceId).url
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

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnsuccessfulAutomaticSubcontractorUpdateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(instanceId)(request, messages(application)).toString
      }
    }

    "must redirect to journey recovery if prepopSuccessful is 'Y'" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[PrepopService].toInstance(mockPrepopService),
          bind[AuthorizedForSchemeActionProvider].toInstance(mockSchemeAccessProvider)
        )
        .build()

      val instanceId = "900001"

      running(application) {
        val request = FakeRequest(
          GET,
          routes.UnsuccessfulAutomaticSubcontractorUpdateController.onPageLoad(instanceId).url
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

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).isDefined mustEqual true
        redirectLocation(result).value mustEqual "/there-is-a-problem"
      }
    }

    "must redirect to system error if there is no scheme" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[PrepopService].toInstance(mockPrepopService),
          bind[AuthorizedForSchemeActionProvider].toInstance(mockSchemeAccessProvider)
        )
        .build()

      val instanceId = "900001"

      running(application) {
        val request = FakeRequest(
          GET,
          routes.UnsuccessfulAutomaticSubcontractorUpdateController.onPageLoad(instanceId).url
        )

        when(mockPrepopService.getScheme(eqTo(instanceId))(any[HeaderCarrier])).thenReturn(
          Future.successful(None)
        )

        when(mockSchemeAccessProvider.apply(eqTo(instanceId))(using any[ExecutionContext]))
          .thenReturn(new FakeAuthorizedForSchemeAction)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).isDefined mustEqual true
        redirectLocation(result).value mustEqual "/system-error/there-is-a-problem"
      }
    }

    "must redirect to AddContractorDetailsController on submit" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AuthorizedForSchemeActionProvider].toInstance(mockSchemeAccessProvider)
        )
        .build()

      val instanceId = "900001"

      running(application) {
        val request = FakeRequest(
          POST,
          routes.UnsuccessfulAutomaticSubcontractorUpdateController.onSubmit(instanceId).url
        )

        when(mockSchemeAccessProvider.apply(eqTo(instanceId))(using any[ExecutionContext]))
          .thenReturn(new FakeAuthorizedForSchemeAction)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AddContractorDetailsController.onPageLoad().url
      }
    }
  }
}
