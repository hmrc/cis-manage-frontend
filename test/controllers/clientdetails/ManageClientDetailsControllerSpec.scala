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

package controllers.clientdetails

import base.SpecBase
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.clientdetails.ManageClientDetailsView
import controllers.actions.{ClientListStatusGuard, HasClientGuard}
import models.requests.{DataRequest, IdentifierRequest}
import play.api.mvc.{ActionFilter, Result}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import scala.concurrent.{ExecutionContext, Future}

class ManageClientDetailsControllerSpec extends SpecBase with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global

  private val clientListStatusGuard = mock[ClientListStatusGuard]
  private val hasClientGuard        = mock[HasClientGuard]

  private val passThroughIdentifierFilter =
    new ActionFilter[IdentifierRequest] {
      override protected def executionContext: ExecutionContext                               = ec
      override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] =
        Future.successful(None)
    }

  private val passThroughDataFilter =
    new ActionFilter[DataRequest] {
      override protected def executionContext: ExecutionContext                         = ec
      override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] =
        Future.successful(None)
    }

  "ManageClientDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      when(clientListStatusGuard.groupB(any()))
        .thenReturn(passThroughIdentifierFilter)

      when(hasClientGuard.currentClient)
        .thenReturn(passThroughDataFilter)

      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          additionalBindings = Seq(
            bind[ClientListStatusGuard].toInstance(clientListStatusGuard),
            bind[HasClientGuard].toInstance(hasClientGuard)
          )
        ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ManageClientDetailsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ManageClientDetailsView]

        val fakeUniqueId: String    = "1"
        val fakeClientName: String  = "{Client}"
        val fakeEmployerRef: String = "{123/ab4}"
        val fakeClientRef: String   = "{AOR1}"

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fakeUniqueId, fakeClientName, fakeEmployerRef, fakeClientRef)(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
