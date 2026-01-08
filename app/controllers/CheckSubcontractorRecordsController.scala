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

import controllers.actions.{AuthorizedForSchemeActionProvider, DataRequiredAction, DataRetrievalAction, IdentifierAction}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.PrepopService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CheckSubcontractorRecordsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckSubcontractorRecordsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireSchemeAccess: AuthorizedForSchemeActionProvider,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckSubcontractorRecordsView,
  service: PrepopService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(
    taxOfficeNumber: String,
    taxOfficeReference: String,
    instanceId: String,
    targetKey: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSchemeAccess(taxOfficeNumber, taxOfficeReference))
      .async { implicit request =>
        service.getScheme(instanceId).map {
          case None                                                          =>
            Redirect(routes.SystemErrorController.onPageLoad())
          case Some(scheme) if scheme.prePopSuccessful.exists(_.equals("Y")) =>
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          case _                                                             =>
            Ok(view(taxOfficeNumber, taxOfficeReference, instanceId, targetKey))
        }
      }

  def onSubmit(
    taxOfficeNumber: String,
    taxOfficeReference: String,
    instanceId: String,
    targetKey: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      Redirect(
        controllers.routes.RetrievingSubcontractorsController
          .onPageLoad(taxOfficeNumber, taxOfficeReference, instanceId, targetKey)
      )
    }
}
