/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.SuccessfulAutomaticSubcontractorUpdateViewModel
import views.html.SuccessfulAutomaticSubcontractorUpdateView

import javax.inject.Inject

class SuccessfulAutomaticSubcontractorUpdateController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  view: SuccessfulAutomaticSubcontractorUpdateView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    val subcontractorsList: Seq[SuccessfulAutomaticSubcontractorUpdateViewModel] = Seq(
      SuccessfulAutomaticSubcontractorUpdateViewModel("Alice, A", "1111111111", " ", "01 Jan 2014"),
      SuccessfulAutomaticSubcontractorUpdateViewModel("Bob, B", "2222222222", " ", "01 Jan 2014"),
      SuccessfulAutomaticSubcontractorUpdateViewModel("Dave, D", "4444444444", "V1000000009", "07 May 2015"),
      SuccessfulAutomaticSubcontractorUpdateViewModel("Charles, C", "3333333333", "V1000000009", "01 Jan 2014"),
      SuccessfulAutomaticSubcontractorUpdateViewModel("Elise, E", "5555555555", "V1000000009", "07 May 2015"),
      SuccessfulAutomaticSubcontractorUpdateViewModel("Frank, F", "6666666666", "V1000000009", "07 Jan 2018")
    )

    Ok(view(subcontractorsList))
  }
}
