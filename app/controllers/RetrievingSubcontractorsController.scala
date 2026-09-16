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
import services.PrepopService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RetrievingSubcontractorsView
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, SchemeAuthorisationGuard}
import models.EmployerReference
import models.requests.DataRequest
import pages.AgentClientsPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrievingSubcontractorsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  schemeAuthorisationGuard: SchemeAuthorisationGuard,
  val controllerComponents: MessagesControllerComponents,
  view: RetrievingSubcontractorsView,
  prepopService: PrepopService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(
    instanceId: String,
    targetKey: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen schemeAuthorisationGuard.forInstanceId(instanceId)) {
      implicit request =>
        Ok(view())
          .withHeaders(
            "Refresh" -> s"0; url=${controllers.routes.RetrievingSubcontractorsController.start(instanceId, targetKey).url}"
          )
    }

  def start(
    instanceId: String,
    targetKey: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen schemeAuthorisationGuard.forInstanceId(instanceId)).async {
      implicit request =>
        resolveEmployerReference(request, instanceId) match {

          case None =>
            Future.successful(
              Redirect(routes.SystemErrorController.onPageLoad())
            )

          case Some(EmployerReference(taxOfficeNumber, taxOfficeReference)) =>
            for {
              prepopOk  <- prepopService.prepopulate(taxOfficeNumber, taxOfficeReference, instanceId)
              schemeOpt <- if (prepopOk) prepopService.getScheme(instanceId) else Future.successful(None)
              result    <- schemeOpt match {
                             case None =>
                               Future.successful(
                                 Redirect(routes.UnsuccessfulAutomaticSubcontractorUpdateController.onPageLoad(instanceId))
                               )

                             case Some(scheme) =>
                               scheme.prePopSuccessful match {
                                 case Some("Y") if scheme.subcontractorCounter.exists(_ > 0) =>
                                   Future.successful(
                                     Redirect(
                                       routes.SuccessfulAutomaticSubcontractorUpdateController
                                         .onPageLoad(instanceId, targetKey)
                                     )
                                   )

                                 case Some("Y") =>
                                   Future.successful(
                                     Redirect(routes.SuccessfulNoRecordsFoundController.onPageLoad(instanceId, targetKey))
                                   )

                                 case Some("N") =>
                                   Future.successful(
                                     Redirect(
                                       routes.UnsuccessfulAutomaticSubcontractorUpdateController.onPageLoad(instanceId)
                                     )
                                   )

                                 case _ =>
                                   Future.successful(
                                     Redirect(
                                       routes.UnsuccessfulAutomaticSubcontractorUpdateController.onPageLoad(instanceId)
                                     )
                                   )
                               }
                           }
            } yield result
        }
    }

  private def resolveEmployerReference[A](
    request: DataRequest[A],
    instanceId: String
  ): Option[EmployerReference] =
    if request.isAgent then
      AgentClientsPage
        .findClient(request.userAnswers, instanceId)
        .map { client =>
          EmployerReference(client.taxOfficeNumber, client.taxOfficeRef)
        }
    else request.employerReference

}
