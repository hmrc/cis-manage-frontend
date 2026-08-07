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
import forms.verify.VerificationHistorySelectTaxYearFormProvider
import models.Mode
import models.verify.{VerificationHistoryData, VerificationTaxYearSelection}
import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear, TaxYearPeriod}
import pages.verify.{VerificationHistoryDataPage, VerificationHistorySelectTaxYearPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import models.verify.VerificationTaxYearSelection.given
import services.VerificationHistoryService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.VerificationHistorySelectTaxYearView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VerificationHistorySelectTaxYearController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: VerificationHistorySelectTaxYearFormProvider,
  verificationHistoryService: VerificationHistoryService,
  val controllerComponents: MessagesControllerComponents,
  view: VerificationHistorySelectTaxYearView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def selectionFrom(value: String): VerificationTaxYearSelection =
    VerificationTaxYearSelection.fromString(value)

  private def taxYears(data: VerificationHistoryData): Seq[TaxYearPeriod] =
    verificationHistoryService
      .getSubmittedVerificationTaxYears(data)

  private def validValues(taxYears: Seq[TaxYearPeriod]): Seq[String] =
    taxYears.map(_.startYear.toString)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers.get(VerificationHistoryDataPage) match {
        case Some(data) =>
          val years = taxYears(data)
          val form  = formProvider(validValues(years))

          val preparedForm =
            request.userAnswers.get(VerificationHistorySelectTaxYearPage) match {
              case Some(AllTaxYears) => form.fill("all")
              case Some(TaxYear(v))  => form.fill(v.toString)
              case None              => form
            }

          Ok(view(preparedForm, mode, years))

        case None =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.get(VerificationHistoryDataPage) match {
        case Some(data) =>
          val years = taxYears(data)
          val form  = formProvider(validValues(years))

          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(view(formWithErrors, mode, years))
                ),
              value => {
                val selection = selectionFrom(value)

                for {
                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(VerificationHistorySelectTaxYearPage, selection)
                                    )
                  _              <- sessionRepository.set(updatedAnswers)
                } yield selection match {
                  case AllTaxYears =>
                    Redirect(controllers.verify.routes.VerificationHistoryController.onPageLoadAllYears())
                  case TaxYear(_)  =>
                    Redirect(controllers.verify.routes.VerificationHistoryController.onPageLoadSingleYear())
                }
              }
            )

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}
