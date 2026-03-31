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
import pages.SubmittedReturnsDataPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{StubSubmittedReturnsData, SubmittedReturnsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.history.SubmittedReturnsView

import javax.inject.Inject

class SubmittedReturnsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: SubmittedReturnsView,
  service: SubmittedReturnsService
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoadSingleYear(taxYear: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val userAnswersWithStub =
        request.userAnswers.get(SubmittedReturnsDataPage) match {
          case Some(_) => request.userAnswers
          case None    => StubSubmittedReturnsData.addTo(request.userAnswers)
        }

      service.buildSingleYearViewModel(userAnswersWithStub, taxYear) match {
        case Some(vm) => Ok(view(vm))
        case None     => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onPageLoadAllYears: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val userAnswersWithStub =
      request.userAnswers.get(SubmittedReturnsDataPage) match {
        case Some(_) => request.userAnswers
        case None    => StubSubmittedReturnsData.addTo(request.userAnswers)
      }

    service.buildAllYearsViewModel(userAnswersWithStub) match {
      case Some(vm) => Ok(view(vm))
      case None     => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
