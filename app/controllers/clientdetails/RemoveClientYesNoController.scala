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
import navigation.{ClientListCheckNavigator, Navigator}
import pages.clientdetails.RemoveClientYesNoPage
import pages.{AgentClientsPage, ClientListSearchPage}
import play.api.Logging
import play.api.data.Form
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
  navigator: Navigator,
  @Named("AgentIdentifier") identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  hasClientGuard: HasClientGuard,
  clientListStatusGuard: ClientListStatusGuard,
  clientListCheckNavigator: ClientListCheckNavigator,
  formProvider: RemoveClientYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  manageService: ManageService,
  view: RemoveClientYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(uniqueId: String, mode: Mode): Action[AnyContent] =
    (identify
      andThen clientListStatusGuard.groupB(clientListCheckNavigator.removeClient(mode))
      andThen getData
      andThen requireData
      andThen hasClientGuard.forInstanceId(uniqueId)).async { implicit request =>
      request.userAnswers.get(AgentClientsPage).flatMap(_.find(_.uniqueId == uniqueId)) match {
        case Some(client) =>
          manageService
            .getClientByEmployerReference(client.taxOfficeNumber, client.taxOfficeRef)
            .map { response =>
              val clientName   = response.schemeName.getOrElse("")
              val preparedForm = request.userAnswers.get(RemoveClientYesNoPage) match {
                case None        => form
                case Some(value) => form.fill(value)
              }

              Ok(view(clientName, preparedForm, mode, uniqueId))
            }
            .recover { case e =>
              logger.error(
                s"[RemoveClientYesNoController][onPageLoad] Failed for uniqueId=$uniqueId",
                e
              )
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }

        case None =>
          Future.successful(
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          )
      }
    }

  def onSubmit(uniqueId: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.get(AgentClientsPage).flatMap(_.find(_.uniqueId == uniqueId)) match {
        case Some(client) =>
          manageService
            .getClientByEmployerReference(client.taxOfficeNumber, client.taxOfficeRef)
            .flatMap { response =>
              val clientName = response.schemeName.getOrElse("")
              form
                .bindFromRequest()
                .fold(
                  formWithErrors =>
                    Future.successful(
                      BadRequest(view(clientName, formWithErrors, mode, uniqueId))
                    ),
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
            .recover { case e =>
              logger.error(
                s"[RemoveClientYesNoController][onSubmit] Failed for uniqueId=$uniqueId",
                e
              )
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }

        case None =>
          Future.successful(
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          )
      }
    }
}
