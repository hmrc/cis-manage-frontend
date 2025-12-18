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

package controllers.agent

import controllers.actions.*
import config.FrontendAppConfig
import models.{CisTaxpayerSearchResult, Target}
import models.Target.*
import models.requests.DataRequest
import pages.AgentClientsPage
import play.api.Logging

import javax.inject.{Inject, Named}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import services.{ManageService, PrepopService}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.agent.AgentLandingView

import scala.util.control.NonFatal
import scala.concurrent.{ExecutionContext, Future}

class AgentLandingController @Inject() (
  override val messagesApi: MessagesApi,
  @Named("AgentIdentifier") identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  manageService: ManageService,
  prepopService: PrepopService,
  val controllerComponents: MessagesControllerComponents,
  view: AgentLandingView,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(uniqueId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      implicit val config: FrontendAppConfig = appConfig
      implicit val hc: HeaderCarrier         = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      manageService
        .getAgentLandingData(uniqueId, request.userAnswers)
        .map { viewModel =>
          Ok(
            view(
              uniqueId = uniqueId,
              clientName = viewModel.clientName,
              employerRef = viewModel.employerRef,
              utr = viewModel.utr.getOrElse(""),
              // still hard-coded, mocked for now
              returnsDueCount = 1,
              returnsDueBy = java.time.LocalDate.of(2025, 10, 19),
              newNoticesCount = 2,
              lastSubmittedDate = java.time.LocalDate.of(2025, 9, 19),
              lastSubmittedTaxMonth = java.time.YearMonth.of(2025, 8)
            )
          )
        }
        .recover { case e =>
          logger.error(s"[AgentLandingController][onPageLoad] Failed for uniqueId=$uniqueId", e)
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onTargetClick(uniqueId: String, targetKey: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      implicit val hc: HeaderCarrier =
        HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      val systemErrorRedirect       = Redirect(controllers.routes.SystemErrorController.onPageLoad())
      val unauthorisedAgentRedirect = Redirect(controllers.routes.UnauthorisedAgentAffinityController.onPageLoad())

      resolveInputs(uniqueId, targetKey, systemErrorRedirect, unauthorisedAgentRedirect) match {
        case Left(result)            =>
          Future.successful(result)
        case Right((target, client)) =>
          handleTargetClick(uniqueId, targetKey, target, client, systemErrorRedirect)
      }
    }

  private def resolveInputs(
    uniqueId: String,
    targetKey: String,
    systemErrorRedirect: Result,
    unauthorisedAgentRedirect: Result
  )(implicit request: DataRequest[_]): Either[Result, (Target, CisTaxpayerSearchResult)] =
    Target.fromKey(targetKey) match {
      case None =>
        logger.warn(s"[AgentLandingController][onTargetClick] Unknown targetKey=$targetKey for uniqueId=$uniqueId")
        Left(NotFound("Unknown target"))

      case Some(target) =>
        request.userAnswers.get(AgentClientsPage).flatMap(_.find(_.uniqueId == uniqueId)) match {
          case None =>
            logger.warn(s"[AgentLandingController][onTargetClick] Missing client in userAnswers for uniqueId=$uniqueId")
            Left(systemErrorRedirect)

          case Some(client) if client.uniqueId.trim.isEmpty =>
            logger.warn(
              s"[AgentLandingController][onTargetClick] Client found but uniqueId is missing/empty for requested uniqueId=$uniqueId"
            )
            Left(unauthorisedAgentRedirect)

          case Some(client) =>
            Right((target, client))
        }
    }

  private def handleTargetClick(
    uniqueId: String,
    targetKey: String,
    target: Target,
    client: CisTaxpayerSearchResult,
    systemErrorRedirect: Result
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val instanceId                    = client.uniqueId
    val addContractorDetailsCall      = controllers.routes.AddContractorDetailsController.onPageLoad()
    val checkSubcontractorRecordsCall = controllers.routes.CheckSubcontractorRecordsController.onPageLoad(
      client.taxOfficeNumber,
      client.taxOfficeRef,
      instanceId,
      targetKey
    )

    prepopService
      .prepopulateContractorKnownFacts(instanceId, client.taxOfficeNumber, client.taxOfficeRef)
      .flatMap(_ => prepopService.getScheme(instanceId))
      .map {
        case Some(scheme) =>
          Redirect(
            prepopService.determineLandingDestination(
              targetCall = targetCall(target, instanceId),
              instanceId = instanceId,
              scheme = scheme,
              addContractorDetailsCall = addContractorDetailsCall,
              checkSubcontractorRecordsCall = checkSubcontractorRecordsCall
            )
          )

        case None =>
          logger.warn(
            s"[AgentLandingController][onTargetClick] No scheme found for instanceId=$instanceId (uniqueId=$uniqueId)"
          )
          systemErrorRedirect
      }
      .recover {
        case u: UpstreamErrorResponse =>
          logger.error(
            s"[AgentLandingController][onTargetClick] upstream error for uniqueId=$uniqueId: ${u.message}",
            u
          )
          systemErrorRedirect

        case NonFatal(e) =>
          logger.error(s"[AgentLandingController][onTargetClick] unexpected error for uniqueId=$uniqueId", e)
          systemErrorRedirect
      }
  }

  private def targetCall(target: Target, instanceId: String): Call =
    target match {
      case Returns       => controllers.routes.ReturnsLandingController.onPageLoad(instanceId)
      // to be added for NoticesLandingController
      case Notices       => controllers.notices.routes.ManageNoticesStatementsController.onPageLoad(instanceId)
      case Subcontractor => controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId)
    }
}
