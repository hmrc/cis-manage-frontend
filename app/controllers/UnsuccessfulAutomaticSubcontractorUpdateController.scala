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

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UnsuccessfulAutomaticSubcontractorUpdateView
import controllers.actions.{AuthorizedForSchemeActionProvider, DataRequiredAction, DataRetrievalAction, IdentifierAction}
import services.PrepopService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UnsuccessfulAutomaticSubcontractorUpdateController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: UnsuccessfulAutomaticSubcontractorUpdateView,
  requireSchemeAccess: AuthorizedForSchemeActionProvider,
  service: PrepopService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(instanceId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSchemeAccess(instanceId)).async { implicit request =>
      service.getScheme(instanceId).map {
        case None                                                  =>
          Redirect(routes.SystemErrorController.onPageLoad())
        case Some(scheme) if scheme.prePopSuccessful.contains("Y") =>
          Redirect(routes.JourneyRecoveryController.onPageLoad())
        case _                                                     =>
          Ok(view(instanceId))
      }
    }

  def onSubmit(instanceId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSchemeAccess(instanceId)) { implicit request =>
      Redirect(controllers.routes.AddContractorDetailsController.onPageLoad())
    }

}
