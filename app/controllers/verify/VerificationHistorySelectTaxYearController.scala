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
import forms.verify.TaxYearFormProvider
import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear, given}
import models.verify.{VerificationHistoryData, VerificationTaxYearSelection}
import pages.verify.{VerificationHistoryDataPage, VerificationHistorySelectTaxYearPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{VerificationHistoryService, VerificationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.VerificationHistorySelectTaxYearView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VerificationHistorySelectTaxYearController @Inject() (
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requireCisId: CisIdRequiredAction,
  formProvider: TaxYearFormProvider,
  verificationService: VerificationService,
  verificationHistoryService: VerificationHistoryService,
  val controllerComponents: MessagesControllerComponents,
  view: VerificationHistorySelectTaxYearView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId).async { implicit request =>
      for
        history     <- verificationService.getSubmittedVerifications(request.cisId)
        historyData  = verificationHistoryService.toVerificationHistoryData(history)
        userAnswers <- Future.fromTry(request.userAnswers.set(VerificationHistoryDataPage, historyData))
        _           <- sessionRepository.set(userAnswers)
        taxYears     = verificationHistoryService.getSubmittedVerificationTaxYears(historyData)
        form         = formProvider(taxYears)
      yield Ok(view(formProvider(taxYears), taxYears))
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.get(VerificationHistoryDataPage) match {
        case Some(data) =>
          val years = verificationHistoryService.getSubmittedVerificationTaxYears(data)
          val form  = formProvider(years)

          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, years))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(VerificationHistorySelectTaxYearPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield value match {
                  case AllTaxYears =>
                    Redirect(controllers.verify.routes.VerificationHistoryController.onPageLoadAllYears())
                  case TaxYear(_)  =>
                    Redirect(controllers.verify.routes.VerificationHistoryController.onPageLoadSingleYear())
                }
            )

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

}
