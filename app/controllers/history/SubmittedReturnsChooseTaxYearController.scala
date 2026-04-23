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
import forms.history.SubmittedReturnsChooseTaxYearFormProvider
import models.history.TaxYearSelection.{AllTaxYears, TaxYear}
import models.history.TaxYearSelection
import pages.history.SubmittedReturnsChooseTaxYearPage
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.ManageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.history.SubmittedReturnsChooseTaxYearView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

class SubmittedReturnsChooseTaxYearController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requireCisId: CisIdRequiredAction,
  formProvider: SubmittedReturnsChooseTaxYearFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SubmittedReturnsChooseTaxYearView,
  manageService: ManageService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen requireCisId).async {
    implicit request =>
      manageService
        .getSubmittedTaxYears(request.cisId)
        .map { taxYears =>
          if (taxYears.isEmpty)
            logger.info(
              "[SubmittedReturnsChooseTaxYearController] Error trying to retrieve submitted tax years"
            )
            Redirect(controllers.routes.SystemErrorController.onPageLoad())
          else if (taxYears.length == 1) {
            // TODO: wire to correct controller once its ready
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          } else
            val taxYearStrings = taxYears.map((start, end) => s"$start to $end")

            val form = formProvider(taxYearStrings)

            val preparedForm = request.userAnswers.get(SubmittedReturnsChooseTaxYearPage) match {
              case None        => form
              case Some(value) => form.fill(value.toString)
            }

            Ok(view(preparedForm, taxYearStrings))
        }
        .recover { err =>
          logger.info(
            "[SubmittedReturnsChooseTaxYearController] Error trying to retrieve submitted tax years"
          )
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData andThen requireCisId).async {
    implicit request =>
      manageService.getSubmittedTaxYears(request.cisId).flatMap { taxYears =>
        val taxYearStrings = taxYears.map((start, end) => s"$start to $end")

        formProvider(taxYearStrings)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYearStrings))),
            value => {
              val regex                                   = Regex("^(\\d{4})\\sto\\s(\\d{4})$")
              val taxYearSelection: Try[TaxYearSelection] = value match {
                case regex(start, end) =>
                  Try(TaxYear(start.toInt, end.toInt))
                    .recover(_ => throw new Exception("Unable to parse tax year start/end"))
                case "all"             => Success(AllTaxYears)
                case _                 => Failure(Exception("unable to parse tax year selection"))
              }

              for {
                taxYear        <- Future.fromTry(taxYearSelection)
                updatedAnswers <- Future.fromTry(request.userAnswers.set(SubmittedReturnsChooseTaxYearPage, taxYear))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(controllers.history.routes.SubmittedReturnsChooseTaxYearController.onPageLoad())
            }
          )
      }
  }
}
