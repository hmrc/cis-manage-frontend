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
import pages.subcontractors.DeleteSubcontractorJourneyPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subcontractors.SubcontractorDeletedConfirmationView

import javax.inject.Inject

class SubcontractorDeletedConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireCisId: CisIdRequiredAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: SubcontractorDeletedConfirmationView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId) { implicit request =>
      request.userAnswers
        .get(DeleteSubcontractorJourneyPage)
        .fold {
          Redirect(
            controllers.routes.JourneyRecoveryController.onPageLoad()
          )
        } { journeyData =>

          val url =
            controllers.subcontractors.routes.SubcontractorsListController
              .onPageLoad(request.cisId, NormalMode)
              .url

          val surveyURL = "#"

          Ok(
            view(
              journeyData.subcontractorName,
              url,
              surveyURL
            )
          )
        }
    }
}
