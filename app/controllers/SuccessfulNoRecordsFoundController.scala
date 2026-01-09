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

import models.Target
import models.Target.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SuccessfulNoRecordsFoundView
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import javax.inject.Inject

class SuccessfulNoRecordsFoundController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: SuccessfulNoRecordsFoundView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(instanceId: String, targetKey: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      Ok(view(instanceId, targetKey))
    }

  def onSubmit(instanceId: String, targetKey: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      Target.fromKey(targetKey) match {
        case Some(target) => Redirect(targetCall(target, instanceId))
        case None         => NotFound("Unknown target")
      }
    }

  private def targetCall(target: Target, instanceId: String): Call =
    target match {
      case Returns       => controllers.routes.ReturnsLandingController.onPageLoad(instanceId)
      case Notices       => controllers.routes.JourneyRecoveryController.onPageLoad()
      case Subcontractor => controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId)
    }
}
