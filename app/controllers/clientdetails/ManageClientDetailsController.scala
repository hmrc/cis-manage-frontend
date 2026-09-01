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
import pages.clientdetails.ChangeClientReferencePage
import pages.{AgentClientsPage, CisIdPage}
import play.api.i18n.Lang.logger

import javax.inject.{Inject, Named}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.ManageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.clientdetails.ManageClientDetailsView

import scala.concurrent.{ExecutionContext, Future}

class ManageClientDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  @Named("AgentIdentifier") identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  manageService: ManageService,
  sessionRepository: SessionRepository,
  view: ManageClientDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(CisIdPage) match {
      case Some(instanceId) =>
        request.userAnswers.get(AgentClientsPage).flatMap(_.find(_.uniqueId == instanceId)) match {
          case Some(client) =>
            manageService
              .getClientByEmployerReference(client.taxOfficeNumber, client.taxOfficeRef)
              .flatMap { response =>
                val uniqueId: String          = response.uniqueId
                val clientName: String        = response.schemeName.getOrElse("")
                val employerReference: String = s"${response.taxOfficeNumber}/${response.taxOfficeRef}"
                val clientReference           = response.agentOwnRef.getOrElse("")
                for {
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.set(ChangeClientReferencePage, clientReference))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Ok(view(uniqueId, clientName, employerReference, clientReference))
              }
              .recover { case e =>
                logger.error(s"[ManageClientDetailsController][onPageLoad] Failed for uniqueId=$instanceId", e)
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }
          case None         =>
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      case _                =>
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  }
}
