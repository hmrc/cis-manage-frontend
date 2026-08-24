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

package controllers.clientdetails

import controllers.actions.*
import pages.{AgentClientsPage, CisIdPage}

import javax.inject.{Inject, Named}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AgentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.clientdetails.ManageClientDetailsView

import scala.concurrent.{ExecutionContext, Future}

class ManageClientDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  @Named("AgentIdentifier") identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  agentService: AgentService,
  view: ManageClientDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(CisIdPage) match {
      case Some(instanceId) =>
        request.userAnswers.get(AgentClientsPage).flatMap(_.find(_.uniqueId == instanceId)) match {
          case Some(client) =>
            val employerRef = s"${client.taxOfficeNumber}/${client.taxOfficeRef}"
            agentService
              .getClientsByEmployersReference(employerRef)
              .flatMap { response =>
                if (response.length == 1) {
                  val uniqueId: String          = response.head.uniqueId
                  val clientName: String        = response.head.schemeName.toString
                  val employerReference: String = employerRef
                  val clientReference: String   = response.head.agentOwnRef.toString
                  Future.successful(Ok(view(uniqueId, clientName, employerReference, clientReference)))

                } else {
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                }
              }
          case None         =>
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      case _                =>
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  }
}
