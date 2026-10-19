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

package controllers.splitter.core

import base.UnitSpec
import config.FrontendAppConfig
import connectors.RateLimitedAllowListConnector
import controllers.actions.FakeIdentifierAction
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, reset, verify, when}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class SplitterCoreControllerSpec extends UnitSpec {

  private val config    = mock[FrontendAppConfig]
  private val connector = mock[RateLimitedAllowListConnector]

  private def controller(isAgent: Boolean = false) =
    new SplitterCoreController(
      stubMessagesControllerComponents(),
      config,
      connector,
      new FakeIdentifierAction(isAgent)(stubControllerComponents().parsers)
    )

  private val request = FakeRequest(GET, "/some/path")

  private val introductionUrl = controllers.routes.IntroductionController.onPageLoad().url

  private def setUp(useAllowList: Boolean): Unit = {
    reset(config, connector)
    when(config.useRateLimitedAllowList).thenReturn(useAllowList)
    when(config.splitterServiceName).thenReturn("cis")
    when(config.splitterAllowListName).thenReturn("allow-list")
    when(config.legacyCisServiceCoreUrl).thenReturn("/construction-ind-scheme")
  }

  "SplitterCoreController.coreLandingPage" - {

    "must redirect to the legacy service when the allow list is disabled" in {
      setUp(useAllowList = false)

      val result = controller().coreLandingPage("some/path")(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual "/construction-ind-scheme/some/path"
      verify(connector, never()).checkAllowList(any(), any(), any())(using any())
    }

    "must redirect to the introduction page when an organisation is on the allow list" in {
      setUp(useAllowList = true)
      when(
        connector.checkAllowList(eqTo("cis"), eqTo("allow-list"), eqTo("taxOfficeNumber/taxOfficeReference"))(using
          any()
        )
      ).thenReturn(Future.successful(true))

      val result = controller().coreLandingPage("some/path")(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual introductionUrl
    }

    "must redirect to the legacy service when an organisation is not on the allow list" in {
      setUp(useAllowList = true)
      when(connector.checkAllowList(any(), any(), any())(using any())).thenReturn(Future.successful(false))

      val result = controller().coreLandingPage("some/path")(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual "/construction-ind-scheme/some/path"
    }

    "must check the agent reference and redirect to the introduction page when an agent is on the allow list" in {
      setUp(useAllowList = true)
      when(
        connector.checkAllowList(eqTo("cis"), eqTo("allow-list"), eqTo("agentReferenceNumber"))(using any())
      ).thenReturn(Future.successful(true))

      val result = controller(isAgent = true).coreLandingPage("some/path")(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual introductionUrl
    }

    "must redirect an agent to the legacy service when not on the allow list" in {
      setUp(useAllowList = true)
      when(connector.checkAllowList(any(), any(), any())(using any())).thenReturn(Future.successful(false))

      val result = controller(isAgent = true).coreLandingPage("some/path")(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual "/construction-ind-scheme/some/path"
    }
  }
}
