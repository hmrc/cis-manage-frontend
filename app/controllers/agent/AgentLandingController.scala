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

import config.FrontendAppConfig
import controllers.actions.*
import models.Target.*
import models.audit.ClientDetailsRetrievedAuditEventModel
import models.requests.DataRequest
import models.{CisTaxpayerSearchResult, Target}
import navigation.ClientListCheckNavigator
import pages.{AgentClientsPage, CisIdPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import services.{AuditService, ManageService, PrepopService}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.agent.AgentLandingView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class AgentLandingController @Inject() (
  override val messagesApi: MessagesApi,
  @Named("AgentIdentifier") identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  clientListStatusGuard: ClientListStatusGuard,
  hasClientGuard: HasClientGuard,
  clientListCheckNavigator: ClientListCheckNavigator,
  manageService: ManageService,
  prepopService: PrepopService,
  auditService: AuditService,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: AgentLandingView
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(uniqueId: String): Action[AnyContent] =
    (identify
      andThen clientListStatusGuard.groupB(clientListCheckNavigator.agentDashboard(uniqueId))
      andThen getData
      andThen requireData
      andThen hasClientGuard.forInstanceId(uniqueId)).async { implicit request =>

      given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      AgentClientsPage.findClient(request.userAnswers, uniqueId) match {
        case Some(client) =>
          loadLandingPage(uniqueId, client)

        case None =>
          logger.error(s"[AgentLandingController] Client not found in userAnswers for uniqueId=$uniqueId")
          Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
      }
    }

  def onTargetClick(uniqueId: String, targetKey: String): Action[AnyContent] =
    (identify
      andThen getData
      andThen requireData
      andThen hasClientGuard.forInstanceId(uniqueId)).async { implicit request =>
      val systemErrorRedirect       = Redirect(controllers.routes.SystemErrorController.onPageLoad())
      val unauthorisedAgentRedirect = Redirect(controllers.routes.UnauthorisedAgentAffinityController.onPageLoad())

      resolveInputs(uniqueId, targetKey, systemErrorRedirect, unauthorisedAgentRedirect) match {
        case Left(result)            =>
          Future.successful(result)
        case Right((target, client)) =>
          handleTargetClick(uniqueId, targetKey, target, client, systemErrorRedirect)
      }
    }

  private def loadLandingPage(
    uniqueId: String,
    client: CisTaxpayerSearchResult
  )(using request: DataRequest[?], hc: HeaderCarrier): Future[Result] =
    (for {
      _                  <- auditClientDetailsRetrieved(client, uniqueId)
      updatedUserAnswers <- Future.fromTry(request.userAnswers.set(CisIdPage, uniqueId))
      _                  <- sessionRepository.set(updatedUserAnswers)
      viewModel          <- manageService.getAgentLandingData(uniqueId, updatedUserAnswers, request.userId)
    } yield Ok(
      view(
        uniqueId = uniqueId,
        agentName = request.itmpName,
        schemeName = viewModel.schemeName,
        employerRef = viewModel.employerRef
      )
    )).recover { case NonFatal(ex) =>
      logger.error(s"[AgentLandingController] unexpected error for uniqueId=$uniqueId", ex)
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }

  private def auditClientDetailsRetrieved(
    client: CisTaxpayerSearchResult,
    uniqueId: String
  )(using request: DataRequest[?], hc: HeaderCarrier): Future[Unit] =
    request.agentReference match {

      case Some(agentRef) =>
        val auditEvent = ClientDetailsRetrievedAuditEventModel(
          agentReference = agentRef,
          taxOfficeNumber = client.taxOfficeNumber,
          taxOfficeReference = client.taxOfficeRef
        )
        auditService
          .sendEvent(auditEvent)
          .map(_ => ())
          .recover { case NonFatal(ex) =>
            logger.error(
              s"[AgentLandingController] failed to send ClientDetailsRetrieved audit for uniqueId=$uniqueId",
              ex
            )
            ()
          }

      case None =>
        Future.failed(
          new IllegalStateException(
            s"[AgentLandingController] Missing agent reference in request for uniqueId=$uniqueId"
          )
        )
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
        AgentClientsPage.findClient(request.userAnswers, uniqueId) match {
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
      case Notices       => controllers.notices.routes.ManageNoticesStatementsController.onPageLoad(instanceId)
      case Subcontractor => controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId)
    }
}
