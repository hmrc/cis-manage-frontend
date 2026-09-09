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

import models.agent.ClientListCheckPolicy
import models.agent.ClientListCheckPolicy.*
import play.api.Logging
import play.api.mvc.RequestHeader
import play.api.routing.Router

import javax.inject.{Inject, Singleton}

@Singleton
class ClientListCheckPolicyResolver @Inject() extends Logging {

  private type RouteKey = (String, String)

  private val groupARoutes: Set[RouteKey] =
    Set(
      "controllers.RetrievingSubcontractorsController"   -> "start",
      "controllers.history.SubmittedReturnsController"   -> "onPageLoadSingleYear",
      "controllers.history.SubmittedReturnsController"   -> "onPageLoadAllYears",
      "controllers.history.SubmittedReturnsController"   -> "viewSubmissionReceipt",
      "controllers.history.SubmittedReturnsController"   -> "startAmendment",
      "controllers.history.SubmittedReturnsController"   -> "onInProgressRedirect",
      "controllers.verify.VerificationHistoryController" -> "onPageLoadSingleYear",
      "controllers.verify.VerificationHistoryController" -> "onPageLoadAllYears"
    )

  private val groupBRoutes: Set[RouteKey] =
    Set(
      "controllers.agent.AgentLandingController"                  -> "onPageLoad",
      "controllers.agent.ClientListSearchController"              -> "onPageLoad",
      "controllers.clientdetails.ManageClientDetailsController"   -> "onPageLoad",
      "controllers.clientdetails.ChangeClientReferenceController" -> "onPageLoad",
      "controllers.clientdetails.RemoveClientYesNoController"     -> "onPageLoad"
    )

  private val centralHasClientExemptRoutes: Set[RouteKey] =
    Set(
      // hasClientGuard intentionally skipped after the client has been removed
      "controllers.clientdetails.ClientRemovedController" -> "onPageLoad",

      // route-specific hasClientGuard via forInstanceId(instanceId), avoiding duplicate hasClient checks
      "controllers.ReturnsLandingController"                           -> "onPageLoad",
      "controllers.SubcontractorsLandingPageController"                -> "onPageLoad",
      "controllers.CheckSubcontractorRecordsController"                -> "onPageLoad",
      "controllers.RetrievingSubcontractorsController"                 -> "onPageLoad",
      "controllers.RetrievingSubcontractorsController"                 -> "start",
      "controllers.SuccessfulAutomaticSubcontractorUpdateController"   -> "onPageLoad",
      "controllers.SuccessfulNoRecordsFoundController"                 -> "onPageLoad",
      "controllers.UnsuccessfulAutomaticSubcontractorUpdateController" -> "onPageLoad",
      "controllers.subcontractors.SubcontractorsListController"        -> "onPageLoad",
      "controllers.notices.ManageNoticesStatementsController"          -> "onPageLoad"
    )

  private val exemptControllers: Set[String] =
    Set(
      "controllers.agent.RetrievingClientController",
      "controllers.agent.FailedToRetrieveClientController",
      "controllers.agent.AgentLostAccessController",
      "controllers.SecurityCheckController",
      "controllers.SystemErrorController",
      "controllers.JourneyRecoveryController",
      "controllers.AccessDeniedController",
      "controllers.UnauthorisedAgentAffinityController",
      "controllers.UnauthorisedController",
      "controllers.UnauthorisedIndividualAffinityController",
      "controllers.UnauthorisedOrganisationAffinityController",
      "controllers.UnauthorisedWrongRoleController"
    )

  def resolve(request: RequestHeader): ClientListCheckPolicy = {

    val route = routeKey(request)

    val policy =
      if request.method != "GET" then Exempt
      else
        route match {

          case Some((controller, _)) if exemptControllers.contains(controller) =>
            Exempt

          case Some(route) if groupBRoutes.contains(route) =>
            GroupB

          case Some(route) if groupARoutes.contains(route) =>
            GroupA

          case Some((_, "onPageLoad")) =>
            GroupA

          case _ =>
            Exempt
        }

    val handler = request.attrs
      .get(Router.Attrs.HandlerDef)
      .map(h => s"${h.controller}.${h.method}")
      .getOrElse("UnknownHandler")

    logger.info(
      s"[ClientListCheckPolicyResolver] method=${request.method} uri=${request.uri} handler=$handler policy=$policy "
    )

    policy
  }

  def shouldRunCentralHasClient(request: RequestHeader): Boolean =
    routeKey(request).exists { route =>
      !centralHasClientExemptRoutes.contains(route)
    }

  private def routeKey(request: RequestHeader): Option[RouteKey] =
    request.attrs
      .get(Router.Attrs.HandlerDef)
      .map { handler =>
        handler.controller -> handler.method
      }
}
