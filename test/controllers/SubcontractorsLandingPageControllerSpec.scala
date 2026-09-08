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
import config.FrontendAppConfig
import controllers.actions.HasClientGuard
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.{ActionFilter, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.SubcontractorsLandingPageView
import scala.concurrent.{ExecutionContext, Future}

class SubcontractorsLandingPageControllerSpec extends SpecBase with MockitoSugar {
  private val instanceId = "CIS-123"

  implicit val ec: ExecutionContext = ExecutionContext.global

  private val hasClientGuard = mock[HasClientGuard]

  private val passThroughFilter =
    new ActionFilter[DataRequest] {
      override protected def executionContext: ExecutionContext                         = ec
      override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] =
        Future.successful(None)
    }

  when(hasClientGuard.forInstanceId(any[String])).thenReturn(passThroughFilter)

  "SubcontractorsLandingPageController" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = Seq(bind[HasClientGuard].toInstance(hasClientGuard))
      ).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.SubcontractorsLandingPageController.onPageLoad(instanceId).url
        )
        val result  = route(application, request).value

        val view = application.injector.instanceOf[SubcontractorsLandingPageView]

        status(result) mustBe OK

        implicit val appConfig: FrontendAppConfig =
          application.injector.instanceOf[FrontendAppConfig]

        contentAsString(result) mustEqual
          view()(request, appConfig, messages(application)).toString
      }
    }
  }
}
