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

import models.UserAnswers
import pages.ContractorNamePage
import play.api.Logging
import play.api.mvc.{Action, AnyContent, RequestHeader}
import repositories.SessionRepository
import services.ManageService
import views.html.ReturnsLandingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class ReturnsLandingController @Inject() (
  val controllerComponents: CisControllerComponents,
  sessionRepository: SessionRepository,
  view: ReturnsLandingView,
  service: ManageService
)(using ExecutionContext)
    extends CisController
    with Logging {

  def onPageLoad(instanceId: String): Action[AnyContent] =
    (identify
      andThen getData
      andThen requireData
      andThen hasClientGuard.forInstanceId(instanceId)).async { implicit request =>
      updateContractorNameFromQueryParam(request.userAnswers)
        .flatMap { userAnswers =>
          service
            .buildReturnsLandingContext(instanceId, userAnswers, request.isAgent)
            .map {
              case Some(context) =>
                Ok(
                  view(
                    context.contractorName,
                    context.standardReturnLink,
                    context.nilReturnLink,
                    context.returnToHomeLink
                  )
                )
              case None          =>
                logger.warn(
                  s"[ReturnsLandingController] missing context (isAgent=${request.isAgent}, instanceId=$instanceId)"
                )
                SystemError
            }
            .recover { case NonFatal(e) =>
              logger.error(s"[ReturnsLandingController] failed for instanceId=$instanceId", e)
              SystemError
            }
        }
    }

  private def updateContractorNameFromQueryParam(
    userAnswers: UserAnswers
  )(implicit request: RequestHeader): Future[UserAnswers] = {
    val contractorNameOpt = request.getQueryString("contractorName").map(_.trim).filter(_.nonEmpty)

    contractorNameOpt match {
      case Some(contractorName) =>
        Future
          .fromTry(userAnswers.set(ContractorNamePage, contractorName))
          .flatMap { updatedUserAnswers =>
            sessionRepository.set(updatedUserAnswers).map(_ => updatedUserAnswers)
          }
      case None                 =>
        Future.successful(userAnswers)
    }
  }
}
