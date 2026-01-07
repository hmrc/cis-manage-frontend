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

import models.EmployerReference
import models.requests.DataRequest
import pages.AgentClientsPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.{ExecutionContext, Future}

trait AuthorizedForSchemeAction extends ActionRefiner[DataRequest, DataRequest]

class AuthorizedForSchemeActionProvider {
  def apply(employerReference: EmployerReference)(using ec: ExecutionContext): AuthorizedForSchemeAction =
    new AuthorizedForSchemeAction {
      override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
        val authorized = if (request.isAgent) {
          request.userAnswers
            .get(AgentClientsPage)
            .toList
            .flatten
            .map(c => EmployerReference(c.taxOfficeNumber, c.taxOfficeRef))
            .contains(employerReference)
        } else {
          request.employerReference.contains(employerReference)
        }

        if (authorized) {
          Future.successful(Right(request))
        } else {
          Future.successful(Left(Redirect(controllers.routes.UnauthorisedController.onPageLoad())))
        }
      }

      override def executionContext: ExecutionContext = ec
    }

  def apply(taxOfficeNumber: String, taxOfficeReference: String)(using
    ec: ExecutionContext
  ): AuthorizedForSchemeAction = apply(EmployerReference(taxOfficeNumber, taxOfficeReference))
}
