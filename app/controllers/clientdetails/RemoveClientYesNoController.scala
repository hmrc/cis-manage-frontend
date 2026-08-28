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
import forms.clientdetails.RemoveClientYesNoFormProvider
import models.Mode
import navigation.Navigator
import pages.ClientListSearchPage
import pages.clientdetails.RemoveClientYesNoPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.ManageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.clientdetails.RemoveClientYesNoView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class RemoveClientYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  manageService: ManageService,
  navigator: Navigator,
  @Named("AgentIdentifier") identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RemoveClientYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveClientYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form       = formProvider()
  val clientName = "clientName"

  def onPageLoad(uniqueId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(RemoveClientYesNoPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(clientName, preparedForm, mode, uniqueId))
  }

  def onSubmit(uniqueId: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(clientName, formWithErrors, mode, uniqueId))),
          value => {
            val result =
              for {
                updatedAnswers            <- Future.fromTry(request.userAnswers.set(RemoveClientYesNoPage, value))
                updatedAnswersWithClients <-
                  if (value) {
                    for {
                      _                    <- manageService.removeClient(uniqueId, updatedAnswers)
                      uaWithClients        <- Future.fromTry(updatedAnswers.remove(ClientListSearchPage))
                      (_, resolvedAnswers) <- manageService.resolveAndStoreAgentClients(uaWithClients)
                    } yield resolvedAnswers
                  } else {
                    Future.successful(updatedAnswers)
                  }
                _                         <- sessionRepository.set(updatedAnswersWithClients)
              } yield Redirect(
                navigator.nextPage(RemoveClientYesNoPage, mode, updatedAnswersWithClients)
              )

            result.recover { case ex =>
              logger.error(
                s"[RemoveClientYesNoController][onSubmit] Failed to process remove client: ${ex.getMessage}",
                ex
              )
              Redirect(controllers.routes.SystemErrorController.onPageLoad())
            }
          }
        )
    }
}
