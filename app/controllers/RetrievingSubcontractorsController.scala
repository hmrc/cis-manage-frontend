/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import controllers.actions.IdentifierAction
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ManageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RetrievingSubcontractorsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrievingSubcontractorsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view: RetrievingSubcontractorsView,
  manageService: ManageService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(instanceId: String): Action[AnyContent] = identify.async { implicit request =>
    import uk.gov.hmrc.http.HeaderCarrier
    import uk.gov.hmrc.play.http.HeaderCarrierConverter

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    request.employerReference match {
      case Some(employerRef) =>
        manageService
          .prepopulateContractorAndSubcontractors(employerRef, instanceId)
          .flatMap { prepopSuccessful =>
            if (prepopSuccessful) {
              manageService
                .getScheme(instanceId)
                .map { case (schemePrepopSuccessful, subcontractorCounter) =>
                  if (schemePrepopSuccessful && subcontractorCounter > 0) {
                    Redirect(controllers.routes.SuccessfulAutomaticSubcontractorUpdateController.onPageLoad())
                  } else if (schemePrepopSuccessful && subcontractorCounter == 0) {
                    Redirect(controllers.routes.SuccessfulNoRecordsFoundController.onPageLoad())
                  } else {
                    Redirect(controllers.routes.UnsuccessfulAutomaticSubcontractorUpdateController.onPageLoad())
                  }
                }
            } else {
              Future.successful(
                Redirect(controllers.routes.UnsuccessfulAutomaticSubcontractorUpdateController.onPageLoad())
              )
            }
          }
          .recover { case exception =>
            Redirect(controllers.routes.UnsuccessfulAutomaticSubcontractorUpdateController.onPageLoad())
          }

      case None =>
        Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
    }
  }
}
