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

package controllers.actions

import base.SpecBase
import models.requests.DataRequest
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.SEE_OTHER
import play.api.mvc.AnyContent
import repositories.SessionRepository
import services.ConstructionIndustrySchemeService
import scala.concurrent.ExecutionContext

class HasClientGuardSpec extends SpecBase with MockitoSugar {

  given ExecutionContext = ExecutionContext.global

  private val cisService        = mock[ConstructionIndustrySchemeService]
  private val sessionRepository = mock[SessionRepository]

  private val guard =
    new HasClientGuard(cisService, sessionRepository)

  "HasClientGuard.checkCurrentClient" - {

    "must allow the request when the user is not an agent" in {
      val request = mock[DataRequest[AnyContent]]

      when(request.isAgent).thenReturn(false)

      guard.checkCurrentClient(request).futureValue mustBe None

      verifyNoInteractions(cisService)
    }

    "must redirect to the system error page when CisId is missing" in {
      val request = mock[DataRequest[AnyContent]]

      when(request.isAgent).thenReturn(true)
      when(request.userAnswers).thenReturn(emptyUserAnswers)

      val result = guard.checkCurrentClient(request).futureValue

      result mustBe defined

      result.value.header.status mustEqual SEE_OTHER
      result.value.header.headers.get("Location").value mustEqual
        controllers.routes.SystemErrorController.onPageLoad().url

      verifyNoInteractions(cisService)
    }
  }

  "HasClientGuard.checkForInstanceId" - {

    "must redirect to the system error page when the client cannot be found" in {
      val request = mock[DataRequest[AnyContent]]

      when(request.isAgent).thenReturn(true)
      when(request.userAnswers).thenReturn(emptyUserAnswers)

      val result =
        guard.checkForInstanceId(request, "instance-id").futureValue

      result mustBe defined

      result.value.header.status mustEqual SEE_OTHER
      result.value.header.headers.get("Location").value mustEqual
        controllers.routes.SystemErrorController.onPageLoad().url

      verifyNoInteractions(cisService)
    }
  }
}
