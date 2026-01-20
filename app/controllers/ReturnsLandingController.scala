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
import play.api.Logging
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ManageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.ReturnsLandingView

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class ReturnsLandingController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ReturnsLandingView,
  service: ManageService
)(implicit appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(instanceId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      service
        .buildReturnsLandingContext(instanceId, request.userAnswers, request.isAgent)
        .map {
          case Some(context) =>
            Ok(view(context.contractorName, context.returnsList, context.standardReturnLink, context.nilReturnLink))
          case None          =>
            logger.warn(
              s"[ReturnsLandingController] missing context (isAgent=${request.isAgent}, instanceId=$instanceId)"
            )
            Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
        .recover { case NonFatal(e) =>
          logger.error(s"[ReturnsLandingController] failed for instanceId=$instanceId)", e)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }
}
