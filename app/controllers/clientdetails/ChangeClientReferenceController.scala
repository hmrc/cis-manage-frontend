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
import forms.clientdetails.ChangeClientReferenceFormProvider
import models.Mode
import navigation.{ClientListCheckNavigator, Navigator}
import pages.clientdetails.ChangeClientReferencePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.ManageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.clientdetails.ChangeClientReferenceView
import play.api.Logging

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class ChangeClientReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  @Named("AgentIdentifier") identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  hasClientGuard: HasClientGuard,
  clientListStatusGuard: ClientListStatusGuard,
  clientListCheckNavigator: ClientListCheckNavigator,
  formProvider: ChangeClientReferenceFormProvider,
  manageService: ManageService,
  val controllerComponents: MessagesControllerComponents,
  view: ChangeClientReferenceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(uniqueId: String, mode: Mode): Action[AnyContent] =
    (identify
      andThen clientListStatusGuard.groupB(clientListCheckNavigator.changeClientReference(mode))
      andThen getData
      andThen requireData
      andThen hasClientGuard.forInstanceId(uniqueId)).async { implicit request =>
      val preparedForm = request.userAnswers.get(ChangeClientReferencePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Future(Ok(view(preparedForm, uniqueId, mode)))
    }

  def onSubmit(uniqueId: String, mode: Mode): Action[AnyContent] =
    (identify
      andThen clientListStatusGuard.groupB(clientListCheckNavigator.changeClientReference(mode))
      andThen getData
      andThen requireData
      andThen hasClientGuard.forInstanceId(uniqueId)).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, uniqueId, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ChangeClientReferencePage, value))
              _              <- manageService.updateClient(uniqueId, updatedAnswers, value)
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(controllers.clientdetails.routes.ClientRefUpdateConfirmationController.onPageLoad())
        )
        .recover { case ex =>
          logger.error(s"Failed to update client reference for uniqueId $uniqueId", ex)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }
}
