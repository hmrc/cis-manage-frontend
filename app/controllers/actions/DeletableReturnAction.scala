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

package controllers.actions

import javax.inject.Inject
import models.requests.DataRequest
import models.{UnsubmittedMonthlyReturn, UserAnswers}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result, WrappedRequest}
import play.api.Logging
import queries.delete.UnsubmittedReturnToDeleteQuery

import scala.concurrent.{ExecutionContext, Future}

case class DeletableReturnRequest[A](
  request: DataRequest[A],
  returnToDelete: UnsubmittedMonthlyReturn
) extends WrappedRequest[A](request) {
  def userAnswers: UserAnswers = request.userAnswers
}

trait DeletableReturnAction extends ActionRefiner[DataRequest, DeletableReturnRequest]

class DeletableReturnActionImpl @Inject() (implicit val executionContext: ExecutionContext)
    extends DeletableReturnAction
    with Logging {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DeletableReturnRequest[A]]] =
    request.userAnswers.get(UnsubmittedReturnToDeleteQuery) match {
      case Some(returnToDelete) if returnToDelete.deletable =>
        Future.successful(Right(DeletableReturnRequest(request, returnToDelete)))

      case Some(returnToDelete) =>
        logger.warn(
          s"[DeletableReturnAction] Record not deletable for monthlyReturnId=${returnToDelete.monthlyReturnId}"
        )
        Future.successful(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

      case None =>
        logger.error("[DeletableReturnAction] UnsubmittedReturnToDeleteQuery missing")
        Future.successful(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
    }
}
