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

package controllers.verify

import controllers.actions.*
import models.requests.CisIdDataRequest
import models.verify.{VerificationHistoryData, VerificationTaxYearSelection}
import models.verify.VerificationTaxYearSelection.TaxYear
import pages.verify.{VerificationHistoryDataPage, VerificationHistorySelectTaxYearPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ManageService, VerificationHistoryService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.verify.VerificationHistoryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VerificationHistoryController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requireCisId: CisIdRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: VerificationHistoryView,
  verificationHistoryService: VerificationHistoryService,
  manageService: ManageService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoadSingleYear(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId).async { implicit request =>

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      request.userAnswers.get(VerificationHistorySelectTaxYearPage) match {
        case Some(TaxYear(v)) =>
          val taxYear = v.takeWhile(_ != ' ')
          resolveVerificationHistoryData
            .map { data =>
              verificationHistoryService.buildSingleYearViewModel(data, taxYear, request.cisId) match {
                case Some(vm) => Ok(view(vm))
                case None     => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }
            }
            .recover { case _ =>
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
        case _                =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def onPageLoadAllYears: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId).async { implicit request =>

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      resolveVerificationHistoryData
        .map { data =>
          verificationHistoryService.buildAllYearsViewModel(data, request.cisId) match {
            case Some(vm) => Ok(view(vm))
            case None     => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
        }
        .recover { case _ =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  private def resolveVerificationHistoryData(implicit
    request: CisIdDataRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[VerificationHistoryData] =
    request.userAnswers.get(VerificationHistoryDataPage) match {
      case Some(data) =>
        Future.successful(data)
      case None       =>
        manageService.getVerificationHistory(request.cisId)
    }
}
