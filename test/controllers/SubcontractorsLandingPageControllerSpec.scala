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
import models.{CisTaxpayerSearchResult, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.{AgentClientsPage, CisIdPage, ContractorNamePage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.SubcontractorsLandingPageView

class SubcontractorsLandingPageControllerSpec extends SpecBase with MockitoSugar {

  private val instanceId      = "CIS-123"
  private val contractorName  = "ABC Construction Ltd"
  private val agentClientName = "Client Ltd"

  "SubcontractorsLandingPageController.onPageLoad (contractor)" - {

    "must return OK and the correct view when ContractorNamePage is present" in {
      val userAnswers: UserAnswers =
        userAnswersWithCisId
          .set(CisIdPage, instanceId)
          .success
          .value
          .set(ContractorNamePage, contractorName)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          additionalBindings = Seq.empty
        ).build()

      running(application) {
        implicit val appConfig: FrontendAppConfig =
          application.injector.instanceOf[FrontendAppConfig]

        val request =
          FakeRequest(
            GET,
            controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId).url
          )

        val result = route(application, request).value
        val view   = application.injector.instanceOf[SubcontractorsLandingPageView]

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(contractorName)(request, appConfig, messages(application)).toString
      }
    }

    "must redirect to JourneyRecoveryController when ContractorNamePage is missing" in {
      val userAnswers: UserAnswers =
        userAnswersWithCisId
          .set(CisIdPage, instanceId)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          additionalBindings = Seq.empty
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId).url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "SubcontractorsLandingPageController.onPageLoad (agent)" - {

    "must return OK and the correct view when client with matching instanceId and schemeName exists" in {
      val client = CisTaxpayerSearchResult(
        uniqueId = instanceId,
        taxOfficeNumber = "163",
        taxOfficeRef = "AB0063",
        agentOwnRef = Some("ownRef"),
        schemeName = Some(agentClientName),
        utr = Some("1234567890")
      )

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(AgentClientsPage, List(client))
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          additionalBindings = Seq.empty,
          isAgent = true
        ).build()

      running(application) {
        implicit val appConfig: FrontendAppConfig =
          application.injector.instanceOf[FrontendAppConfig]

        val request =
          FakeRequest(
            GET,
            controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId).url
          )

        val result = route(application, request).value
        val view   = application.injector.instanceOf[SubcontractorsLandingPageView]

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(agentClientName)(request, appConfig, messages(application)).toString
      }
    }

    "must redirect to JourneyRecoveryController when no matching client or schemeName is found" in {
      val otherClient = CisTaxpayerSearchResult(
        uniqueId = "OTHER-ID",
        taxOfficeNumber = "163",
        taxOfficeRef = "AB0063",
        agentOwnRef = Some("ownRef"),
        schemeName = Some(agentClientName),
        utr = Some("1234567890")
      )

      val userAnswersNoMatch: UserAnswers =
        emptyUserAnswers
          .set(AgentClientsPage, List(otherClient))
          .success
          .value

      val applicationNoMatch =
        applicationBuilder(
          userAnswers = Some(userAnswersNoMatch),
          additionalBindings = Seq.empty,
          isAgent = true
        ).build()

      running(applicationNoMatch) {
        val request =
          FakeRequest(
            GET,
            controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId).url
          )

        val result = route(applicationNoMatch, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      val clientNoName = CisTaxpayerSearchResult(
        uniqueId = instanceId,
        taxOfficeNumber = "163",
        taxOfficeRef = "AB0063",
        agentOwnRef = Some("ownRef"),
        schemeName = None,
        utr = Some("1234567890")
      )

      val userAnswersNoName: UserAnswers =
        emptyUserAnswers
          .set(AgentClientsPage, List(clientNoName))
          .success
          .value

      val applicationNoName =
        applicationBuilder(
          userAnswers = Some(userAnswersNoName),
          additionalBindings = Seq.empty,
          isAgent = true
        ).build()

      running(applicationNoName) {
        val request =
          FakeRequest(
            GET,
            controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId).url
          )

        val result = route(applicationNoName, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
