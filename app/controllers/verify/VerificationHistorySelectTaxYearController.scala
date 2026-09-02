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
import models.verify.VerificationTaxYearSelection
import models.verify.VerificationTaxYearSelection.AllTaxYears
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{VerificationHistoryService, VerificationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.VerificationHistorySelectTaxYearView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class VerificationHistorySelectTaxYearController @Inject() (
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
      verificationService
        .getSubmittedVerifications(request.cisId)
        .map { history =>
          val taxYears = verificationHistoryService.getSubmittedVerificationTaxYears(history)

          if taxYears.length > 1
          then Ok(view(formProvider(taxYears), taxYears))
          else Redirect(routes.VerificationHistoryController.onPageLoad(AllTaxYears.toPath))
        }
        .recover(_ => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId).async { implicit request =>
      verificationService
        .getSubmittedVerifications(request.cisId)
        .map { history =>
          val years = verificationHistoryService.getSubmittedVerificationTaxYears(history)
          val form  = formProvider(years)

          form
            .bindFromRequest()
            .fold(
              formWithErrors => BadRequest(view(formWithErrors, years)),
              selection => Redirect(routes.VerificationHistoryController.onPageLoad(selection.toPath))
            )
        }
        .recover(_ => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
}
