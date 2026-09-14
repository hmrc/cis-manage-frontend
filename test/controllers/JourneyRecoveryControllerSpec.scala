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

package controllers

import base.UnitSpec
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{clearInvocations, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import views.html.{JourneyRecoveryContinueView, JourneyRecoveryStartAgainView}

import scala.concurrent.Future

class JourneyRecoveryControllerSpec extends UnitSpec with BeforeAndAfterEach {
  import play.twirl.api.Html

  private given conf: FrontendAppConfig = mock[FrontendAppConfig]
  when(conf.constructionIndustryOrgAccountUrl) thenReturn "/cis/org/"
  when(conf.constructionIndustryAgentAccountUrl) thenReturn "/cis/agent/"

  private val stubContinueView    = mock[JourneyRecoveryContinueView]
  private val stubContinueContent = "JourneyRecoveryContinue"
  when(stubContinueView.apply(any)(any, any, any)) thenReturn Html(stubContinueContent)

  private val stubStartAgainView    = mock[JourneyRecoveryStartAgainView]
  private val stubStartAgainContent = "JourneyRecoveryStartAgain"
  when(stubStartAgainView.apply(any)(any, any, any)) thenReturn Html(stubStartAgainContent)

  private val controllerUnderTest = new JourneyRecoveryController(
    mockCisControllerComponents,
    mockSessionRepo,
    stubContinueView,
    stubStartAgainView
  )

  "JourneyRecovery Controller" - {

    Seq(
      ("AGENT",        true,  conf.constructionIndustryAgentAccountUrl),
      ("ORGANISATION", false, conf.constructionIndustryOrgAccountUrl)
    ).foreach { case (accountTypeSTR, isAgent, expectedUrl) =>
      s"when accountType is '$accountTypeSTR'" - {

        "when a relative continue Url is supplied" - {

          "must return OK and the continue view" in {
            if isAgent then mockCisControllerComponents.loginAsAgent() else mockCisControllerComponents.loginAsOrg()

            val continueUrl = RedirectUrl("/foo")
            val result      = controllerUnderTest.onPageLoad(Some(continueUrl))(FakeRequest())

            status(result) mustEqual OK
            contentAsString(result) mustEqual stubContinueContent
          }
        }

        "when an absolute continue Url is supplied" - {

          "must return OK and the start again view" in {
            if isAgent then mockCisControllerComponents.loginAsAgent() else mockCisControllerComponents.loginAsOrg()

            val continueUrl = RedirectUrl("https://foo.com")
            val result      = controllerUnderTest.onPageLoad(Some(continueUrl))(FakeRequest())

            status(result) mustEqual OK
            contentAsString(result) mustEqual stubStartAgainContent
          }
        }

        "when no continue Url is supplied" - {

          "must return OK and the start again view" in {
            if isAgent then mockCisControllerComponents.loginAsAgent() else mockCisControllerComponents.loginAsOrg()

            val result = controllerUnderTest.onPageLoad(continueUrl = None)(FakeRequest())

            status(result) mustEqual OK
            contentAsString(result) mustEqual stubStartAgainContent
          }
        }

        "when the session store is unavailable" - {

          "must still render the start again view using the base account URL" in {
            if isAgent then mockCisControllerComponents.loginAsAgent() else mockCisControllerComponents.loginAsOrg()
            when(mockSessionRepo.get(any)) thenReturn Future.failed(new RuntimeException("Mongo down"))

            val result = controllerUnderTest.onPageLoad()(FakeRequest())

            status(result) mustEqual OK
            contentAsString(result) mustEqual stubStartAgainContent

            verify(stubStartAgainView).apply(eqTo(expectedUrl))(any, any, any)
          }
        }
      }
    }

    "when accountType is AGENT and CisIdPage is missing" - {

      "must return the agent account URL without a cisId appended" in {
        mockCisControllerComponents.loginAsAgent()
        when(mockSessionRepo.get(any)) thenReturn Future.successful(Some(emptyUserAnswers))

        val result = controllerUnderTest.onPageLoad()(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual stubStartAgainContent

        verify(stubStartAgainView).apply(eqTo(conf.constructionIndustryAgentAccountUrl))(any, any, any)
      }
    }
  }

  override def afterEach(): Unit = clearInvocations(stubContinueView, stubStartAgainView)
}
