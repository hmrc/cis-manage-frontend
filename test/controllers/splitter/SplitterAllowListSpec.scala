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

package controllers.splitter

import base.UnitSpec
import config.FrontendAppConfig
import connectors.RateLimitedAllowListConnector
import models.EmployerReference
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, reset, verify, when}
import play.api.test.FakeRequest

import scala.concurrent.Future

class SplitterAllowListSpec extends UnitSpec {

  private val mockConfig    = mock[FrontendAppConfig]
  private val mockConnector = mock[RateLimitedAllowListConnector]

  private class TestAllowList extends SplitterAllowList {
    override protected val config: FrontendAppConfig                                    = mockConfig
    override protected val rateLimitedAllowListConnector: RateLimitedAllowListConnector = mockConnector

    def check[A](request: IdentifierRequest[A]): Future[Boolean] = checkAllowList(request)
  }

  private val underTest = new TestAllowList

  private def setUp(useAllowList: Boolean): Unit = {
    reset(mockConfig, mockConnector)
    when(mockConfig.useRateLimitedAllowList).thenReturn(useAllowList)
    when(mockConfig.splitterServiceName).thenReturn("cis")
    when(mockConfig.splitterAllowListName).thenReturn("allow-list")
  }

  private val organisation =
    IdentifierRequest(FakeRequest(), "id", Some(EmployerReference("123", "AB456")), None)
  private val agent        =
    IdentifierRequest(FakeRequest(), "id", None, Some("ARN1"), isAgent = true)

  "SplitterAllowList.checkAllowList" - {

    "must return false without calling the connector when the allow list is disabled" in {
      setUp(useAllowList = false)

      underTest.check(organisation).futureValue mustBe false
      verify(mockConnector, never()).checkAllowList(any(), any(), any())(using any())
    }

    "must check an organisation using its tax office number and reference" in {
      setUp(useAllowList = true)
      when(mockConnector.checkAllowList(eqTo("cis"), eqTo("allow-list"), eqTo("123/AB456"))(using any()))
        .thenReturn(Future.successful(true))

      underTest.check(organisation).futureValue mustBe true
    }

    "must check an agent using its agent reference" in {
      setUp(useAllowList = true)
      when(mockConnector.checkAllowList(eqTo("cis"), eqTo("allow-list"), eqTo("ARN1"))(using any()))
        .thenReturn(Future.successful(true))

      underTest.check(agent).futureValue mustBe true
    }

    "must return false without calling the connector when an organisation has no employer reference" in {
      setUp(useAllowList = true)

      underTest.check(organisation.copy(employerReference = None)).futureValue mustBe false
      verify(mockConnector, never()).checkAllowList(any(), any(), any())(using any())
    }

    "must return false without calling the connector when an agent has no agent reference" in {
      setUp(useAllowList = true)

      underTest.check(agent.copy(agentReference = None)).futureValue mustBe false
      verify(mockConnector, never()).checkAllowList(any(), any(), any())(using any())
    }
  }
}
