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

package controllers.contractor

import ContractorLandingController.fromUserAnswers
import models.{EmployerReference, Target, UserAnswers}
import pages.*
import config.FrontendAppConfig
import controllers.actions.*
import models.Target.*
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.{ManageService, PrepopService}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.contractor.ContractorLandingViewModel
import views.html.contractor.ContractorLandingView

import javax.inject.{Inject, Named}
import scala.util.control.NonFatal
import scala.concurrent.{ExecutionContext, Future}

class ContractorLandingController @Inject() (
  override val messagesApi: MessagesApi,
  @Named("ContractorIdentifier") identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view: ContractorLandingView,
  appConfig: FrontendAppConfig,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  manageService: ManageService,
  prepopService: PrepopService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    manageService
      .resolveAndStoreCisId(request.userAnswers)
      .map { case (_, updatedUa) =>
        val viewModel = fromUserAnswers(updatedUa, appConfig)
        Ok(view(viewModel))
      }
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case exception                                             =>
          logger.error(
            s"[ContractorLandingController] Failed to retrieve cisId: ${exception.getMessage}",
            exception
          )
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
      }

  }

  def onTargetClick(targetKey: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      implicit val hc: HeaderCarrier                  = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      val targetOpt: Option[Target]                   = Target.fromKey(targetKey)
      val maybeInstanceId: Option[String]             = request.userAnswers.get(CisIdPage)
      val maybeEmployerRef: Option[EmployerReference] = request.employerReference

      val systemErrorRedirect     = Redirect(controllers.routes.SystemErrorController.onPageLoad())
      val unauthorizedOrgRedirect = Redirect(controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad())

      (targetOpt, maybeInstanceId, maybeEmployerRef) match {

        case (Some(target), Some(instanceId), Some(employerRef)) =>
          prepopService
            .prepopulateContractorKnownFacts(
              instanceId = instanceId,
              taxOfficeNumber = employerRef.taxOfficeNumber,
              taxOfficeReference = employerRef.taxOfficeReference
            )
            .map { _ =>
              Redirect(targetCall(target, instanceId))
            }
            .recover {
              case u: UpstreamErrorResponse =>
                logger.error(s"[ContractorLandingController][onTargetClick] upstream error: ${u.message}", u)
                systemErrorRedirect

              case NonFatal(e) =>
                logger.error("[ContractorLandingController][onTargetClick] unexpected error", e)
                systemErrorRedirect
            }

        case (Some(_), None, _) =>
          logger.warn("[ContractorLandingController][onTargetClick] Missing CisIdPage (instanceId) in userAnswers")
          Future.successful(unauthorizedOrgRedirect)

        case (Some(_), _, None) =>
          logger.warn("[ContractorLandingController][onTargetClick] Missing employerReference on DataRequest")
          Future.successful(systemErrorRedirect)

        case (None, _, _) =>
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

object ContractorLandingController {

  def fromUserAnswers(ua: UserAnswers, appConfig: FrontendAppConfig): ContractorLandingViewModel =
    ContractorLandingViewModel(
      contractorName = ua.get(ContractorNamePage).getOrElse(""),
      employerReference = ua.get(EmployerReferencePage).getOrElse(""),
      utr = ua.get(UniqueTaxReferencePage).getOrElse(""),
      // still hard-coded for now
      returnCount = 1,
      returnDueDate = "19 October 2025",
      noticeCount = 2,
      lastSubmittedDate = "19 September 2025",
      lastSubmittedTaxMonthYear = "August 2025",
      whatIsUrl = appConfig.contractorLandingWhatIsUrl,
      guidanceUrl = appConfig.contractorLandingGuidanceUrl,
      penaltiesUrl = appConfig.contractorLandingPenaltiesUrl
    )
}
