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
import controllers.actions.IdentifierAction
import models.UserAnswers
import models.requests.RequestUserType

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthProviders, AuthorisationException, AuthorisedFunctions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IntroductionView

import scala.concurrent.{ExecutionContext, Future}

class IntroductionController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  override val authConnector: AuthConnector,
  identify: IdentifierAction,
  sessionRepository: SessionRepository,
  view: IntroductionView
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with AuthorisedFunctions {

  def onPageLoad: Action[AnyContent] = Action.async { request =>
    signedInUserType(request).map { userType =>
      val viewRequest: Request[AnyContent] = userType match {
        case SignedInAgent => request.addAttr(RequestUserType.IsAgentKey, true)
        case SignedInOther => request
        case SignedOut     => request.addAttr(RequestUserType.IsSignedOutKey, true)
      }
      Ok(view()(viewRequest, appConfig, messagesApi.preferred(viewRequest)))
    }
  }

  private def signedInUserType(request: Request[?]): Future[PublicPageUser] = {
    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    authorised(AuthProviders(GovernmentGateway))
      .retrieve(Retrievals.affinityGroup)(affinityGroup =>
        Future.successful(if (affinityGroup.contains(Agent)) SignedInAgent else SignedInOther)
      )
      .recover {
        case _: AuthorisationException => SignedOut
        case _                         => SignedInOther
      }
  }

  def affinityGroupRouting: Action[AnyContent] = identify.async { implicit request =>
    val userAnswers = UserAnswers(request.userId)
    sessionRepository.set(userAnswers).map { _ =>
      if (request.isAgent) {
        Redirect(controllers.agent.routes.RetrievingClientController.onPageLoad())
      } else {
        Redirect(controllers.contractor.routes.ContractorLandingController.onPageLoad())
      }
    }
  }

}

private sealed trait PublicPageUser
private case object SignedInAgent extends PublicPageUser
private case object SignedInOther extends PublicPageUser
private case object SignedOut extends PublicPageUser
