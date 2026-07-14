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
import forms.subcontractors.SubcontractorsListFormProvider
import models.{Mode, UserAnswers}
import models.response.GetSubcontractor
import pages.subcontractors.SubcontractorListPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, RequestHeader, Result}
import services.PaginationSubcontractorsListService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.subcontractors.*
import views.html.subcontractors.SubcontractorsListView

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SubcontractorsListController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SubcontractorsListFormProvider,
  paginationService: PaginationSubcontractorsListService,
  val controllerComponents: MessagesControllerComponents,
  view: SubcontractorsListView
) extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  private val dateAddedFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy")

  private final case class ListFilters(
    searchTerm: String,
    verificationStatus: VerificationStatusFilter,
    taxTreatment: TaxTreatmentFilter
  )

  private def encode(value: String): String =
    URLEncoder.encode(value, StandardCharsets.UTF_8.toString)

  private def appendQueryParams(
    url: String,
    params: Seq[(String, String)]
  ): String = {
    val filteredParams =
      params.collect {
        case (key, value) if value.nonEmpty =>
          s"$key=${encode(value)}"
      }

    if (filteredParams.isEmpty) {
      url
    } else {
      val separator =
        if (url.contains("?")) "&"
        else "?"

      s"$url$separator${filteredParams.mkString("&")}"
    }
  }

  private def rowsFromUserAnswers(
    userAnswers: UserAnswers
  ): Option[Seq[SubcontractorsListRow]] =
    userAnswers
      .get(SubcontractorListPage)
      .map(_.subcontractors.map(toListRow))

  private def toListRow(
    subcontractor: GetSubcontractor
  ): SubcontractorsListRow =
    SubcontractorsListRow(
      id = subcontractor.subcontractorId.toString,
      name = subcontractor.displayName,
      utr = subcontractor.utr.getOrElse(""),
      verified = subcontractor.verified.exists(_.equalsIgnoreCase("Y")),
      verificationNumber = subcontractor.verificationNumber.getOrElse(""),
      taxTreatment = toTaxTreatment(subcontractor.taxTreatment),
      dateAdded = subcontractor.createDate
        .map(_.format(dateAddedFormatter))
        .getOrElse(""),
      subbieResourceRef = getSubbieResourceRef(subcontractor)
    )

  private def getSubbieResourceRef(
    subcontractor: GetSubcontractor
  ): Long =
    subcontractor.subbieResourceRef.getOrElse {
      throw new IllegalStateException(
        s"Missing subbieResourceRef for subcontractorId ${subcontractor.subcontractorId}"
      )
    }

  private def toTaxTreatment(
    taxTreatment: Option[String]
  ): TaxTreatment =
    taxTreatment.map(_.trim.toLowerCase) match {
      case Some("gross") =>
        TaxTreatment.Gross

      case Some("higher rate" | "higherrate") =>
        TaxTreatment.HigherRate

      case Some("standard rate" | "standardrate") =>
        TaxTreatment.StandardRate

      case _ =>
        TaxTreatment.Unknown
    }

  private def filterRows(
    allRows: Seq[SubcontractorsListRow],
    filters: ListFilters
  ): Seq[SubcontractorsListRow] =
    filterByTaxTreatment(
      filterByVerificationStatus(
        filterBySearchTerm(allRows, filters.searchTerm),
        filters.verificationStatus
      ),
      filters.taxTreatment
    ).sortBy(_.name.toLowerCase)

  private def filterBySearchTerm(
    rows: Seq[SubcontractorsListRow],
    searchTerm: String
  ): Seq[SubcontractorsListRow] = {
    val trimmedSearchTerm =
      searchTerm.trim

    if (trimmedSearchTerm.isEmpty) {
      rows
    } else {
      val lowerCaseSearchTerm =
        trimmedSearchTerm.toLowerCase

      rows.filter { row =>
        row.name.toLowerCase.contains(lowerCaseSearchTerm) ||
        row.utr.contains(trimmedSearchTerm) ||
        row.verificationNumber.contains(trimmedSearchTerm)
      }
    }
  }

  private def filterByVerificationStatus(
    rows: Seq[SubcontractorsListRow],
    verificationStatus: VerificationStatusFilter
  ): Seq[SubcontractorsListRow] =
    verificationStatus match {
      case VerificationStatusFilter.Verified =>
        rows.filter(_.verified)

      case VerificationStatusFilter.NotVerified =>
        rows.filterNot(_.verified)

      case VerificationStatusFilter.All =>
        rows
    }

  private def filterByTaxTreatment(
    rows: Seq[SubcontractorsListRow],
    taxTreatment: TaxTreatmentFilter
  ): Seq[SubcontractorsListRow] =
    taxTreatment match {
      case TaxTreatmentFilter.Gross =>
        rows.filter(_.taxTreatment == TaxTreatment.Gross)

      case TaxTreatmentFilter.HigherRate =>
        rows.filter(_.taxTreatment == TaxTreatment.HigherRate)

      case TaxTreatmentFilter.StandardRate =>
        rows.filter(_.taxTreatment == TaxTreatment.StandardRate)

      case TaxTreatmentFilter.Unknown =>
        rows.filter(_.taxTreatment == TaxTreatment.Unknown)

      case TaxTreatmentFilter.All =>
        rows
    }

  private def getListFilters(
    request: RequestHeader
  ): ListFilters =
    ListFilters(
      searchTerm = request.getQueryString("searchTerm").getOrElse("").trim,
      verificationStatus = VerificationStatusFilter.fromString(
        request
          .getQueryString("verificationStatus")
          .getOrElse(VerificationStatusFilter.All.value)
      ),
      taxTreatment = TaxTreatmentFilter.fromString(
        request
          .getQueryString("taxTreatment")
          .getOrElse(TaxTreatmentFilter.All.value)
      )
    )

  private def getFormFilters(
    formData: Map[String, Seq[String]]
  ): ListFilters =
    ListFilters(
      searchTerm = formData
        .get("searchTerm")
        .flatMap(_.headOption)
        .getOrElse("")
        .trim,
      verificationStatus = VerificationStatusFilter.fromString(
        formData
          .get("verificationStatus")
          .flatMap(_.headOption)
          .getOrElse(VerificationStatusFilter.All.value)
      ),
      taxTreatment = TaxTreatmentFilter.fromString(
        formData
          .get("taxTreatment")
          .flatMap(_.headOption)
          .getOrElse(TaxTreatmentFilter.All.value)
      )
    )

  private def getTargetPage(
    formData: Map[String, Seq[String]]
  ): Int =
    formData
      .get("gotoPage")
      .flatMap(_.headOption)
      .flatMap(_.toIntOption)
      .getOrElse(1)

  private def queryString(
    filters: ListFilters
  ): String =
    Seq(
      Option(filters.searchTerm)
        .filter(_.nonEmpty)
        .map(value => s"searchTerm=${encode(value)}"),
      Option(filters.verificationStatus.value)
        .filter(_ != VerificationStatusFilter.All.value)
        .map(value => s"verificationStatus=${encode(value)}"),
      Option(filters.taxTreatment.value)
        .filter(_ != TaxTreatmentFilter.All.value)
        .map(value => s"taxTreatment=${encode(value)}")
    ).flatten.mkString("&")

  private def paginateRows(
    allRows: Seq[SubcontractorsListRow],
    instanceId: String,
    mode: Mode,
    page: Int,
    filters: ListFilters
  ) =
    paginationService.paginate(
      allItems = filterRows(allRows, filters),
      currentPage = page,
      recordsPerPage = SubcontractorsListConstants.RecordsPerPage,
      baseUrl = routes.SubcontractorsListController
        .onPageLoad(instanceId, mode)
        .url,
      queryString = queryString(filters)
    )

  private def renderList(
    formToDisplay: Form[String],
    allRows: Seq[SubcontractorsListRow],
    instanceId: String,
    mode: Mode,
    page: Int,
    filters: ListFilters
  )(implicit request: Request[AnyContent]) = {
    val result =
      paginateRows(
        allRows,
        instanceId,
        mode,
        page,
        filters
      )

    view(
      formToDisplay,
      mode,
      result.items,
      result.pagination,
      result.currentPage,
      result.totalPages,
      result.startIndex,
      result.totalCount,
      instanceId,
      filters.searchTerm,
      filters.verificationStatus.value,
      filters.taxTreatment.value
    )
  }

  private def renderPage(
    allRows: Seq[SubcontractorsListRow],
    instanceId: String,
    mode: Mode,
    page: Int,
    filters: ListFilters
  )(implicit request: Request[AnyContent]): Result =
    Ok(
      renderList(
        form.fill(filters.searchTerm),
        allRows,
        instanceId,
        mode,
        page,
        filters
      )
    )

  private def renderInvalidForm(
    formWithErrors: Form[String],
    allRows: Seq[SubcontractorsListRow],
    instanceId: String,
    mode: Mode,
    page: Int,
    filters: ListFilters
  )(implicit request: Request[AnyContent]): Result = {
    val filtersForResults =
      if (formWithErrors.error("searchTerm").isDefined) {
        filters.copy(searchTerm = "")
      } else {
        filters
      }

    BadRequest(
      renderList(
        formWithErrors,
        allRows,
        instanceId,
        mode,
        page,
        filtersForResults
      )
    )
  }

  private def redirectToFilteredList(
    instanceId: String,
    mode: Mode,
    targetPage: Int,
    searchTerm: String,
    filters: ListFilters
  ): Result = {
    val baseUrl =
      routes.SubcontractorsListController
        .onPageLoad(instanceId, mode, targetPage)
        .url

    Redirect(
      appendQueryParams(
        baseUrl,
        Seq(
          "searchTerm"         -> searchTerm.trim,
          "verificationStatus" -> filters.verificationStatus.value,
          "taxTreatment"       -> filters.taxTreatment.value
        )
      )
    )
  }

  def onPageLoad(
    instanceId: String,
    mode: Mode,
    page: Int = 1
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      rowsFromUserAnswers(request.userAnswers) match {
        case Some(allRows) if allRows.nonEmpty =>
          renderPage(
            allRows,
            instanceId,
            mode,
            page,
            getListFilters(request)
          )

        case Some(_) =>
          Redirect(routes.NoSubcontractorsExistController.onPageLoad())

        case None =>
          Redirect(
            controllers.routes.JourneyRecoveryController.onPageLoad()
          )
      }
    }

  def onSubmit(
    instanceId: String,
    mode: Mode,
    page: Int = 1
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      rowsFromUserAnswers(request.userAnswers) match {
        case Some(allRows) =>
          val formData =
            request.body.asFormUrlEncoded.getOrElse(Map.empty)

          val filters =
            getFormFilters(formData)

          form
            .bindFromRequest()(request)
            .fold(
              formWithErrors =>
                renderInvalidForm(
                  formWithErrors,
                  allRows,
                  instanceId,
                  mode,
                  page,
                  filters
                ),
              searchTerm =>
                redirectToFilteredList(
                  instanceId,
                  mode,
                  getTargetPage(formData),
                  searchTerm,
                  filters
                )
            )

        case None =>
          Redirect(
            controllers.routes.JourneyRecoveryController.onPageLoad()
          )
      }
    }
}
