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

package controllers.amend

import controllers.actions.*
import forms.amend.ConfirmCancelAmendmentFormProvider
import models.Mode
import navigation.Navigator
import pages.amend.ConfirmCancelAmendmentPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.amend.ConfirmCancelAmendmentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmCancelAmendmentController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ConfirmCancelAmendmentFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmCancelAmendmentView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    getPeriodEnd match {
      case Some(monthYear) =>
        val preparedForm = request.userAnswers.get(ConfirmCancelAmendmentPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, monthYear, mode))
      case None            =>
        logger.error("[ConfirmCancelAmendmentController] monthYear missing")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getPeriodEnd match {
        case Some(monthYear) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, monthYear, mode))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(ConfirmCancelAmendmentPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(ConfirmCancelAmendmentPage, mode, updatedAnswers))
            )
        case None            =>
          logger.error("[ConfirmCancelAmendmentController] monthYear missing")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }

  private def getPeriodEnd: Option[String] =
    Some("April 2026")
}
