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

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import base.SpecBase
import config.FrontendAppConfig
import models.{CisTaxpayerSearchResult, UnsubmittedMonthlyReturnsResponse, UnsubmittedMonthlyReturnsRow, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{AgentClientsPage, CisIdPage, ContractorNamePage}
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ManageService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.ReturnLandingViewModel
import views.html.ReturnsLandingView

import java.time.LocalDateTime
import scala.concurrent.Future

class ReturnsLandingControllerSpec extends SpecBase with MockitoSugar {

  private val instanceId      = "CIS-123"
  private val contractorName  = "ABC Construction Ltd"
  private val agentClientName = "Client Ltd"

  private val returnsList = Seq(
    ReturnLandingViewModel("August 2025", "Standard", "19 September 2025", "Accepted"),
    ReturnLandingViewModel("July 2025", "Nil", "19 August 2025", "Accepted"),
    ReturnLandingViewModel("June 2025", "Standard", "18 July 2025", "Accepted")
  )

  private val unsubmittedResponse = UnsubmittedMonthlyReturnsResponse(
    unsubmittedCisReturns = Seq(
      UnsubmittedMonthlyReturnsRow(2025, 8, "Standard", "Accepted", Some(LocalDateTime.parse("2025-09-19T00:00:00"))),
      UnsubmittedMonthlyReturnsRow(2025, 7, "Nil", "Accepted", Some(LocalDateTime.parse("2025-08-19T00:00:00"))),
      UnsubmittedMonthlyReturnsRow(2025, 6, "Standard", "Accepted", Some(LocalDateTime.parse("2025-07-18T00:00:00")))
    )
  )

  "ReturnsLandingController.onPageLoad (contractor)" - {

    "must return OK and the correct view when ContractorNamePage is present" in {
      val userAnswers: UserAnswers =
        userAnswersWithCisId
          .set(CisIdPage, instanceId)
          .success
          .value
          .set(ContractorNamePage, contractorName)
          .success
          .value

      val mockManageService = mock[ManageService]
      when(mockManageService.getUnsubmittedMonthlyReturns(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(unsubmittedResponse))

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          additionalBindings = Seq(
            inject.bind[ManageService].toInstance(mockManageService)
          )
        ).build()

      running(application) {
        implicit val appConfig: FrontendAppConfig =
          application.injector.instanceOf[FrontendAppConfig]

        val request = FakeRequest(
          GET,
          controllers.routes.ReturnsLandingController.onPageLoad(instanceId).url
        )

        val result = route(application, request).value
        val view   = application.injector.instanceOf[ReturnsLandingView]

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(contractorName, returnsList)(request, appConfig, messages(application)).toString
      }
    }

    "must redirect to SystemErrorController when ContractorNamePage is missing" in {
      val userAnswers: UserAnswers =
        userAnswersWithCisId
          .set(CisIdPage, instanceId)
          .success
          .value

      val mockManageService = mock[ManageService]

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          additionalBindings = Seq(
            inject.bind[ManageService].toInstance(mockManageService)
          )
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            controllers.routes.ReturnsLandingController.onPageLoad(instanceId).url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }

  "ReturnsLandingController.onPageLoad (agent)" - {

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

      val mockManageService = mock[ManageService]
      when(mockManageService.getUnsubmittedMonthlyReturns(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(unsubmittedResponse))

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          isAgent = true,
          additionalBindings = Seq(
            inject.bind[ManageService].toInstance(mockManageService)
          )
        ).build()

      running(application) {
        implicit val appConfig: FrontendAppConfig =
          application.injector.instanceOf[FrontendAppConfig]

        val request =
          FakeRequest(
            GET,
            controllers.routes.ReturnsLandingController.onPageLoad(instanceId).url
          )

        val result = route(application, request).value
        val view   = application.injector.instanceOf[ReturnsLandingView]

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(agentClientName, returnsList)(request, appConfig, messages(application)).toString
      }
    }

    "must redirect to SystemErrorController when no matching client or schemeName is found" in {
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

      val mockManageService = mock[ManageService]

      val applicationNoMatch =
        applicationBuilder(
          userAnswers = Some(userAnswersNoMatch),
          isAgent = true,
          additionalBindings = Seq(
            inject.bind[ManageService].toInstance(mockManageService)
          )
        ).build()

      running(applicationNoMatch) {
        val request =
          FakeRequest(
            GET,
            controllers.routes.ReturnsLandingController.onPageLoad(instanceId).url
          )

        val result = route(applicationNoMatch, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url
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
          isAgent = true,
          additionalBindings = Seq(
            inject.bind[ManageService].toInstance(mockManageService)
          )
        ).build()

      running(applicationNoName) {
        val request =
          FakeRequest(
            GET,
            controllers.routes.ReturnsLandingController.onPageLoad(instanceId).url
          )

        val result = route(applicationNoName, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
