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
import models.Mode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.PaginationSubcontractorsListService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.subcontractors.{SubcontractorsListData, SubcontractorsListRow}
import views.html.subcontractors.SubcontractorsListView
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


import javax.inject.Inject
import scala.concurrent.Future

class SubcontractorsListController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: SubcontractorsListFormProvider,
                                              paginationService: PaginationSubcontractorsListService,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: SubcontractorsListView
                                    ) extends FrontendBaseController with I18nSupport {

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


  def onPageLoad(instanceId: String, mode: Mode, page: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      println(s"REQUEST URI = ${request.uri}")
      println(s"instanceId = $instanceId")

      val searchTerm =
        request.getQueryString("searchTerm").getOrElse("").trim

      val filledForm =
        form.fill(searchTerm)

      val verificationStatus =
        request.getQueryString("verificationStatus").getOrElse("all")

      val taxTreatment =
        request.getQueryString("taxTreatment").getOrElse("all")

      val allRows = SubcontractorsListData.rows

      val searchFiltered =
        if (searchTerm.isEmpty) allRows
        else allRows.filter { row =>
          row.name.toLowerCase.contains(searchTerm.toLowerCase) ||
            row.utr.contains(searchTerm) ||
            row.verificationNumber.contains(searchTerm)
        }

      val verificationFiltered =
        verificationStatus match {
          case "verified" =>
            searchFiltered.filter(_.verified.equalsIgnoreCase("Yes"))

          case "notVerified" =>
            searchFiltered.filter(_.verified.equalsIgnoreCase("No"))

          case _ =>
            searchFiltered
        }

      val taxFiltered =
        taxTreatment match {
          case "gross" =>
            verificationFiltered.filter(_.taxTreatment.equalsIgnoreCase("Gross"))

          case "higherRate" =>
            verificationFiltered.filter(_.taxTreatment.equalsIgnoreCase("Higher rate"))

          case "standardRate" =>
            verificationFiltered.filter(_.taxTreatment.equalsIgnoreCase("Standard rate"))

          case "unknown" =>
            verificationFiltered.filter(_.taxTreatment.equalsIgnoreCase("Unknown"))

          case _ =>
            verificationFiltered
        }

      val sortedRows =
        taxFiltered.sortBy(_.name.toLowerCase)

      val queryString =
        Seq(
          Option(searchTerm).filter(_.nonEmpty).map("searchTerm=" + _),
          Option(verificationStatus).filter(_ != "all").map("verificationStatus=" + _),
          Option(taxTreatment).filter(_ != "all").map("taxTreatment=" + _)
        ).flatten.mkString("&")

      val result =
        paginationService.paginate(
          allItems = sortedRows,
          currentPage = page,
          recordsPerPage = 8,
          baseUrl = routes.SubcontractorsListController.onPageLoad(instanceId, mode).url,
          queryString = queryString
        )

      println(s"Search rows: ${searchFiltered.size}")
      println(s"Verification rows: ${verificationFiltered.size}")
      println(s"Tax rows: ${taxFiltered.size}")

      Ok(

        view(
          filledForm,
          mode,
          result.items,
          result.pagination,
          result.currentPage,
          result.totalPages,
          result.startIndex,
          result.totalCount,
          instanceId,
          searchTerm,
          verificationStatus,
          taxTreatment
        )
      )
    }


  def onSubmit(instanceId: String, mode: Mode, page: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val formData =
        request.body.asFormUrlEncoded.getOrElse(Map.empty)

      val gotoPage =
        formData
          .get("gotoPage")
          .flatMap(_.headOption)
          .flatMap(_.toIntOption)

      val searchTerm =
        formData.get("searchTerm").flatMap(_.headOption).getOrElse("").trim

      val verificationStatus =
        formData.get("verificationStatus").flatMap(_.headOption).getOrElse("all")

      val taxTreatment =
        formData.get("taxTreatment").flatMap(_.headOption).getOrElse("all")

      val targetPage =
        gotoPage.getOrElse(1)

      val baseUrl =
        routes.SubcontractorsListController
          .onPageLoad(instanceId, mode, targetPage)
          .url

      val redirectUrl =
        appendQueryParams(
          baseUrl,
          Seq(
            "searchTerm" -> searchTerm,
            "verificationStatus" -> verificationStatus,
            "taxTreatment" -> taxTreatment
          )
        )

      Future.successful(Redirect(redirectUrl))
    }
}
