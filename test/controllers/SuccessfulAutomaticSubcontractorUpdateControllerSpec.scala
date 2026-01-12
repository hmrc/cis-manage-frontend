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
import play.api.test.Helpers.*
import services.PrepopService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.SuccessfulAutomaticSubcontractorUpdateViewModel
import views.html.SuccessfulAutomaticSubcontractorUpdateView

import scala.concurrent.{ExecutionContext, Future}

class SuccessfulAutomaticSubcontractorUpdateControllerSpec extends SpecBase {

  val mockPrepopService: PrepopService                            = mock[PrepopService]
  val mockSchemeAccessProvider: AuthorizedForSchemeActionProvider = mock[AuthorizedForSchemeActionProvider]

  "SuccessfulAutomaticSubcontractorUpdate Controller" - {

    "must return OK and the correct view for a GET" in {
      val subcontractorsList: Seq[SuccessfulAutomaticSubcontractorUpdateViewModel] = Seq(
        SuccessfulAutomaticSubcontractorUpdateViewModel("Alice, A", "1111111111", " ", "01 Jan 2014"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Bob, B", "2222222222", " ", "01 Jan 2014"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Dave, D", "4444444444", "V1000000009", "07 May 2015"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Charles, C", "3333333333", "V1000000009", "01 Jan 2014"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Elise, E", "5555555555", "V1000000009", "07 May 2015"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Frank, F", "6666666666", "V1000000009", "07 Jan 2018")
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[PrepopService].toInstance(mockPrepopService),
          bind[AuthorizedForSchemeActionProvider].toInstance(mockSchemeAccessProvider)
        )
        .build()

      val instanceId = "900001"
      val targetKey  = "subcontractors"

      running(application) {

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

        val result = route(application, request).value

        val view = application.injector.instanceOf[SuccessfulAutomaticSubcontractorUpdateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(subcontractorsList, instanceId, targetKey)(
          request,
          messages(application)
        ).toString
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
      val targetKey  = "subcontractors"

      running(application) {
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
      val targetKey  = "subcontractors"

      running(application) {
        val request = FakeRequest(
          GET,
          routes.SuccessfulAutomaticSubcontractorUpdateController.onPageLoad(instanceId, targetKey).url
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
  }
}
