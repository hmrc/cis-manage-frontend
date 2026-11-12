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
import models.requests.RequestUserType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, MissingBearerToken}
import uk.gov.hmrc.http.HeaderCarrier
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.IntroductionView

import scala.concurrent.{ExecutionContext, Future}

class IntroductionControllerSpec extends SpecBase {

  private def connectorReturning(authResult: Future[Option[AffinityGroup]]): AuthConnector =
    new AuthConnector {
      override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
        hc: HeaderCarrier,
        ec: ExecutionContext
      ): Future[A] = authResult.asInstanceOf[Future[A]]
    }

  "IntroductionController.onPageLoad" - {
    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AuthConnector].toInstance(connectorReturning(Future.failed(new MissingBearerToken))))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.IntroductionController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IntroductionView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(
          request.addAttr(RequestUserType.IsSignedOutKey, true),
          applicationConfig,
          messages(application)
        ).toString
      }
    }
  }

  "IntroductionController.onPageLoad account link" - {

    def pageFor(authResult: Future[Option[AffinityGroup]]): String = {
      val connector   = mock[AuthConnector]
      when(connector.authorise[Option[AffinityGroup]](any(), any())(any(), any())).thenReturn(authResult)
      val application = applicationBuilder().overrides(bind[AuthConnector].toInstance(connector)).build()
      running(application) {
        contentAsString(route(application, FakeRequest(GET, routes.IntroductionController.onPageLoad().url)).value)
      }
    }

    "must show 'Back to agent services' for a signed in agent" in {
      val html = pageFor(Future.successful(Some(AffinityGroup.Agent)))
      html must include("Back to agent services")
      html must not include "Back to business tax account"
    }

    "must show 'Back to business tax account' for a signed in organisation" in {
      pageFor(Future.successful(Some(AffinityGroup.Organisation))) must include("Back to business tax account")
    }

    "must not show an account link when there is no active session" in {
      val html = pageFor(Future.failed(new MissingBearerToken))
      html must not include "Back to business tax account"
      html must not include "Back to agent services"
    }

    "must still show the language toggle when there is no active session" in {
      pageFor(Future.failed(new MissingBearerToken)) must include("hmrc-service-navigation-language-select")
    }
  }

  "IntroductionController.affinityGroupRouting" - {

    "for a contractor" - {

      "must redirect to the contractor landing page" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.IntroductionController.affinityGroupRouting().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.contractor.routes.ContractorLandingController
            .onPageLoad()
            .url
        }
      }

    }

    "for an agent" - {

      "must redirect to the agent landing page" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = None, isAgent = true)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.IntroductionController.affinityGroupRouting().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.agent.routes.RetrievingClientController
            .onPageLoad()
            .url
        }
      }

    }

  }
}
