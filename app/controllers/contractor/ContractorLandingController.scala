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

package controllers.contractor

import ContractorLandingController.viewModel
import config.FrontendAppConfig
import controllers.actions.*
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ManageService
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.contractor.ContractorLandingViewModel
import views.html.contractor.ContractorLandingView

import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext

class ContractorLandingController @Inject() (
  override val messagesApi: MessagesApi,
  @Named("ContractorIdentifier") identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view: ContractorLandingView,
  appConfig: FrontendAppConfig,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  manageService: ManageService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    manageService
      .resolveAndStoreCisId(request.userAnswers)
      .map { _ =>
        Ok(view(viewModel(appConfig)))
      }
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case exception                                             =>
          logger.error(
            s"[ContractorLandingController] Failed to retrieve cisId: ${exception.getMessage}",
            exception
          )
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
      }

  }
}

object ContractorLandingController {

  def viewModel(appConfig: FrontendAppConfig): ContractorLandingViewModel = ContractorLandingViewModel(
    "123/AB45678",
    "1234567890",
    1,
    "19 October 2025",
    2,
    "19 September 2025",
    "August 2025",
    appConfig.contractorLandingWhatIsUrl,
    appConfig.contractorLandingGuidanceUrl,
    appConfig.contractorLandingPenaltiesUrl
  )
}
