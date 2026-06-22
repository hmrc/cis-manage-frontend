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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.SubcontractorSubmissionReceiptView

import javax.inject.Inject

class SubcontractorSubmissionReceiptController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requireCisId: CisIdRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: SubcontractorSubmissionReceiptView
) extends FrontendBaseController
    with I18nSupport {

  private val submissionTime = "12:00"
  private val submissionDate = "18 May 2025"
  private val contractorName = "John Doe"
  private val employerRef    = "ABC12345"
  private val IRNumber       = "123456"

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData andThen requireCisId) {
    implicit request =>
      Ok(view(submissionTime, submissionDate, contractorName, employerRef, IRNumber, request.cisId))
  }
}
