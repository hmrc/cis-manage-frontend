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

package controllers.agent

import controllers.actions.*
import forms.ClientListSearchFormProvider
import models.UserAnswers
import models.agent.ClientListFormData
import models.requests.DataRequest
import pages.ClientListSearchPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.{ClientListPaginationResult, ManageService, PaginationService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.agent.{ClientListViewModel, SearchByList}
import views.html.agent.ClientListSearchView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class ClientListSearchController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  @Named("AgentIdentifier") identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ClientListSearchFormProvider,
  manageService: ManageService,
  paginationService: PaginationService,
  val controllerComponents: MessagesControllerComponents,
  view: ClientListSearchView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[ClientListFormData] = formProvider()

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      implicit val hc: HeaderCarrier =
        HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      val sortBy      = request.getQueryString("sortBy").orElse(Some("clientName"))
      val sortOrder   = request.getQueryString("sortOrder").orElse(Some("ascending"))
      val pageParam   = request.getQueryString("page")
      val currentPage = pageParam.flatMap(_.toIntOption).getOrElse(1)
      val pageToUse   = if ((sortBy.isDefined || sortOrder.isDefined) && pageParam.isEmpty) 1 else currentPage

      manageService
        .resolveAndStoreAgentClients(request.userAnswers)
        .flatMap {
          case (Nil, _) =>
            Future.successful(
              Redirect(controllers.agent.routes.NoAuthorisedClientsController.onPageLoad())
            )

          case (cisClients, uaWithClients) =>
            val preparedForm             = prepareForm(uaWithClients)
            val (searchBy, searchFilter) = currentSearch(preparedForm)

            val allClientsVm      = ClientListViewModel.fromCisClients(cisClients)
            val filteredClientsVm = ClientListViewModel.filterByField(searchBy, searchFilter, allClientsVm)
            val sortedClientsVm   = ClientListViewModel.sortClients(filteredClientsVm, sortBy, sortOrder)

            val paginationResult = paginationService.paginateClientList(
              allClients = sortedClientsVm,
              currentPage = pageToUse,
              baseUrl = routes.ClientListSearchController.onPageLoad().url,
              sortBy = sortBy,
              sortOrder = sortOrder
            )

            saveSearchAndRender(
              uaWithClients,
              preparedForm,
              searchBy,
              searchFilter,
              paginationResult,
              sortBy,
              sortOrder
            )
        }
        .recover { case e =>
          logger.error(s"[ClientListSearchController][onPageLoad] failed: ${e.getMessage}", e)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }

  def clearFilter(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      implicit val hc: HeaderCarrier =
        HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      manageService
        .resolveAndStoreAgentClients(request.userAnswers)
        .flatMap { case (_, uaWithClients) =>
          for {
            updatedAnswers <- Future.fromTry(uaWithClients.remove(ClientListSearchPage))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(routes.ClientListSearchController.onPageLoad())
        }
        .recover { case e =>
          logger.error(s"[ClientListSearchController][clearFilter] failed: ${e.getMessage}", e)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }

  def onSubmit: Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      implicit val hc: HeaderCarrier =
        HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      manageService
        .resolveAndStoreAgentClients(request.userAnswers)
        .flatMap { case (cisClients, uaWithClients) =>
          val allClientsVm = ClientListViewModel.fromCisClients(cisClients)

          val paginationResult = paginationService.paginateClientList(
            allClients = allClientsVm,
            currentPage = 1,
            baseUrl = routes.ClientListSearchController.onPageLoad().url,
            sortBy = None,
            sortOrder = None
          )

          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      SearchByList.searchByOptions,
                      paginationResult.paginatedData,
                      paginationResult.paginationViewModel,
                      None,
                      None
                    )
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(uaWithClients.set(ClientListSearchPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(routes.ClientListSearchController.onPageLoad())
            )
        }
        .recover { case e =>
          logger.error(s"[ClientListSearchController][onSubmit] failed: ${e.getMessage}", e)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }

  def downloadClientList(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      implicit val hc: HeaderCarrier =
        HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      implicit val msgs: Messages = messagesApi.preferred(request)

      manageService
        .resolveAndStoreAgentClients(request.userAnswers)
        .map { case (cisClients, uaWithClients) =>
          val clientsToDownload = ClientListViewModel.fromCisClients(cisClients)

          val header = Seq(
            msgs("agent.clientListSearch.th.clientName"),
            msgs("agent.clientListSearch.th.employerReference"),
            msgs("agent.clientListSearch.th.clientReference")
          ).mkString(",")

          val rows: Seq[String] = clientsToDownload.map { c =>
            val name        = csvEscape(c.clientName)
            val employerRef = csvEscape(c.employerReference)
            val clientRef   = csvEscape(c.clientReference)
            s"$name,$employerRef,$clientRef"
          }

          val csvContent = (header +: rows).mkString("\n")

          Ok(csvContent)
            .as("text/csv")
            .withHeaders("Content-Disposition" -> "attachment; filename=CISAgentClientList.csv")
        }
        .recover { case e =>
          logger.error(s"[ClientListSearchController][downloadClientList] failed: ${e.getMessage}", e)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }

  private def prepareForm(ua: UserAnswers): Form[ClientListFormData] =
    ua.get(ClientListSearchPage).fold(form)(form.fill)

  private def currentSearch(preparedForm: Form[ClientListFormData]): (String, String) = {
    val data               = preparedForm.value
    val activeSearchBy     = data.map(_.searchBy).getOrElse("")
    val activeSearchFilter = data.map(_.searchFilter).getOrElse("")
    (activeSearchBy, activeSearchFilter)
  }

  private def saveSearchAndRender(
    ua: UserAnswers,
    preparedForm: Form[ClientListFormData],
    searchBy: String,
    searchFilter: String,
    paginationResult: ClientListPaginationResult,
    sortBy: Option[String],
    sortOrder: Option[String]
  )(implicit request: DataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(ua.set(ClientListSearchPage, ClientListFormData(searchBy, searchFilter)))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Ok(
      view(
        preparedForm,
        SearchByList.searchByOptions,
        paginationResult.paginatedData,
        paginationResult.paginationViewModel,
        sortBy,
        sortOrder
      )
    )

  private def csvEscape(raw: String): String = {
    val trimmed     = raw.dropWhile(_.isWhitespace)
    val dangerous   = trimmed.nonEmpty && "=+-@\t\r\n".contains(trimmed.head)
    val neutralised = if (dangerous) "'" + raw else raw
    "\"" + neutralised.replace("\"", "\"\"") + "\""
  }
}
