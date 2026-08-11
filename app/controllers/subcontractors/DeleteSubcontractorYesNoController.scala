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

package controllers.subcontractors

import controllers.actions.*
import forms.subcontractors.DeleteSubcontractorYesNoFormProvider
import models.Mode
import models.requests.CisIdDataRequest
import pages.subcontractors.{DeleteSubcontractorJourneyPage, DeleteSubcontractorYesNoPage, DeletedSubcontractorPage, SubcontractorListPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subcontractors.DeleteSubcontractorYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteSubcontractorYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  subcontractorService: SubcontractorService,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requireCisId: CisIdRequiredAction,
  formProvider: DeleteSubcontractorYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeleteSubcontractorYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(verificationNumber: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers
        .get(DeleteSubcontractorJourneyPage)
        .fold {
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        } { journeyData =>
          if (!journeyData.subcontractorCanBeDeleted) {
            Redirect(
              controllers.subcontractors.routes.CannotDeleteSubcontractorController.onPageLoad()
            )
          } else {
            val preparedForm =
              request.userAnswers
                .get(DeleteSubcontractorYesNoPage)
                .fold(form)(form.fill)

            val subcontractorName = verificationNumber.toLongOption
              .flatMap { subbieResourceRef =>
                request.userAnswers
                  .get(SubcontractorListPage)
                  .flatMap(_.subcontractors.find(_.subbieResourceRef.contains(subbieResourceRef)))
                  .flatMap(_.displayName)
              }
              .getOrElse(journeyData.subcontractorName)

            Ok(
              view(
                verificationNumber,
                subcontractorName,
                preparedForm,
                mode
              )
            )
          }
        }
    }

  def onSubmit(verificationNumber: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            request.userAnswers
              .get(DeleteSubcontractorJourneyPage)
              .fold(
                Future.successful(
                  Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                )
              ) { journeyData =>
                Future.successful(
                  BadRequest(
                    view(
                      verificationNumber,
                      journeyData.subcontractorName,
                      formWithErrors,
                      mode
                    )
                  )
                )
              },
          value =>
            if (!value) {
              Future.successful(
                Redirect(
                  controllers.subcontractors.routes.SubcontractorsListController.onPageLoad(request.cisId, mode)
                )
              )
            } else {
              request.userAnswers
                .get(DeleteSubcontractorJourneyPage)
                .fold(
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                ) { journeyData =>
                  verificationNumber.toLongOption match {
                    case Some(subbieResourceRef) =>
                      val subcontractorName = request.userAnswers
                        .get(SubcontractorListPage)
                        .flatMap(_.subcontractors.find(_.subbieResourceRef.contains(subbieResourceRef)))
                        .flatMap(_.displayName)
                        .getOrElse(journeyData.subcontractorName)
                      subcontractorService
                        .deleteSubcontractor(request.cisId, subbieResourceRef)
                        .flatMap { _ =>
                          cleanupUserAnswers(subcontractorName)
                            .map { _ =>
                              Redirect(
                                controllers.subcontractors.routes.SubcontractorDeletedConfirmationController
                                  .onPageLoad()
                              )
                            }
                        }
                        .recover { case ex =>
                          logger.error(
                            s"[DeleteSubcontractorYesNoController] Failed to delete subcontractor " +
                              s"(cisId=${request.cisId}, subbieResourceRef=$subbieResourceRef)",
                            ex
                          )
                          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                        }
                    case None                    =>
                      logger.error(
                        s"[DeleteSubcontractorYesNoController] Invalid verificationNumber: $verificationNumber"
                      )
                      Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                  }
                }
            }
        )
    }

  private def cleanupUserAnswers(
    subcontractorName: String
  )(implicit request: CisIdDataRequest[AnyContent]): Future[Boolean] =
    Future
      .fromTry {
        for {
          ua1 <- request.userAnswers.set(DeletedSubcontractorPage, subcontractorName)
          ua2 <- ua1.remove(DeleteSubcontractorJourneyPage)
          ua3 <- ua2.remove(DeleteSubcontractorYesNoPage)
          ua4 <- ua3.remove(SubcontractorListPage)
        } yield ua4
      }
      .flatMap(sessionRepository.set)
}
