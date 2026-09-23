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

package controllers.actions

import base.SpecBase
import controllers.Execution.trampoline
import models.requests.CisIdDataRequest
import models.{CisTaxpayerSearchResult, EmployerReference, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.AgentClientsPage
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.PrepopService
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.Future

class FormpRdsReconcileActionSpec extends SpecBase with MockitoSugar {

  private val cisId              = "cis-123"
  private val taxOfficeNumber    = "123"
  private val taxOfficeReference = "AB456"
  private val employerRef        = EmployerReference(taxOfficeNumber, taxOfficeReference)

  private class Harness(prepopService: PrepopService) extends FormpRdsReconcileActionImpl(prepopService) {
    def callFilter[A](request: CisIdDataRequest[A]): Future[Option[Result]] = filter(request)
  }

  private def contractorRequest =
    CisIdDataRequest(FakeRequest(), "id", emptyUserAnswers, cisId, employerReference = Some(employerRef))

  private def agentRequest(userAnswers: UserAnswers) =
    CisIdDataRequest(FakeRequest(), "id", userAnswers, cisId, employerReference = None, isAgent = true)

  "FormpRdsReconcileAction" - {

    "must pass through (None) when the FORMP-RDS comparison succeeds for a contractor" in {
      val prepopService = mock[PrepopService]
      when(
        prepopService.prepopulateContractorKnownFacts(eqTo(cisId), eqTo(taxOfficeNumber), eqTo(taxOfficeReference))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.unit)

      val result = new Harness(prepopService).callFilter(contractorRequest)

      whenReady(result)(_ mustBe None)
      verify(prepopService).prepopulateContractorKnownFacts(
        eqTo(cisId),
        eqTo(taxOfficeNumber),
        eqTo(taxOfficeReference)
      )(any[HeaderCarrier])
    }

    "must resolve the tax office from AgentClientsPage for an agent and pass through on success" in {
      val prepopService = mock[PrepopService]
      val client        =
        CisTaxpayerSearchResult(
          cisId,
          taxOfficeNumber,
          taxOfficeReference,
          agentOwnRef = None,
          schemeName = None,
          utr = None
        )
      val userAnswers   = emptyUserAnswers.set(AgentClientsPage, List(client)).success.value

      when(
        prepopService.prepopulateContractorKnownFacts(eqTo(cisId), eqTo(taxOfficeNumber), eqTo(taxOfficeReference))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.unit)

      val result = new Harness(prepopService).callFilter(agentRequest(userAnswers))

      whenReady(result)(_ mustBe None)
      verify(prepopService).prepopulateContractorKnownFacts(
        eqTo(cisId),
        eqTo(taxOfficeNumber),
        eqTo(taxOfficeReference)
      )(any[HeaderCarrier])
    }

    "must redirect to the unauthorised organisation page when the tax office details cannot be resolved" in {
      val prepopService = mock[PrepopService]

      val result = new Harness(prepopService).callFilter(agentRequest(emptyUserAnswers))

      whenReady(result) { maybeResult =>
        maybeResult.value.header.status mustEqual SEE_OTHER
        redirectLocation(Future.successful(maybeResult.value)).value mustEqual
          controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad().url
      }
      verify(prepopService, never).prepopulateContractorKnownFacts(any(), any(), any())(any[HeaderCarrier])
    }

    "must redirect to the unauthorised organisation page when the contractor data is missing (412)" in {
      val prepopService = mock[PrepopService]
      when(prepopService.prepopulateContractorKnownFacts(any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.failed(UpstreamErrorResponse("not found", PRECONDITION_FAILED, PRECONDITION_FAILED)))

      val result = new Harness(prepopService).callFilter(contractorRequest)

      whenReady(result) { maybeResult =>
        maybeResult.value.header.status mustEqual SEE_OTHER
        redirectLocation(Future.successful(maybeResult.value)).value mustEqual
          controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad().url
      }
    }

    "must redirect to the system error page when the comparison fails unexpectedly" in {
      val prepopService = mock[PrepopService]
      when(prepopService.prepopulateContractorKnownFacts(any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val result = new Harness(prepopService).callFilter(contractorRequest)

      whenReady(result) { maybeResult =>
        maybeResult.value.header.status mustEqual SEE_OTHER
        redirectLocation(Future.successful(maybeResult.value)).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
