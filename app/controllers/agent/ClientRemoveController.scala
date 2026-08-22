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

package controllers.agent

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.ClientRemovePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AgentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClientRemoveController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  agentService: AgentService,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging{

  def onPageLoad(empRef: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      agentService
        .getClientsByEmployersReference(empRef)
        .flatMap { response =>
          if (response.length == 1) {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ClientRemovePage, response.head))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          } else {
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            //Future.successful(Redirect(controllers.clientdetails.routes.RemoveClientYesNoController.onPageLoad(NormalMode)))
          }
        }.recover { case ex: Exception =>
          logger.error(
            "[ClientRemoveController] Failed to fetch clients by employer reference", ex)
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }
}
