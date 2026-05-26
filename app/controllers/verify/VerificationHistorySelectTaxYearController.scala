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
import models.verify.VerificationTaxYearSelection
import models.verify.VerificationTaxYearSelection.{AllTaxYears, TaxYear}
import pages.verify.VerificationHistorySelectTaxYearPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import models.verify.VerificationTaxYearSelection.given
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.VerificationHistorySelectTaxYearView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VerificationHistorySelectTaxYearController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  formProvider: VerificationHistorySelectTaxYearFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: VerificationHistorySelectTaxYearView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val taxYearStrings: Seq[String] =
    VerificationTaxYearSelection.taxYears.map(_.value)

  private val form = formProvider(taxYearStrings)

  private def selectionFrom(value: String): VerificationTaxYearSelection =
    if (value == "all") AllTaxYears else TaxYear(value)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData) { implicit request =>

      val preparedForm =
        request.userAnswers.flatMap(_.get(VerificationHistorySelectTaxYearPage)) match {
          case Some(AllTaxYears) => form.fill("all")
          case Some(TaxYear(v))  => form.fill(v)
          case None              => form
        }

      Ok(view(preparedForm, mode, taxYearStrings))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, mode, taxYearStrings))
            ),
          value => {

            val selection = selectionFrom(value)

            request.userAnswers match {

              case None =>
                Future.successful(
                  Redirect(controllers.verify.routes.VerificationHistorySelectTaxYearController.onPageLoad())
                )

              case Some(ua) =>
                for {
                  updatedAnswers <- Future.fromTry(
                                      ua.set(VerificationHistorySelectTaxYearPage, selection)
                                    )
                  _              <- sessionRepository.set(updatedAnswers)
                } yield selection match {
                  case AllTaxYears =>
                    Redirect(controllers.verify.routes.VerificationHistoryController.onPageLoadAllYears())
                  case TaxYear(v)  =>
                    val yearStart = v.takeWhile(_ != ' ')
                    Redirect(controllers.verify.routes.VerificationHistoryController.onPageLoadSingleYear(yearStart))
                }
            }
          }
        )
    }
}
