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
import models.{Mode, NormalMode}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.PaginationSubcontractorsListService
import viewmodels.subcontractors.{SubcontractorsListData, SubcontractorsListRow}
import views.html.subcontractors.SubcontractorsListView

class SubcontractorsListControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new SubcontractorsListFormProvider()
  private val form         = formProvider()

  private val instanceId = "test-instance-id"
  private val mode: Mode = NormalMode

  private def filterRows(
    searchTerm: String,
    verificationStatus: String,
    taxTreatment: String
  ): Seq[SubcontractorsListRow] = {

    val allRows = SubcontractorsListData.rows

    val searchFiltered =
      if (searchTerm.isEmpty) allRows
      else {
        val lowerSearch = searchTerm.toLowerCase
        allRows.filter { row =>
          row.name.toLowerCase.contains(lowerSearch) ||
          row.utr.contains(searchTerm) ||
          row.verificationNumber.contains(searchTerm)
        }
      }

    val verificationFiltered =
      verificationStatus match {
        case "verified"    =>
          searchFiltered.filter(_.verified.equalsIgnoreCase("Yes"))
        case "notVerified" =>
          searchFiltered.filter(_.verified.equalsIgnoreCase("No"))
        case _             =>
          searchFiltered
      }

    val taxFiltered =
      taxTreatment match {
        case "gross"        =>
          verificationFiltered.filter(_.taxTreatment.equalsIgnoreCase("Gross"))
        case "higherRate"   =>
          verificationFiltered.filter(_.taxTreatment.equalsIgnoreCase("Higher rate"))
        case "standardRate" =>
          verificationFiltered.filter(_.taxTreatment.equalsIgnoreCase("Standard rate"))
        case "unknown"      =>
          verificationFiltered.filter(_.taxTreatment.equalsIgnoreCase("Unknown"))
        case _              =>
          verificationFiltered
      }

    taxFiltered.sortBy(_.name.toLowerCase)
  }

  "SubcontractorsListController" - {

    "must return OK and the correct view for a GET with default filters" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, mode, 1).url
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
            baseUrl = routes.SubcontractorsListController.onPageLoad(instanceId, mode).url,
            queryString = ""
          )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(""),
          mode,
          paginationResult.items,
          paginationResult.pagination,
          paginationResult.currentPage,
          paginationResult.totalPages,
          paginationResult.startIndex,
          paginationResult.totalCount,
          instanceId,
          "",
          "all",
          "all"
        )(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET with search and filters applied" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, mode, 1).url +
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
            Some("taxTreatment=gross")
          ).flatten.mkString("&")

        val paginationResult =
          paginationService.paginate(
            allItems = filteredRows,
            currentPage = 1,
            recordsPerPage = 8,
            baseUrl = routes.SubcontractorsListController.onPageLoad(instanceId, mode).url,
            queryString = queryString
          )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(searchTerm),
          mode,
          paginationResult.items,
          paginationResult.pagination,
          paginationResult.currentPage,
          paginationResult.totalPages,
          paginationResult.startIndex,
          paginationResult.totalCount,
          instanceId,
          searchTerm,
          verificationStatus,
          taxTreatment
        )(request, messages(application)).toString
      }
    }

    "must redirect to the selected page with filters preserved when pagination is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            routes.SubcontractorsListController.onSubmit(instanceId, mode, 1).url
          ).withFormUrlEncodedBody(
            "gotoPage"           -> "2",
            "searchTerm"         -> "Alan",
            "verificationStatus" -> "verified",
            "taxTreatment"       -> "gross"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        val redirectUrl = redirectLocation(result).value

        redirectUrl must include(routes.SubcontractorsListController.onPageLoad(instanceId, mode, 2).url)
        redirectUrl must include("searchTerm=Alan")
        redirectUrl must include("verificationStatus=verified")
        redirectUrl must include("taxTreatment=gross")
      }
    }

    "must redirect to page 1 when gotoPage is not submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            routes.SubcontractorsListController.onSubmit(instanceId, mode, 1).url
          ).withFormUrlEncodedBody(
            "searchTerm"         -> "Alan",
            "verificationStatus" -> "verified",
            "taxTreatment"       -> "gross"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        val redirectUrl = redirectLocation(result).value

        redirectUrl must include(routes.SubcontractorsListController.onPageLoad(instanceId, mode, 1).url)
        redirectUrl must include("searchTerm=Alan")
        redirectUrl must include("verificationStatus=verified")
        redirectUrl must include("taxTreatment=gross")
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, mode, 1).url
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
            routes.SubcontractorsListController.onSubmit(instanceId, mode, 1).url
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
