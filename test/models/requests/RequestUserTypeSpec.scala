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

package models.requests

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.FakeRequest

class RequestUserTypeSpec extends AnyWordSpec with Matchers {

  private val fake = FakeRequest()

  "RequestUserType.isAgent" should {
    "be true for an agent identifier request" in {
      RequestUserType.isAgent(IdentifierRequest(fake, "id", None, Some("arn"), isAgent = true)) mustBe true
    }
    "be false for an organisation identifier request" in {
      RequestUserType.isAgent(IdentifierRequest(fake, "id", None, None)) mustBe false
    }
    "be true for an agent optional data request" in {
      RequestUserType.isAgent(OptionalDataRequest(fake, "id", None, isAgent = true)) mustBe true
    }
    "be true for a request wrapping an agent data request (e.g. DeletableReturnRequest)" in {
      import controllers.actions.DeletableReturnRequest
      import models.{UnsubmittedMonthlyReturnsRow, UserAnswers}
      val dataRequest = DataRequest(fake, "id", UserAnswers("id"), isAgent = true)
      val row         = org.mockito.Mockito.mock(classOf[UnsubmittedMonthlyReturnsRow])
      RequestUserType.isAgent(DeletableReturnRequest(dataRequest, row)) mustBe true
    }
    "be false for an unauthenticated request" in {
      RequestUserType.isAgent(fake) mustBe false
    }
  }
}
