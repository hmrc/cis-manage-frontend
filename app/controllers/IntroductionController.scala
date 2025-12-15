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

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import models.UserAnswers
import play.api.Logging
import services.ManageService

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IntroductionView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class IntroductionController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  sessionRepository: SessionRepository,
  view: IntroductionView,
  manageService: ManageService
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(view())
  }

  def affinityGroupRouting: Action[AnyContent] = identify.async { implicit request =>
    val userAnswers = UserAnswers(request.userId)
    sessionRepository.set(userAnswers).map { _ =>
      if (request.isAgent) {
        Redirect(controllers.agent.routes.RetrievingClientController.onPageLoad())
      } else {
        Redirect(controllers.contractor.routes.ContractorLandingController.onPageLoad())
      }
    }
  }

  def onContinue: Action[AnyContent] = identify.async { implicit request =>
    val maybeEmployerRef = request.employerReference
    val instanceId       = s"${request.userId}-${Random.alphanumeric.take(8).mkString}"

    maybeEmployerRef match {
      case Some(employerRef) =>
        manageService
          .prepopulateContractorAndSubcontractors(employerRef, instanceId)
          .map { prepopSuccessful =>
            if (prepopSuccessful) {
              Redirect(controllers.routes.SuccessfulAutomaticSubcontractorUpdateController.onPageLoad())
            } else {
              Redirect(controllers.routes.UnsuccessfulAutomaticSubcontractorUpdateController.onPageLoad())
            }
          }
          .recover { case exception =>
            logger.error(
              s"[IntroductionController][onContinue] Prepopulation contractor and subcontractors failed: ${exception.getMessage}",
              exception
            )
            Redirect(controllers.routes.UnsuccessfulAutomaticSubcontractorUpdateController.onPageLoad())
          }

      case None =>
        logger.error("[IntroductionController][onContinue] Employer reference missing in identifier request")
        Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
    }
  }

}
