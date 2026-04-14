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

package controllers.history

import controllers.actions.*
import models.history.SubmittedReturnsData
import models.requests.DataRequest
import pages.CisIdPage
import pages.history.SubmittedReturnsDataPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ManageService, SubmittedReturnsService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.history.SubmittedReturnsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmittedReturnsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: SubmittedReturnsView,
  submittedReturnsService: SubmittedReturnsService,
  manageService: ManageService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoadSingleYear(taxYear: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      resolveSubmittedReturnsData
        .map {
          case Some(data) =>
            submittedReturnsService.buildSingleYearViewModel(data, taxYear) match {
              case Some(vm) => Ok(view(vm))
              case None     => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }

          case None =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
        .recover { case e =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
  }

  def onPageLoadAllYears: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      resolveSubmittedReturnsData
        .map {
          case Some(vm) =>
            submittedReturnsService.buildAllYearsViewModel(vm) match {
              case Some(vm) => Ok(view(vm))
              case None     => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }

          case None =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
        .recover { case e =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
  }

  private def resolveSubmittedReturnsData(implicit
    request: DataRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Option[SubmittedReturnsData]] =
    request.userAnswers.get(SubmittedReturnsDataPage) match {
      case Some(data) =>
        Future.successful(Some(data))
      case None       =>
        request.userAnswers.get(CisIdPage) match {
          case Some(instanceId) =>
            manageService.getSubmittedMonthlyReturns(instanceId).map(Some(_))

          case None =>
            Future.successful(None)
        }
    }
}
