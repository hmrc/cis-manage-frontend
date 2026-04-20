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

package controllers.delete

import controllers.actions.*
import forms.delete.DeleteAmendedMonthlyReturnFormProvider
import models.{Deletable, Mode}
import pages.CisIdPage
import pages.delete.DeleteAmendedMonthlyReturnPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.delete.UnsubmittedMonthlyReturnToDeleteQuery
import repositories.SessionRepository
import services.ManageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.delete.DeleteAmendedMonthlyReturnView

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject

class DeleteAmendedMonthlyReturnController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  deletableReturnAction: DeletableReturnAction,
  service: ManageService,
  formProvider: DeleteAmendedMonthlyReturnFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeleteAmendedMonthlyReturnView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen deletableReturnAction) { implicit request =>

      val preparedForm = request.userAnswers.get(DeleteAmendedMonthlyReturnPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      val langCode  = messagesApi.preferred(request).lang.code
      val monthYear = request.returnToDelete.monthYear(langCode)

      Ok(view(preparedForm, monthYear, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen deletableReturnAction).async { implicit request =>
      val instanceId  = request.userAnswers.get(CisIdPage).getOrElse {
        logger.error("[DeleteAmendedMonthlyReturnController] cisId missing from userAnswers")
        throw new IllegalStateException("cisId missing from userAnswers")
      }
      val userAnswers = request.userAnswers
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val langCode  = messagesApi.preferred(request).lang.code
            val monthYear = request.returnToDelete.monthYear(langCode)
            Future.successful(BadRequest(view(formWithErrors, monthYear, mode)))
          },
          value =>
            if (value) {
              val result =
                for {
                  deletionStatus <- service.checkUnsubmittedMonthlyReturnDeletion(
                                      request.userAnswers,
                                      request.returnToDelete.monthlyReturnId
                                    )
                  _              <- deletionStatus match {
                                      case Deletable(record) =>
                                        service.deleteUnsubmittedMonthlyReturn(userAnswers, record)
                                      case _                 =>
                                        Future.failed(new IllegalStateException("Record is not deletable"))
                                    }
                  updatedAnswers <- Future.fromTry(userAnswers.remove(UnsubmittedMonthlyReturnToDeleteQuery))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  controllers.routes.ReturnsLandingController.onPageLoad(instanceId)
                )

              result.recover { case ex =>
                logger.error(
                  s"[DeleteAmendedMonthlyReturnController] Failed to delete returnId=${request.returnToDelete.monthlyReturnId}: ${ex.getMessage}",
                  ex
                )
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }
            } else {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.remove(UnsubmittedMonthlyReturnToDeleteQuery))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                controllers.routes.ReturnsLandingController.onPageLoad(instanceId)
              )
            }
        )
    }
}
