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

import config.FrontendAppConfig
import models.Target
import models.Target.*
import models.response.GetSubcontractor
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.DateTimeFormats
import viewmodels.SuccessfulAutomaticSubcontractorUpdateViewModel
import views.html.SuccessfulAutomaticSubcontractorUpdateView
import controllers.actions.{AuthorizedForSchemeActionProvider, DataRequiredAction, DataRetrievalAction, HasClientGuard, IdentifierAction}
import services.{PrepopService, SubcontractorService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SuccessfulAutomaticSubcontractorUpdateController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  hasClientGuard: HasClientGuard,
  val controllerComponents: MessagesControllerComponents,
  view: SuccessfulAutomaticSubcontractorUpdateView,
  requireSchemeAccess: AuthorizedForSchemeActionProvider,
  service: PrepopService,
  subcontractorService: SubcontractorService,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(instanceId: String, targetKey: String): Action[AnyContent] =
    (identify
      andThen getData
      andThen requireData
      andThen requireSchemeAccess(instanceId)
      andThen hasClientGuard.forInstanceId(instanceId)).async { implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      implicit val lang: Lang        = messagesApi.preferred(request).lang
      service.getScheme(instanceId).flatMap {
        case None                                                  =>
          Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
        case Some(scheme) if scheme.prePopSuccessful.contains("N") =>
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case _                                                     =>
          subcontractorService.getSubcontractorList(instanceId).map { response =>
            Ok(view(response.subcontractors.map(toViewModel), instanceId, targetKey))
          }
      }
    }

  def onSubmit(instanceId: String, targetKey: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSchemeAccess(instanceId)) { implicit request =>
      Target.fromKey(targetKey) match {
        case Some(target) => Redirect(targetCall(target, instanceId))
        case None         => NotFound("Unknown target")
      }
    }

  private def targetCall(target: Target, instanceId: String): Call =
    target match {
      case Returns                 => controllers.routes.ReturnsLandingController.onPageLoad(instanceId)
      case Notices                 => controllers.routes.JourneyRecoveryController.onPageLoad()
      case Subcontractor           => controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId)
      case ManageContractorDetails => Call("GET", appConfig.contractorDetailsManagementUrl)
    }

  private def toViewModel(
    subcontractor: GetSubcontractor
  )(implicit lang: Lang): SuccessfulAutomaticSubcontractorUpdateViewModel =
    SuccessfulAutomaticSubcontractorUpdateViewModel(
      name = subcontractor.displayName.getOrElse(""),
      uniqueReferenceNumber = subcontractor.utr.getOrElse(""),
      verificationNumber = subcontractor.verificationNumber.getOrElse(""),
      dateAdded = subcontractor.createDate
        .map(_.format(DateTimeFormats.shortDateFormat()))
        .getOrElse("")
    )
}
