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

import base.SpecBase
import forms.subcontractors.SubcontractorsListFormProvider
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.subcontractors.TaxTreatment
import services.PaginationSubcontractorsListService
import viewmodels.subcontractors.{SubcontractorsListData, SubcontractorsListRow}
import views.html.subcontractors.SubcontractorsListView

class SubcontractorsListControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new SubcontractorsListFormProvider()
  private val form         = formProvider()

  private val instanceId = "test-instance-id"

  private def filterRows(
    searchTerm: String,
    verificationStatus: String,
    taxTreatment: String,
    sortBy: String = "name",
    sortOrder: String = "ascending"
  ): Seq[SubcontractorsListRow] = {

    val allRows = SubcontractorsListData.rows

    val searchFiltered =
      if (searchTerm.isEmpty) allRows
      else {
        val lowerSearch = searchTerm.toLowerCase
        allRows.filter { row =>
          row.name.toLowerCase.contains(lowerSearch)
        }
      }

    val verificationFiltered =
      verificationStatus match {
        case "verified" =>
          searchFiltered.filter(_.verified)

        case "notVerified" =>
          searchFiltered.filter(!_.verified)

        case _ =>
          searchFiltered
      }

    val taxFiltered =
      taxTreatment match {
        case "gross" =>
          verificationFiltered.filter(_.taxTreatment == TaxTreatment.Gross)

        case "higherRate" =>
          verificationFiltered.filter(_.taxTreatment == TaxTreatment.HigherRate)

        case "standardRate" =>
          verificationFiltered.filter(_.taxTreatment == TaxTreatment.StandardRate)

        case "unknown" =>
          verificationFiltered.filter(_.taxTreatment == TaxTreatment.Unknown)

        case _ =>
          verificationFiltered
      }

    sortBy match {
      case "dateAdded" =>
        val sortedRows = taxFiltered.sortBy(_.dateAdded)
        if (sortOrder == "descending") sortedRows.reverse else sortedRows

      case _ =>
        val noNameRows =
          taxFiltered.filter(_.name.trim.equalsIgnoreCase("No name provided"))

        val namedRows =
          taxFiltered.filterNot(_.name.trim.equalsIgnoreCase("No name provided"))

        val sortedNamedRows =
          namedRows.sortBy(_.name.trim.toLowerCase)

        val orderedNamedRows =
          if (sortOrder == "descending") sortedNamedRows.reverse else sortedNamedRows

        noNameRows ++ orderedNamedRows
    }
  }

  "SubcontractorsListController" - {

    "must return OK and the correct view for a GET with default filters" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, 1).url
          )

        val result = route(application, request).value

        val view              = application.injector.instanceOf[SubcontractorsListView]
        val paginationService = application.injector.instanceOf[PaginationSubcontractorsListService]

        val filteredRows = filterRows("", "all", "all")

        val paginationResult =
          paginationService.paginate(
            allItems = filteredRows,
            currentPage = 1,
            recordsPerPage = 8,
            baseUrl = routes.SubcontractorsListController.onPageLoad(instanceId).url,
            queryString = "sortBy=name&sortOrder=ascending"
          )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(""),
          paginationResult.items,
          paginationResult.pagination,
          paginationResult.currentPage,
          paginationResult.totalPages,
          paginationResult.startIndex,
          paginationResult.totalCount,
          instanceId,
          "",
          "all",
          "all",
          "name",
          "ascending"
        )(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET with search and filters applied" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, 1).url +
              "?searchTerm=Alan&verificationStatus=verified&taxTreatment=gross"
          )

        val result = route(application, request).value

        val view              = application.injector.instanceOf[SubcontractorsListView]
        val paginationService = application.injector.instanceOf[PaginationSubcontractorsListService]

        val searchTerm         = "Alan"
        val verificationStatus = "verified"
        val taxTreatment       = "gross"

        val filteredRows =
          filterRows(searchTerm, verificationStatus, taxTreatment)

        val queryString =
          Seq(
            Some("searchTerm=Alan"),
            Some("verificationStatus=verified"),
            Some("taxTreatment=gross"),
            Some("sortBy=name"),
            Some("sortOrder=ascending")
          ).flatten.mkString("&")

        val paginationResult =
          paginationService.paginate(
            allItems = filteredRows,
            currentPage = 1,
            recordsPerPage = 8,
            baseUrl = routes.SubcontractorsListController.onPageLoad(instanceId).url,
            queryString = queryString
          )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(searchTerm),
          paginationResult.items,
          paginationResult.pagination,
          paginationResult.currentPage,
          paginationResult.totalPages,
          paginationResult.startIndex,
          paginationResult.totalCount,
          instanceId,
          searchTerm,
          verificationStatus,
          taxTreatment,
          "name",
          "ascending"
        )(request, messages(application)).toString
      }
    }

    "must redirect to the selected page with filters preserved when pagination is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            routes.SubcontractorsListController.onSubmit(instanceId, 1).url
          ).withFormUrlEncodedBody(
            "gotoPage"           -> "2",
            "searchTerm"         -> "Alan",
            "verificationStatus" -> "verified",
            "taxTreatment"       -> "gross",
            "sortBy"             -> "name",
            "sortOrder"          -> "ascending"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        val redirectUrl = redirectLocation(result).value

        redirectUrl must include(routes.SubcontractorsListController.onPageLoad(instanceId, 2).url)
        redirectUrl must include("searchTerm=Alan")
        redirectUrl must include("verificationStatus=verified")
        redirectUrl must include("taxTreatment=gross")
        redirectUrl must include("sortBy=name")
        redirectUrl must include("sortOrder=ascending")
      }
    }

    "must redirect to page 1 when gotoPage is not submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            routes.SubcontractorsListController.onSubmit(instanceId, 1).url
          ).withFormUrlEncodedBody(
            "searchTerm"         -> "Alan",
            "verificationStatus" -> "verified",
            "taxTreatment"       -> "gross",
            "sortBy"             -> "name",
            "sortOrder"          -> "ascending"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        val redirectUrl = redirectLocation(result).value

        redirectUrl must include(routes.SubcontractorsListController.onPageLoad(instanceId, 1).url)
        redirectUrl must include("searchTerm=Alan")
        redirectUrl must include("verificationStatus=verified")
        redirectUrl must include("taxTreatment=gross")
        redirectUrl must include("sortBy=name")
        redirectUrl must include("sortOrder=ascending")
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, 1).url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            routes.SubcontractorsListController.onSubmit(instanceId, 1).url
          ).withFormUrlEncodedBody(
            "gotoPage"   -> "2",
            "searchTerm" -> "Alan"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
