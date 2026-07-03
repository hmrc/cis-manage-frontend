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
import models.requests.DataRequest
import models.subcontractors.DeleteSubcontractorJourneyData
import pages.CisIdPage
import pages.subcontractors.DeleteSubcontractorJourneyPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class GetSubcontractorForDeleteController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subcontractorService: SubcontractorService,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(
    subbieResourceRef: Long,
    displayName: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.get(CisIdPage) match {

        case Some(instanceId) =>
          subcontractorService
            .getSubcontractorDeleteStatus(instanceId, subbieResourceRef)
            .flatMap { response =>
              saveJourneyData(
                displayName = displayName,
                subbieResourceRef = subbieResourceRef,
                canBeDeleted = response.subcontractorCanBeDeleted
              ).map { _ =>
                redirectToNextPage(response.subcontractorCanBeDeleted)
              }
            }
            .recover { case ex =>
              logger.error(
                s"[GetSubcontractorForDeleteController] Failed to get delete status " +
                  s"(instanceId=$instanceId, subbieResourceRef=$subbieResourceRef)",
                ex
              )

              journeyRecoveryRedirect
            }

        case None =>
          logger.error(
            "[GetSubcontractorForDeleteController] Missing CisId in UserAnswers"
          )

          Future.successful(journeyRecoveryRedirect)
      }
    }

  private def saveJourneyData(
    displayName: String,
    subbieResourceRef: Long,
    canBeDeleted: Boolean
  )(implicit request: DataRequest[_]): Future[Unit] = {

    val journeyData =
      DeleteSubcontractorJourneyData(
        subcontractorName = displayName,
        subbieResourceRef = subbieResourceRef,
        subcontractorCanBeDeleted = canBeDeleted
      )

    request.userAnswers
      .set(DeleteSubcontractorJourneyPage, journeyData) match {

      case Success(updatedAnswers) =>
        sessionRepository
          .set(updatedAnswers)
          .map(_ => ())

      case Failure(ex) =>
        logger.error(
          "[GetSubcontractorForDeleteController] Failed to save delete journey data",
          ex
        )

        Future.failed(ex)
    }
  }

  private def redirectToNextPage(
    canDelete: Boolean
  ) =
    if (canDelete) {
      Redirect(routes.DeleteSubcontractorYesNoController.onPageLoad())
    } else {
      Redirect(routes.CannotDeleteSubcontractorController.onPageLoad())
    }

  private def journeyRecoveryRedirect =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
}
