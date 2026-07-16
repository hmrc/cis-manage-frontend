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
import models.NormalMode
import pages.subcontractors.{DeleteSubcontractorJourneyPage, DeleteSubcontractorYesNoPage}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteSubcontractorController @Inject() (
  subcontractorService: SubcontractorService,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requireCisId: CisIdRequiredAction,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController {
  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId).async { implicit request =>
      (
        request.userAnswers.get(DeleteSubcontractorJourneyPage),
        request.userAnswers.get(DeleteSubcontractorYesNoPage)
      ) match {

        case (Some(journeyData), Some(true)) =>
          subcontractorService
            .deleteSubcontractor(
              request.cisId,
              journeyData.subbieResourceRef
            )
            .map { _ =>
              Redirect(
                controllers.subcontractors.routes.SubcontractorDeletedConfirmationController.onPageLoad()
              )
            }

        case (Some(_), Some(false)) =>
          Future.successful(
            Redirect(
              controllers.subcontractors.routes.SubcontractorsListController.onPageLoad(
                request.cisId,
                NormalMode
              )
            )
          )

        case _ =>
          Future.successful(
            Redirect(
              controllers.routes.JourneyRecoveryController.onPageLoad()
            )
          )
      }
    }
}
