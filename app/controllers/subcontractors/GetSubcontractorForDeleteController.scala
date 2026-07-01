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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.CisIdPage
import pages.subcontractors.SubcontractorDeleteStatusPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GetSubcontractorForDeleteController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subcontractorService: SubcontractorService,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(
    subbieResourceRef: Long
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.get(CisIdPage) match {

        case Some(instanceId) =>
          subcontractorService
            .getSubcontractorDeleteStatus(instanceId, subbieResourceRef)
            .flatMap { response =>
              request.userAnswers
                .set(SubcontractorDeleteStatusPage, response) match {

                case scala.util.Success(updatedAnswers) =>
                  sessionRepository.set(updatedAnswers).map { _ =>
                    if (response.canBeDeleted) {
                      Redirect(routes.DeleteSubcontractorYesNoController.onPageLoad())
                    } else {
                      Redirect(routes.CannotDeleteSubcontractorController.onPageLoad())
                    }
                  }

                case scala.util.Failure(ex) =>
                  logger.error("[SubcontractorController] Failed to update UserAnswers", ex)
                  Future.successful(
                    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                  )
              }
            }
            .recover { case ex =>
              logger.error(
                s"[SubcontractorController][onPageLoad] failed (instanceId=$instanceId, subbieResourceRef=$subbieResourceRef)",
                ex
              )
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }

        case None =>
          logger.error("[SubcontractorController][onPageLoad] Missing CisId in UserAnswers")
          Future.successful(
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          )
      }
    }

}
