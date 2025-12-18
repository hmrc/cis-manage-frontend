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
import models.{CisTaxpayerSearchResult, EmployerReference}
import models.requests.DataRequest
import pages.AgentClientsPage
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthorizedForSchemeActionSpec extends SpecBase {

  private val taxOfficeNumber    = "123"
  private val taxOfficeReference = "ABC456"
  private val employerReference  = EmployerReference(taxOfficeNumber, taxOfficeReference)

  private val otherTaxOfficeNumber    = "456"
  private val otherTaxOfficeReference = "DEF789"
  private val otherEmployerReference  = EmployerReference(otherTaxOfficeNumber, otherTaxOfficeReference)

  private val client1 = CisTaxpayerSearchResult(
    uniqueId = "id1",
    taxOfficeNumber = taxOfficeNumber,
    taxOfficeRef = taxOfficeReference,
    agentOwnRef = Some("ref1"),
    schemeName = Some("Scheme 1"),
    utr = Some("1234567890")
  )

  private val client2 = CisTaxpayerSearchResult(
    uniqueId = "id2",
    taxOfficeNumber = otherTaxOfficeNumber,
    taxOfficeRef = otherTaxOfficeReference,
    agentOwnRef = Some("ref2"),
    schemeName = Some("Scheme 2"),
    utr = Some("0987654321")
  )

  "AuthorizedForSchemeAction" - {

    "for agents" - {

      "when the employer reference is in the agent's client list" - {
        "must allow access" in {
          val userAnswers = emptyUserAnswers.set(AgentClientsPage, List(client1, client2)).success.value
          val request     = DataRequest(FakeRequest(), "id", userAnswers, None, Some("agentRef"), isAgent = true)

          val action = AuthorizedForSchemeAction.canAccessScheme(employerReference)
          val result = action.invokeBlock(request, implicit request => Future.successful(Results.NoContent))

          whenReady(result) { result =>
            result mustBe Results.NoContent
          }
        }
      }

      "when the employer reference is not in the agent's client list" - {
        "must redirect to unauthorised page" in {
          val userAnswers = emptyUserAnswers.set(AgentClientsPage, List(client2)).success.value
          val request     = DataRequest(FakeRequest(), "id", userAnswers, None, Some("agentRef"), isAgent = true)

          val action = AuthorizedForSchemeAction.canAccessScheme(employerReference)
          val result = action.invokeBlock(request, implicit request => Future.successful(Results.NoContent))

          whenReady(result) { result =>

            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe Some(
              controllers.routes.UnauthorisedController.onPageLoad().url
            )
          }
        }
      }

      "when the agent has no clients in their list" - {
        "must redirect to unauthorised page" in {
          val userAnswers = emptyUserAnswers.set(AgentClientsPage, List.empty).success.value
          val request     = DataRequest(FakeRequest(), "id", userAnswers, None, Some("agentRef"), isAgent = true)

          val action = AuthorizedForSchemeAction.canAccessScheme(employerReference)
          val result = action.invokeBlock(request, implicit request => Future.successful(Results.NoContent))

          whenReady(result) { result =>
            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe Some(
              controllers.routes.UnauthorisedController.onPageLoad().url
            )
          }
        }
      }

      "when AgentClientsPage is not set in UserAnswers" - {
        "must redirect to unauthorised page" in {
          val request = DataRequest(FakeRequest(), "id", emptyUserAnswers, None, Some("agentRef"), isAgent = true)

          val action = AuthorizedForSchemeAction.canAccessScheme(employerReference)
          val result = action.invokeBlock(request, implicit request => Future.successful(Results.NoContent))

          whenReady(result) { result =>
            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe Some(
              controllers.routes.UnauthorisedController.onPageLoad().url
            )
          }
        }
      }
    }

    "for non-agents" - {

      "when the employer reference matches the request's employer reference" - {
        "must allow access" in {
          val request =
            DataRequest(FakeRequest(), "id", emptyUserAnswers, Some(employerReference), None)

          val action = AuthorizedForSchemeAction.canAccessScheme(employerReference)
          val result = action.invokeBlock(request, implicit request => Future.successful(Results.NoContent))

          whenReady(result) { result =>
            result mustBe Results.NoContent
          }
        }
      }

      "when the employer reference does not match the request's employer reference" - {
        "must redirect to unauthorised page" in {
          val request =
            DataRequest(FakeRequest(), "id", emptyUserAnswers, Some(otherEmployerReference), None)

          val action = AuthorizedForSchemeAction.canAccessScheme(employerReference)
          val result = action.invokeBlock(request, implicit request => Future.successful(Results.NoContent))

          whenReady(result) { result =>
            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe Some(
              controllers.routes.UnauthorisedController.onPageLoad().url
            )
          }
        }
      }

      "when there is no employer reference in the request" - {
        "must redirect to unauthorised page" in {
          val request = DataRequest(FakeRequest(), "id", emptyUserAnswers, None, None)

          val action = AuthorizedForSchemeAction.canAccessScheme(employerReference)
          val result = action.invokeBlock(request, implicit request => Future.successful(Results.NoContent))

          whenReady(result) { result =>
            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe Some(
              controllers.routes.UnauthorisedController.onPageLoad().url
            )
          }
        }
      }
    }

    "requireSchemeAccess" - {
      "must delegate to canAccessScheme with the correct employer reference" in {
        val request =
          DataRequest(FakeRequest(), "id", emptyUserAnswers, Some(employerReference), None)

        val action = AuthorizedForSchemeAction.requireSchemeAccess(taxOfficeNumber, taxOfficeReference)
        val result = action.invokeBlock(request, implicit request => Future.successful(Results.NoContent))

        whenReady(result) { result =>
          result mustBe Results.NoContent
        }
      }
    }
  }
}
