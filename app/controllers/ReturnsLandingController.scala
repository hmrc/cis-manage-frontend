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

import config.FrontendAppConfig
import controllers.actions.*
import pages.{AgentClientsPage, ContractorNamePage}
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ManageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.ReturnLandingViewModel
import views.html.ReturnsLandingView

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, TextStyle}
import java.util.Locale
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class ReturnsLandingController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ReturnsLandingView,
  service: ManageService
)(implicit appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(instanceId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val contractorNameOpt: Option[String] =
        if (request.isAgent) {
          for {
            clients <- request.userAnswers.get(AgentClientsPage)
            client  <- clients.find(_.uniqueId == instanceId)
            name    <- client.schemeName
          } yield name
        } else {
          request.userAnswers.get(ContractorNamePage)
        }

      contractorNameOpt match {
        case Some(contractorName) =>
          service
            .getUnsubmittedMonthlyReturns(instanceId)
            .map { response =>
              val returnsList: Seq[ReturnLandingViewModel] =
                response.unsubmittedCisReturns.map { r =>
                  ReturnLandingViewModel(
                    taxMonth = formatPeriod(r.taxMonth, r.taxYear),
                    returnType = r.returnType,
                    dateSubmitted = formatLastUpdate(r.lastUpdate),
                    status = r.status
                  )
                }

              Ok(view(contractorName, returnsList))
            }
            .recover { case NonFatal(e) =>
              logger.error(
                s"[ReturnsLandingController] Error fetching unsubmitted returns for instanceId: $instanceId",
                e
              )
              Redirect(controllers.routes.SystemErrorController.onPageLoad())
            }

        case None =>
          logger.warn(s"[ReturnsLandingController] contractorName missing (isAgent=${request.isAgent})")
          Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
      }
  }

  private def formatPeriod(taxMonth: Int, taxYear: Int): String = {
    val monthName = java.time.Month.of(taxMonth).getDisplayName(TextStyle.FULL, Locale.UK)
    s"$monthName $taxYear"
  }

  private def formatLastUpdate(lastUpdate: Option[LocalDateTime]): String =
    lastUpdate match {
      case Some(dateTime) =>
        dateTime.toLocalDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK))
      case None           => ""
    }

}
