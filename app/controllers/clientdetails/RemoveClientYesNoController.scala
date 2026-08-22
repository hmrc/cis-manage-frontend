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
import pages.ClientRemovePage
import pages.clientdetails.RemoveClientYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AgentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.clientdetails.RemoveClientYesNoView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class RemoveClientYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  @Named("AgentIdentifier") identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RemoveClientYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  agentService: AgentService,
  view: RemoveClientYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form       = formProvider()
  val clientName = "clientName"

  def onPageLoad(employerRef: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      agentService
        .getClientsByEmployersReference(employerRef)
        .flatMap { response =>
          if (response.length == 1) {
            val preparedForm = request.userAnswers.get(RemoveClientYesNoPage) match {
              case None        => form
              case Some(value) => form.fill(value)
            }
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ClientRemovePage, response.head))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Ok(view(clientName, preparedForm, mode))

          } else {
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

          }
        }
        .recover { case e: Exception =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(clientName, formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveClientYesNoPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(RemoveClientYesNoPage, mode, updatedAnswers))
        )
    }
}
