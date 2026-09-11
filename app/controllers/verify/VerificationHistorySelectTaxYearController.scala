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

import controllers.{CisController, CisControllerComponents}
import forms.verify.TaxYearFormProvider
import models.verify.VerificationTaxYearSelection
import models.verify.VerificationTaxYearSelection.AllTaxYears
import play.api.mvc.{Action, AnyContent}
import services.{VerificationHistoryService, VerificationService}
import views.html.verify.VerificationHistorySelectTaxYearView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class VerificationHistorySelectTaxYearController @Inject() (
  val controllerComponents: CisControllerComponents,
  formProvider: TaxYearFormProvider,
  verificationService: VerificationService,
  verificationHistoryService: VerificationHistoryService,
  view: VerificationHistorySelectTaxYearView
)(implicit ec: ExecutionContext)
    extends CisController {

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
