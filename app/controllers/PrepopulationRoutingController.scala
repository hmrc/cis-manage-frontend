package controllers

import config.FrontendAppConfig
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ManageService, SchemeStatus}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.bootstrap.frontend.http.RedirectUrl
import pages.CisIdPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PrepopulationRoutingController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  controllerComponents: MessagesControllerComponents,
  manageService: ManageService
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def route(target: RedirectUrl): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    val safeTarget = target.getEither.toOption.filter(_.isRelativeUrl).map(_.url).getOrElse(controllers.routes.PageNotFoundController.onPageLoad().url)
    val ua        = request.userAnswers.getOrElse(UserAnswers(request.userId))

    getOrCreateCisId(ua).flatMap { case (cisId, updatedUa) =>
      manageService
        .getSchemeStatus(cisId)
        .map { status =>
          val destination = decideDestination(status, safeTarget)
          Redirect(destination)
        }
        .recover { _ =>
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }
  }

  private def decideDestination(status: SchemeStatus, targetUrl: String): String = {
    if (status.prePopSuccessful) {
      targetUrl
    } else {
      val namePresent = status.name.exists(_.nonEmpty)
      val utrPresent  = status.utr.exists(_.nonEmpty)

      (namePresent, utrPresent) match {
        case (true, true) =>
          targetUrl
        case (false, false) =>
          if (status.subcontractorCounter > 0) {
            controllers.routes.AddContractorDetailsController.onPageLoad().url
          } else {
            controllers.routes.IntroductionController.onPageLoad().url
          }
        case _ =>
          controllers.routes.AddContractorDetailsController.onPageLoad().url
      }
    }
  }

  private def getOrCreateCisId(ua: UserAnswers)(implicit request: models.requests.IdentifierRequest[_]): Future[(String, UserAnswers)] =
    ua.get(pages.CisIdPage) match {
      case Some(cisId) => Future.successful((cisId, ua))
      case None        => manageService.resolveAndStoreCisId(ua)
    }
}

