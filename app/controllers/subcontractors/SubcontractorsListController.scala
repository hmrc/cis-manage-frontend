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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.PaginationSubcontractorsListService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.subcontractors.*
import views.html.subcontractors.SubcontractorsListView
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import scala.util.Try

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

  val form = formProvider()

  private def encode(value: String): String =
    URLEncoder.encode(value, StandardCharsets.UTF_8.toString)

  private def appendQueryParams(url: String, params: Seq[(String, String)]): String = {
    val filteredParams =
      params.collect {
        case (key, value) if value.nonEmpty => s"$key=${encode(value)}"
      }

    if (filteredParams.isEmpty) url
    else {
      val separator = if (url.contains("?")) "&" else "?"
      s"$url$separator${filteredParams.mkString("&")}"
    }
  }

  private val SortByName      = "name"
  private val SortByDateAdded = "dateAdded"

  private val SortOrderAsc  = "ascending"
  private val SortOrderDesc = "descending"

  private def normalisedSortBy(value: Option[String]): String =
    value match {
      case Some(SortByDateAdded) => SortByDateAdded
      case _                     => SortByName
    }

  private def normalisedSortOrder(value: Option[String]): String =
    value match {
      case Some(SortOrderDesc) | Some("descending") => SortOrderDesc
      case Some(SortOrderAsc) | Some("ascending")   => SortOrderAsc
      case _                                        => SortOrderAsc
    }

  private val dateFormatters: Seq[DateTimeFormatter] =
    Seq(
      DateTimeFormatter.ISO_LOCAL_DATE,
      DateTimeFormatter.ofPattern("d MMM yyyy", Locale.UK),
      DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK),
      DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.UK),
      DateTimeFormatter.ofPattern("d/M/yyyy", Locale.UK)
    )

  private def parseDateAdded(value: String): LocalDate =
    dateFormatters.view
      .flatMap(formatter => Try(LocalDate.parse(value.trim, formatter)).toOption)
      .headOption
      .getOrElse(LocalDate.MIN)

  private def sortRows(
    rows: Seq[SubcontractorsListRow],
    sortBy: String,
    sortOrder: String
  ): Seq[SubcontractorsListRow] =
    sortBy match {

      case SortByDateAdded =>
        val sortedRows =
          rows.sortBy(row => parseDateAdded(row.dateAdded))

        if (sortOrder == SortOrderDesc) sortedRows.reverse
        else sortedRows

      case SortByName =>
        val noNameRows =
          rows.filter(row => row.name.trim.equalsIgnoreCase("No name provided"))

        val namedRows =
          rows.filterNot(row => row.name.trim.equalsIgnoreCase("No name provided"))

        val sortedNamedRows =
          namedRows.sortBy(row => row.name.trim.toLowerCase(Locale.UK))

        val orderedNamedRows =
          if (sortOrder == SortOrderDesc) sortedNamedRows.reverse
          else sortedNamedRows

        noNameRows ++ orderedNamedRows

      case _ =>
        rows
    }

  private def buildQueryString(
    searchTerm: String,
    verificationStatus: VerificationStatusFilter,
    taxTreatment: TaxTreatmentFilter,
    sortBy: String,
    sortOrder: String
  ): String =
    Seq(
      Option(searchTerm).filter(_.nonEmpty).map(v => "searchTerm=" + encode(v)),
      Option(verificationStatus.value)
        .filter(_ != VerificationStatusFilter.All.value)
        .map(v => "verificationStatus=" + encode(v)),
      Option(taxTreatment.value)
        .filter(_ != TaxTreatmentFilter.All.value)
        .map(v => "taxTreatment=" + encode(v)),
      Some("sortBy=" + encode(sortBy)),
      Some("sortOrder=" + encode(sortOrder))
    ).flatten.mkString("&")

  private def matchesSearchTerm(row: SubcontractorsListRow, searchTerm: String): Boolean = {
    val normalisedSearchTerm = searchTerm.trim.toLowerCase

    row.name.toLowerCase.contains(normalisedSearchTerm)
  }

  def onPageLoad(instanceId: String, page: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      val searchTerm =
        request.getQueryString("searchTerm").getOrElse("").trim

      val filledForm =
        form.fill(searchTerm)

      val verificationStatus =
        VerificationStatusFilter.fromString(
          request.getQueryString("verificationStatus").getOrElse(VerificationStatusFilter.All.value)
        )

      val taxTreatment =
        TaxTreatmentFilter.fromString(
          request.getQueryString("taxTreatment").getOrElse(TaxTreatmentFilter.All.value)
        )

      val sortBy =
        normalisedSortBy(request.getQueryString("sortBy"))

      val sortOrder =
        normalisedSortOrder(request.getQueryString("sortOrder"))

      val allRows = SubcontractorsListData.rows

      val searchFiltered =
        if (searchTerm.isEmpty) allRows
        else allRows.filter(row => matchesSearchTerm(row, searchTerm))

      val verificationFiltered =
        verificationStatus match {
          case VerificationStatusFilter.Verified =>
            searchFiltered.filter(_.verified)

          case VerificationStatusFilter.NotVerified =>
            searchFiltered.filter(!_.verified)

          case VerificationStatusFilter.All =>
            searchFiltered
        }

      val taxFiltered =
        taxTreatment match {
          case TaxTreatmentFilter.Gross =>
            verificationFiltered.filter(_.taxTreatment == TaxTreatment.Gross)

          case TaxTreatmentFilter.HigherRate =>
            verificationFiltered.filter(_.taxTreatment == TaxTreatment.HigherRate)

          case TaxTreatmentFilter.StandardRate =>
            verificationFiltered.filter(_.taxTreatment == TaxTreatment.StandardRate)

          case TaxTreatmentFilter.Unknown =>
            verificationFiltered.filter(_.taxTreatment == TaxTreatment.Unknown)

          case TaxTreatmentFilter.All =>
            verificationFiltered
        }

      val sortedRows =
        sortRows(taxFiltered, sortBy, sortOrder)

      val queryString =
        buildQueryString(
          searchTerm,
          verificationStatus,
          taxTreatment,
          sortBy,
          sortOrder
        )

      val result =
        paginationService.paginate(
          allItems = sortedRows,
          currentPage = page,
          recordsPerPage = SubcontractorsListConstants.RecordsPerPage,
          baseUrl = routes.SubcontractorsListController.onPageLoad(instanceId).url,
          queryString = queryString
        )

      Ok(
        view(
          filledForm,
          result.items,
          result.pagination,
          result.currentPage,
          result.totalPages,
          result.startIndex,
          result.totalCount,
          instanceId,
          searchTerm,
          verificationStatus.value,
          taxTreatment.value,
          sortBy,
          sortOrder
        )
      )
    }

  def onSubmit(instanceId: String, page: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {

            val formData =
              request.body.asFormUrlEncoded.getOrElse(Map.empty)

            val searchTerm =
              formData.get("searchTerm").flatMap(_.headOption).getOrElse("").trim

            val verificationStatus =
              VerificationStatusFilter.fromString(
                formData.get("verificationStatus").flatMap(_.headOption).getOrElse(VerificationStatusFilter.All.value)
              )

            val taxTreatment =
              TaxTreatmentFilter.fromString(
                formData.get("taxTreatment").flatMap(_.headOption).getOrElse(TaxTreatmentFilter.All.value)
              )

            val sortBy =
              normalisedSortBy(formData.get("sortBy").flatMap(_.headOption))

            val sortOrder =
              normalisedSortOrder(formData.get("sortOrder").flatMap(_.headOption))

            val allRows = SubcontractorsListData.rows

            val hasSearchTermError =
              formWithErrors.error("searchTerm").isDefined

            val searchFiltered =
              if (hasSearchTermError) {
                allRows
              } else if (searchTerm.isEmpty) {
                allRows
              } else {
                allRows.filter(row => matchesSearchTerm(row, searchTerm))
              }

            val verificationFiltered =
              verificationStatus match {
                case VerificationStatusFilter.Verified =>
                  searchFiltered.filter(_.verified)

                case VerificationStatusFilter.NotVerified =>
                  searchFiltered.filter(!_.verified)

                case VerificationStatusFilter.All =>
                  searchFiltered
              }

            val taxFiltered =
              taxTreatment match {
                case TaxTreatmentFilter.Gross =>
                  verificationFiltered.filter(_.taxTreatment == TaxTreatment.Gross)

                case TaxTreatmentFilter.HigherRate =>
                  verificationFiltered.filter(_.taxTreatment == TaxTreatment.HigherRate)

                case TaxTreatmentFilter.StandardRate =>
                  verificationFiltered.filter(_.taxTreatment == TaxTreatment.StandardRate)

                case TaxTreatmentFilter.Unknown =>
                  verificationFiltered.filter(_.taxTreatment == TaxTreatment.Unknown)

                case TaxTreatmentFilter.All =>
                  verificationFiltered
              }

            val sortedRows =
              sortRows(taxFiltered, sortBy, sortOrder)

            val queryString =
              buildQueryString(
                searchTerm,
                verificationStatus,
                taxTreatment,
                sortBy,
                sortOrder
              )

            val result =
              paginationService.paginate(
                allItems = sortedRows,
                currentPage = page,
                recordsPerPage = SubcontractorsListConstants.RecordsPerPage,
                baseUrl = routes.SubcontractorsListController.onPageLoad(instanceId).url,
                queryString = queryString
              )

            BadRequest(
              view(
                formWithErrors,
                result.items,
                result.pagination,
                result.currentPage,
                result.totalPages,
                result.startIndex,
                result.totalCount,
                instanceId,
                searchTerm,
                verificationStatus.value,
                taxTreatment.value,
                sortBy,
                sortOrder
              )
            )
          },
          searchTerm => {

            val formData =
              request.body.asFormUrlEncoded.getOrElse(Map.empty)

            val gotoPage =
              formData
                .get("gotoPage")
                .flatMap(_.headOption)
                .flatMap(_.toIntOption)

            val verificationStatus =
              VerificationStatusFilter.fromString(
                formData.get("verificationStatus").flatMap(_.headOption).getOrElse(VerificationStatusFilter.All.value)
              )

            val taxTreatment =
              TaxTreatmentFilter.fromString(
                formData.get("taxTreatment").flatMap(_.headOption).getOrElse(TaxTreatmentFilter.All.value)
              )

            val sortBy =
              normalisedSortBy(formData.get("sortBy").flatMap(_.headOption))

            val sortOrder =
              normalisedSortOrder(formData.get("sortOrder").flatMap(_.headOption))

            val targetPage =
              gotoPage.getOrElse(1)

            val baseUrl =
              routes.SubcontractorsListController
                .onPageLoad(instanceId, targetPage)
                .url

            val redirectUrl =
              appendQueryParams(
                baseUrl,
                Seq(
                  "searchTerm"         -> searchTerm.trim,
                  "verificationStatus" -> verificationStatus.value,
                  "taxTreatment"       -> taxTreatment.value,
                  "sortBy"             -> sortBy,
                  "sortOrder"          -> sortOrder
                )
              )

            Redirect(redirectUrl)
          }
        )
    }
}
