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

import com.google.inject.Inject
import models.requests.{IdentifierRequest, OptionalIdentifierRequest}
import play.api.Logging
import play.api.mvc.*
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.AuthUtils.{hasCisAgentEnrolment, hasCisOrgEnrolment}

import scala.concurrent.{ExecutionContext, Future}

trait OptionalIdentifierAction
    extends ActionBuilder[OptionalIdentifierRequest, AnyContent]
    with ActionFunction[Request, OptionalIdentifierRequest]

class SplitterIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  val parser: BodyParsers.Default,
  clientListCheckEnforcer: ClientListCheckEnforcer
)(implicit val executionContext: ExecutionContext)
    extends OptionalIdentifierAction
    with AuthorisedFunctions
    with Logging {

  override def invokeBlock[A](
    request: Request[A],
    block: OptionalIdentifierRequest[A] => Future[Result]
  ): Future[Result] = {
    implicit val hc: HeaderCarrier  = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val defaultPredicate: Predicate = AuthProviders(GovernmentGateway)
    authorised(defaultPredicate)
      .retrieve(
        Retrievals.internalId and Retrievals.allEnrolments
          and Retrievals.affinityGroup
      ) {
        case Some(internalId) ~ Enrolments(enrolments) ~ Some(Organisation) =>
          hasCisOrgEnrolment(enrolments)
            .map { employerReference =>
              block(
                OptionalIdentifierRequest(
                  request,
                  Some(IdentifierRequest(request, internalId, Some(employerReference), None))
                )
              )
            }
            .getOrElse(
              block(OptionalIdentifierRequest(request, None))
            )
        case Some(internalId) ~ _ ~ Some(Individual)                        =>
          logger.info("EnrolmentAuthIdentifierAction - Individual login attempt")
          block(OptionalIdentifierRequest(request, None))
        case Some(internalId) ~ Enrolments(enrolments) ~ Some(Agent)        =>
          hasCisAgentEnrolment(enrolments)
            .map { agentReference =>
              val identifierRequest = IdentifierRequest(request, internalId, None, Some(agentReference), true)
              clientListCheckEnforcer.optionalCheck(OptionalIdentifierRequest(request, Some(identifierRequest)))(block)
            }
            .getOrElse(
              Future.successful(
                Redirect(controllers.routes.UnauthorisedAgentAffinityController.onPageLoad())
                  .addingToSession("userId" -> internalId)(request)
              )
            )
        case _                                                              =>
          logger.warn("EnrolmentAuthIdentifierAction - Unable to retrieve internal id or affinity group")
          block(OptionalIdentifierRequest(request, None))
      } recoverWith {
      case _: NoActiveSession        =>
        block(OptionalIdentifierRequest(request, None))
      case _: AuthorisationException =>
        logger.warn("EnrolmentAuthIdentifierAction - AuthorisationException")
        block(OptionalIdentifierRequest(request, None))
    }
  }
}
