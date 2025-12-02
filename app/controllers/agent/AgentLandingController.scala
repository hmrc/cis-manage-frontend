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

package controllers.agent

import controllers.actions.*
import config.FrontendAppConfig
import play.api.Logging

import javax.inject.{Inject, Named}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ManageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.agent.AgentLandingView

import scala.concurrent.ExecutionContext

class AgentLandingController @Inject() (
  override val messagesApi: MessagesApi,
  @Named("AgentIdentifier") identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  manageService: ManageService,
  val controllerComponents: MessagesControllerComponents,
  view: AgentLandingView,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(uniqueId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      implicit val config: FrontendAppConfig = appConfig
      implicit val hc: HeaderCarrier         = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      manageService
        .getAgentLandingData(uniqueId, request.userAnswers)
        .map { viewModel =>
          Ok(
            view(
              clientName = viewModel.clientName,
              employerRef = viewModel.employerRef,
              utr = viewModel.utr.getOrElse(""),
              // still hard-coded, mocked for now
              returnsDueCount = 1,
              returnsDueBy = java.time.LocalDate.of(2025, 10, 19),
              newNoticesCount = 2,
              lastSubmittedDate = java.time.LocalDate.of(2025, 9, 19),
              lastSubmittedTaxMonth = java.time.YearMonth.of(2025, 8)
            )
          )
        }
        .recover { case e =>
          logger.error(s"[AgentLandingController][onPageLoad] Failed for uniqueId=$uniqueId", e)
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }
}
