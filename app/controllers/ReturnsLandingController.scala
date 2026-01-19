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
import controllers.actions.*
import pages.{AgentClientsPage, ContractorNamePage}
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.ReturnLandingViewModel
import views.html.ReturnsLandingView

class ReturnsLandingController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ReturnsLandingView
)(implicit appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(instanceId: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val returnsList = Seq(
        ReturnLandingViewModel("August 2025", "Standard", "19 September 2025", "Accepted"),
        ReturnLandingViewModel("July 2025", "Nil", "19 August 2025", "Accepted"),
        ReturnLandingViewModel("June 2025", "Standard", "18 July 2025", "Accepted")
      )

      if (request.isAgent) {
        val clientOpt =
          for {
            clients <- request.userAnswers.get(AgentClientsPage)
            client  <- clients.find(_.uniqueId == instanceId)
          } yield client

        clientOpt match {
          case Some(client) =>
            client.schemeName match {
              case Some(contractorName) =>
                val standardReturnLink =
                  appConfig.fileStandardReturnUrl(client.taxOfficeNumber, client.taxOfficeRef, instanceId)
                val nilReturnLink      =
                  appConfig.fileNilReturnUrl(client.taxOfficeNumber, client.taxOfficeRef, instanceId)
                Ok(view(contractorName, returnsList, standardReturnLink, nilReturnLink))
              case None                 =>
                logger.warn(
                  s"[ReturnsLandingController][onPageLoad] - Agent client scheme name missing for instanceId=$instanceId"
                )
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
          case None         =>
            logger.warn(s"[ReturnsLandingController][onPageLoad] - Agent client missing for instanceId=$instanceId")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      } else {
        request.userAnswers.get(ContractorNamePage) match {
          case Some(contractorName) =>
            val standardReturnLink = appConfig.fileStandardReturnUrl
            val nilReturnLink      = appConfig.fileNilReturnUrl

            Ok(view(contractorName, returnsList, standardReturnLink, nilReturnLink))
          case None                 =>
            logger.warn(s"[ReturnsLandingController] - Contractor name missing (isAgent=false)")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
  }
}
