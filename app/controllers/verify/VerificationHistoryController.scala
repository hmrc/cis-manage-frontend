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
import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear}
import models.verify.{VerificationHistoryData, VerificationTaxYearSelection}
import pages.verify.VerificationHistoryDataPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{VerificationHistoryService, VerificationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PageNotFoundView
import views.html.verify.{NoVerificationHistoryView, VerificationHistoryView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VerificationHistoryController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requireCisId: CisIdRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: VerificationHistoryView,
  noHistoryView: NoVerificationHistoryView,
  notFoundView: PageNotFoundView,
  verificationHistoryService: VerificationHistoryService,
  verificationService: VerificationService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(selectionStr: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId).async { implicit request =>
      VerificationTaxYearSelection fromPath selectionStr match
        case Some(selection) =>
          resolveVerificationHistoryData
            .map { data =>
              selection match
                case AllTaxYears    =>
                  verificationHistoryService.buildAllYearsViewModel(data, request.cisId) match
                    case Some(vm) if vm.taxYears.nonEmpty => Ok(view(vm))
                    case _                                => Ok(noHistoryView(request.cisId))
                case TaxYear(start) =>
                  verificationHistoryService.buildSingleYearViewModel(data, start, request.cisId) match
                    case Some(vm) if vm.taxYears.nonEmpty => Ok(view(vm))
                    case _                                => NotFound(notFoundView())
            }
            .recover { case _ =>
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
        case None            => Future.successful(NotFound(notFoundView()))
    }

  private def resolveVerificationHistoryData(implicit
    request: CisIdDataRequest[AnyContent]
  ): Future[VerificationHistoryData] =
    request.userAnswers.get(VerificationHistoryDataPage) match {
      case Some(data) =>
        Future.successful(data)

      case None =>
        verificationService
          .getSubmittedVerifications(request.cisId)
          .map(verificationHistoryService.toVerificationHistoryData)
    }
}
