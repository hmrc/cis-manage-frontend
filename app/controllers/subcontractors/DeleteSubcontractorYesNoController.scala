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

package controllers.subcontractors

import controllers.actions.*
import forms.subcontractors.DeleteSubcontractorYesNoFormProvider
import models.Mode
import pages.subcontractors.{DeleteSubcontractorJourneyPage, DeleteSubcontractorYesNoPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subcontractors.DeleteSubcontractorYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteSubcontractorYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requireCisId: CisIdRequiredAction,
  formProvider: DeleteSubcontractorYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeleteSubcontractorYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers
        .get(DeleteSubcontractorJourneyPage)
        .fold {
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        } { journeyData =>
          if (!journeyData.subcontractorCanBeDeleted) {
            Redirect(
              controllers.subcontractors.routes.CannotDeleteSubcontractorController.onPageLoad()
            )
          } else {
            val preparedForm =
              request.userAnswers
                .get(DeleteSubcontractorYesNoPage)
                .fold(form)(form.fill)

            Ok(
              view(
                journeyData.subcontractorName,
                preparedForm,
                mode
              )
            )
          }
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            request.userAnswers
              .get(DeleteSubcontractorJourneyPage)
              .fold(
                Future.successful(
                  Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                )
              ) { journeyData =>
                Future.successful(
                  BadRequest(
                    view(
                      journeyData.subcontractorName,
                      formWithErrors,
                      mode
                    )
                  )
                )
              },
          value =>
            for {
              updatedAnswers <- Future.fromTry(
                                  request.userAnswers.set(
                                    DeleteSubcontractorYesNoPage,
                                    value
                                  )
                                )
              _              <- sessionRepository.set(updatedAnswers)
            } yield
              if (value) {
                Redirect(
                  controllers.subcontractors.routes.DeleteSubcontractorController.onSubmit()
                )
              } else {
                Redirect(
                  controllers.subcontractors.routes.SubcontractorsListController.onPageLoad(request.cisId, mode)
                )
              }
        )
    }
}
