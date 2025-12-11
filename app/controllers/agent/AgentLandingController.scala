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
import models.Target
import models.Target.*
import pages.AgentClientsPage
import play.api.Logging

import javax.inject.{Inject, Named}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
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
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      val targetOpt                 = Target.fromKey(targetKey)
      val clientOpt                 = request.userAnswers.get(AgentClientsPage).flatMap(_.find(_.uniqueId == uniqueId))
      val systemErrorRedirect       = Redirect(controllers.routes.SystemErrorController.onPageLoad())
      val unauthorisedAgentRedirect = Redirect(controllers.routes.UnauthorisedAgentAffinityController.onPageLoad())

      (targetOpt, clientOpt) match {
        case (Some(target), Some(client)) if client.uniqueId.nonEmpty =>
          val instanceId         = client.uniqueId
          val taxOfficeNumber    = client.taxOfficeNumber
          val taxOfficeReference = client.taxOfficeRef

          prepopService
            .prepopulateContractorKnownFacts(
              taxOfficeNumber = taxOfficeNumber,
              taxOfficeReference = taxOfficeReference,
              instanceId = instanceId
            )
            .map(_ => Redirect(targetCall(target, instanceId)))
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

        case (Some(_), Some(_)) =>
          logger.warn(
            s"[AgentLandingController][onTargetClick] Client found but uniqueId is missing/empty for requested uniqueId=$uniqueId"
          )
          Future.successful(unauthorisedAgentRedirect)

        case (Some(_), None) =>
          logger.warn(s"[AgentLandingController][onTargetClick] Missing client in userAnswers for uniqueId=$uniqueId")
          Future.successful(systemErrorRedirect)

        case (None, _) =>
          logger.warn(s"[AgentLandingController][onTargetClick] Unknown targetKey=$targetKey for uniqueId=$uniqueId")
          Future.successful(NotFound("Unknown target"))
      }
    }

  private def targetCall(target: Target, instanceId: String): Call =
    target match {
      case Returns       => controllers.routes.ReturnsLandingController.onPageLoad(instanceId)
      // to be added for NoticesLandingController
      case Notices       => controllers.routes.JourneyRecoveryController.onPageLoad()
      case Subcontractor => controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId)
    }
}
