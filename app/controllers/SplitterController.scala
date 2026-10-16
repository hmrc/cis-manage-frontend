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

package controllers

import config.FrontendAppConfig
import connectors.RateLimitedAllowListConnector

import javax.inject.Singleton
import controllers.actions.IdentifierAction
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SplitterController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  config: FrontendAppConfig,
  rateLimitedAllowListConnector: RateLimitedAllowListConnector,
  identify: IdentifierAction
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def checkAllowList[A](request: IdentifierRequest[A])(using HeaderCarrier): Future[Boolean] =
    if (config.useRateLimitedAllowList) {
      if (request.isAgent) {
        val identifier = request.agentReference
        rateLimitedAllowListConnector.checkAllowList(
          config.splitterServiceName,
          config.splitterAllowListName,
          identifier.get
        )
      } else {
        val identifier =
          s"${request.employerReference.get.taxOfficeNumber}/${request.employerReference.get.taxOfficeReference}"
        rateLimitedAllowListConnector.checkAllowList(
          config.splitterServiceName,
          config.splitterAllowListName,
          identifier
        )
      }
    } else Future.successful(false)

  def coreLandingPage(path: String): Action[AnyContent] = identify.async { implicit request =>
    checkAllowList(request).map {
      case true  => Redirect(controllers.routes.IntroductionController.onPageLoad())
      case false =>
        val url = s"${config.legacyCisServiceUrl}/$path"
        logger.info(s"Redirecting to cis legacy service to $url")
        Redirect(url)
    }
  }

  def fileLandingPage(path: String): Action[AnyContent] = identify { implicit request =>
    checkAllowList(request).map {
      case true  => Redirect(controllers.routes.IntroductionController.onPageLoad())
      case false =>
        val url = s"${config.legacyCisServiceLandingUrl}/$path"
        logger.info(s"Redirecting to cis legacy service to $url")
        Redirect(url)
    }
  }

}
