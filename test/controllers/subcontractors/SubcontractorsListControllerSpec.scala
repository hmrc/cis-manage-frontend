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
import models.{Mode, NormalMode, UserAnswers}
import models.response.{GetSubcontractor, GetSubcontractorListResponse}
import pages.subcontractors.SubcontractorListPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.PaginationSubcontractorsListService
import viewmodels.subcontractors.{SubcontractorsListRow, TaxTreatment}
import views.html.subcontractors.SubcontractorsListView

import java.time.LocalDateTime

class SubcontractorsListControllerSpec extends SpecBase {

  private val formProvider = new SubcontractorsListFormProvider()
  private val form         = formProvider()

  private val instanceId = "test-instance-id"
  private val mode: Mode = NormalMode

  private val subcontractors = Seq(
    GetSubcontractor(
      subcontractorId = 1L,
      utr = Some("1234567890"),
      pageVisited = None,
      partnerUtr = None,
      crn = None,
      firstName = Some("Alan"),
      nino = None,
      secondName = None,
      surname = Some("Smith"),
      partnershipTradingName = None,
      tradingName = None,
      subcontractorType = Some("soleTrader"),
      addressLine1 = None,
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      country = None,
      postcode = None,
      emailAddress = None,
      phoneNumber = None,
      mobilePhoneNumber = None,
      worksReferenceNumber = None,
      createDate = Some(LocalDateTime.of(2026, 4, 6, 10, 0)),
      lastUpdate = None,
      subbieResourceRef = Some(10L),
      matched = None,
      autoVerified = None,
      verified = Some("Y"),
      verificationNumber = Some("V000001"),
      taxTreatment = Some("Gross"),
      verificationDate = None,
      version = None,
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = None
    ),
    GetSubcontractor(
      subcontractorId = 2L,
      utr = Some("9876543210"),
      pageVisited = None,
      partnerUtr = None,
      crn = None,
      firstName = Some("Brian"),
      nino = None,
      secondName = None,
      surname = Some("Jones"),
      partnershipTradingName = None,
      tradingName = None,
      subcontractorType = Some("soleTrader"),
      addressLine1 = None,
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      country = None,
      postcode = None,
      emailAddress = None,
      phoneNumber = None,
      mobilePhoneNumber = None,
      worksReferenceNumber = None,
      createDate = Some(LocalDateTime.of(2026, 5, 6, 10, 0)),
      lastUpdate = None,
      subbieResourceRef = Some(20L),
      matched = None,
      autoVerified = None,
      verified = Some("N"),
      verificationNumber = Some("V000002"),
      taxTreatment = Some("Higher Rate"),
      verificationDate = None,
      version = None,
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = None
    )
  )

  private val listResponse = GetSubcontractorListResponse(
    subcontractors = subcontractors
  )

  private val rows = Seq(
    SubcontractorsListRow(
      id = "1",
      name = "Alan Smith",
      utr = "1234567890",
      verified = true,
      verificationNumber = "V000001",
      taxTreatment = TaxTreatment.Gross,
      dateAdded = "6 Apr 2026",
      subbieResourceRef = 10L
    ),
    SubcontractorsListRow(
      id = "2",
      name = "Brian Jones",
      utr = "9876543210",
      verified = false,
      verificationNumber = "V000002",
      taxTreatment = TaxTreatment.HigherRate,
      dateAdded = "6 May 2026",
      subbieResourceRef = 20L
    )
  )

  private def userAnswersWithSubcontractors: UserAnswers =
    emptyUserAnswers
      .set(SubcontractorListPage, listResponse)
      .success
      .value

  private def filterRows(
    searchTerm: String,
    verificationStatus: String,
    taxTreatment: String,
    sortBy: String = "name",
    sortOrder: String = "ascending"
  ): Seq[SubcontractorsListRow] = {
    val searchFiltered =
      filterBySearchTerm(rows, searchTerm)

    val verificationFiltered =
      filterByVerificationStatus(searchFiltered, verificationStatus)

    val taxTreatmentFiltered =
      filterByTaxTreatment(verificationFiltered, taxTreatment)

    sortRows(taxTreatmentFiltered, sortBy, sortOrder)
  }

  private def filterBySearchTerm(
    sourceRows: Seq[SubcontractorsListRow],
    searchTerm: String
  ): Seq[SubcontractorsListRow] = {
    val trimmedSearchTerm =
      searchTerm.trim

    if (trimmedSearchTerm.isEmpty) {
      sourceRows
    } else {
      val lowerSearch =
        trimmedSearchTerm.toLowerCase

      sourceRows.filter { row =>
        row.name.toLowerCase.contains(lowerSearch) ||
        row.utr.contains(trimmedSearchTerm) ||
        row.verificationNumber.contains(trimmedSearchTerm)
      }
    }
  }

  private def filterByVerificationStatus(
    sourceRows: Seq[SubcontractorsListRow],
    verificationStatus: String
  ): Seq[SubcontractorsListRow] =
    verificationStatus match {
      case "verified" =>
        sourceRows.filter(_.verified)

      case "notVerified" =>
        sourceRows.filterNot(_.verified)

      case _ =>
        sourceRows
    }

  private def filterByTaxTreatment(
    sourceRows: Seq[SubcontractorsListRow],
    taxTreatment: String
  ): Seq[SubcontractorsListRow] =
    taxTreatment match {
      case "gross" =>
        sourceRows.filter(_.taxTreatment == TaxTreatment.Gross)

      case "higherRate" =>
        sourceRows.filter(_.taxTreatment == TaxTreatment.HigherRate)

      case "standardRate" =>
        sourceRows.filter(_.taxTreatment == TaxTreatment.StandardRate)

      case "unknown" =>
        sourceRows.filter(_.taxTreatment == TaxTreatment.Unknown)

      case _ =>
        sourceRows
    }

  private def sortRows(
    sourceRows: Seq[SubcontractorsListRow],
    sortBy: String,
    sortOrder: String
  ): Seq[SubcontractorsListRow] =
    sortBy match {
      case "dateAdded" =>
        val sortedRows =
          sourceRows.sortBy(_.dateAdded)

        if (sortOrder == "descending") {
          sortedRows.reverse
        } else {
          sortedRows
        }

      case _ =>
        val noNameRows =
          sourceRows.filter(_.name.trim.equalsIgnoreCase("No name provided"))

        val namedRows =
          sourceRows.filterNot(_.name.trim.equalsIgnoreCase("No name provided"))

        val sortedNamedRows =
          namedRows.sortBy(_.name.trim.toLowerCase)

        val orderedNamedRows =
          if (sortOrder == "descending") {
            sortedNamedRows.reverse
          } else {
            sortedNamedRows
          }

        noNameRows ++ orderedNamedRows
    }

  "SubcontractorsListController" - {

    "must return OK and the correct view for a GET with default filters" in {
      val application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithSubcontractors)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, mode, 1).url
          )

        val result =
          route(application, request).value

        val view =
          application.injector.instanceOf[SubcontractorsListView]

        val paginationService =
          application.injector.instanceOf[PaginationSubcontractorsListService]

        val paginationResult =
          paginationService.paginate(
            allItems = filterRows("", "all", "all"),
            currentPage = 1,
            recordsPerPage = 8,
            baseUrl = routes.SubcontractorsListController.onPageLoad(instanceId, mode).url,
            queryString = "sortBy=name&sortOrder=ascending"
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
          "all",
          "name",
          "ascending"
        )(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET with search and filters applied" in {
      val application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithSubcontractors)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, mode, 1).url +
              "?searchTerm=Alan&verificationStatus=verified&taxTreatment=gross"
          )

        val result =
          route(application, request).value

        val view =
          application.injector.instanceOf[SubcontractorsListView]

        val paginationService =
          application.injector.instanceOf[PaginationSubcontractorsListService]

        val paginationResult =
          paginationService.paginate(
            allItems = filterRows("Alan", "verified", "gross"),
            currentPage = 1,
            recordsPerPage = 8,
            baseUrl = routes.SubcontractorsListController.onPageLoad(instanceId, mode).url,
            queryString =
              "searchTerm=Alan&verificationStatus=verified&taxTreatment=gross&sortBy=name&sortOrder=ascending"
          )

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form.fill("Alan"),
          mode,
          paginationResult.items,
          paginationResult.pagination,
          paginationResult.currentPage,
          paginationResult.totalPages,
          paginationResult.startIndex,
          paginationResult.totalCount,
          instanceId,
          "Alan",
          "verified",
          "gross",
          "name",
          "ascending"
        )(request, messages(application)).toString
      }
    }

    "must redirect to the selected page with filters preserved when pagination is submitted" in {
      val application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithSubcontractors)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.SubcontractorsListController.onSubmit(instanceId, mode, 1).url
          ).withFormUrlEncodedBody(
            "gotoPage"           -> "2",
            "searchTerm"         -> "Alan",
            "verificationStatus" -> "verified",
            "taxTreatment"       -> "gross",
            "sortBy"             -> "name",
            "sortOrder"          -> "ascending"
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        val redirectUrl =
          redirectLocation(result).value

        redirectUrl must include(
          routes.SubcontractorsListController.onPageLoad(instanceId, mode, 2).url
        )
        redirectUrl must include("searchTerm=Alan")
        redirectUrl must include("verificationStatus=verified")
        redirectUrl must include("taxTreatment=gross")
        redirectUrl must include("sortBy=name")
        redirectUrl must include("sortOrder=ascending")
      }
    }

    "must redirect to page 1 when gotoPage is not submitted" in {
      val application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithSubcontractors)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.SubcontractorsListController.onSubmit(instanceId, mode, 1).url
          ).withFormUrlEncodedBody(
            "searchTerm"         -> "Alan",
            "verificationStatus" -> "verified",
            "taxTreatment"       -> "gross",
            "sortBy"             -> "name",
            "sortOrder"          -> "ascending"
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        val redirectUrl =
          redirectLocation(result).value

        redirectUrl must include(
          routes.SubcontractorsListController.onPageLoad(instanceId, mode, 1).url
        )
        redirectUrl must include("searchTerm=Alan")
        redirectUrl must include("verificationStatus=verified")
        redirectUrl must include("taxTreatment=gross")
        redirectUrl must include("sortBy=name")
        redirectUrl must include("sortOrder=ascending")
      }
    }

    "must redirect to Journey Recovery for a GET when subcontractor list data is missing" in {
      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, mode, 1).url
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when subcontractor list data is missing" in {
      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.SubcontractorsListController.onSubmit(instanceId, mode, 1).url
          ).withFormUrlEncodedBody(
            "gotoPage"   -> "2",
            "searchTerm" -> "Alan"
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to no subcontractors exist when the stored list is empty" in {
      val application =
        applicationBuilder(
          userAnswers = Some(
            emptyUserAnswers
              .set(SubcontractorListPage, GetSubcontractorListResponse(Seq.empty))
              .success
              .value
          )
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, mode, 1).url
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.NoSubcontractorsExistController.onPageLoad().url
      }
    }

    "must throw an exception when subbieResourceRef is missing" in {
      val response =
        GetSubcontractorListResponse(
          subcontractors = Seq(
            subcontractors.head.copy(
              subbieResourceRef = None
            )
          )
        )

      val userAnswers =
        emptyUserAnswers
          .set(SubcontractorListPage, response)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.SubcontractorsListController.onPageLoad(instanceId, mode, 1).url
          )

        val result =
          route(application, request).value

        val exception =
          intercept[IllegalStateException] {
            await(result)
          }

        exception.getMessage mustEqual
          "Missing subbieResourceRef for subcontractorId 1"
      }
    }
  }
}
