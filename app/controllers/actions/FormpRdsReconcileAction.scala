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

import models.requests.CisIdDataRequest
import pages.AgentClientsPage
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, PRECONDITION_FAILED}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import services.PrepopService
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait FormpRdsReconcileAction extends ActionFilter[CisIdDataRequest]

// F1 - runs the FORMP vs RDS DataCache comparison/update before a target-link destination is served.
class FormpRdsReconcileActionImpl @Inject() (
  prepopService: PrepopService
)(implicit val executionContext: ExecutionContext)
    extends FormpRdsReconcileAction
    with Logging {

  override protected def filter[A](request: CisIdDataRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    resolveTaxOffice(request) match {
      case None =>
        logger.warn(
          s"[FormpRdsReconcileAction] Missing tax office details for cisId=${request.cisId}; cannot run FORMP-RDS comparison"
        )
        Future.successful(Some(Redirect(controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad())))

      case Some((taxOfficeNumber, taxOfficeReference)) =>
        prepopService
          .prepopulateContractorKnownFacts(request.cisId, taxOfficeNumber, taxOfficeReference)
          .map(_ => None)
          .recover {
            case u: UpstreamErrorResponse if u.statusCode == PRECONDITION_FAILED || u.statusCode == NOT_FOUND =>
              logger.warn(
                s"[FormpRdsReconcileAction] Contractor data missing for cisId=${request.cisId} (status=${u.statusCode})"
              )
              Some(Redirect(controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad()))

            case NonFatal(e) =>
              logger.error(s"[FormpRdsReconcileAction] FORMP-RDS comparison failed for cisId=${request.cisId}", e)
              Some(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
          }
    }
  }

  private def resolveTaxOffice[A](request: CisIdDataRequest[A]): Option[(String, String)] =
    request.employerReference match {
      case Some(ref) => Some((ref.taxOfficeNumber, ref.taxOfficeReference))
      case None      =>
        request.userAnswers
          .get(AgentClientsPage)
          .flatMap(_.find(_.uniqueId == request.cisId))
          .map(client => (client.taxOfficeNumber, client.taxOfficeRef))
    }
}
