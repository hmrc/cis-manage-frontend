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

package controllers.history

import controllers.actions.*
import controllers.routes
import models.*
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import queries.delete.UnsubmittedMonthlyReturnToDeleteQuery
import repositories.SessionRepository
import services.ManageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IncompleteReturnsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IncompleteReturnsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requireCisId: CisIdRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: IncompleteReturnsView,
  service: ManageService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData andThen requireCisId).async {
    implicit request =>
      service.getUnsubmittedMonthlyReturnRows(request.cisId).map { rows =>
        if (rows.isEmpty) {
          Redirect(controllers.amend.routes.NoIncompleteReturnsController.onPageLoad())
        } else {
          Ok(view(rows))
        }
      }
  }

  def onDeleteRedirect(monthlyReturnId: Long): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      service.checkUnsubmittedMonthlyReturnDeletion(request.userAnswers, monthlyReturnId).flatMap {
        case Deletable(record) =>
          for {
            updatedAnswers <- Future.fromTry(
                                request.userAnswers.set(UnsubmittedMonthlyReturnToDeleteQuery, record)
                              )
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(resolveDeleteRoute(record))
        case _                 => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  private def resolveDeleteRoute(record: UnsubmittedMonthlyReturnsRow): Call =
    (record.returnType, record.amendment) match {
      case ("Nil", Some("Y"))      => controllers.delete.routes.DeleteAmendedNilMonthlyReturnController.onPageLoad()
      case ("Nil", Some("N"))      => controllers.delete.routes.DeleteNilMonthlyReturnController.onPageLoad()
      case ("Standard", Some("Y")) => controllers.delete.routes.DeleteAmendedMonthlyReturnController.onPageLoad()
      case ("Standard", Some("N")) => controllers.delete.routes.DeleteMonthlyReturnController.onPageLoad()
      case _                       =>
        logger.warn(s"[ReturnsLandingController] No delete route mapping for monthlyReturnId=${record.monthlyReturnId}")
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }
}
