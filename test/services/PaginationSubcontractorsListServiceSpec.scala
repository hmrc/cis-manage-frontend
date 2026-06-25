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

package services

import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.wordspec.AnyWordSpec
import viewmodels.govuk.PaginationFluency._

class PaginationSubcontractorsListServiceSpec extends AnyWordSpec with Matchers {

  private val service = new PaginationSubcontractorsListService

  "PaginationSubcontractorsListService" should {

    "return the first page of items with correct metadata" in {
      val items = (1 to 20).map(_.toString)

      val result =
        service.paginate(
          allItems = items,
          currentPage = 1,
          recordsPerPage = 8,
          baseUrl = "/subcontractors/test/your-subcontractors"
        )

      result.items mustBe Seq("1", "2", "3", "4", "5", "6", "7", "8")
      result.currentPage mustBe 1
      result.totalPages mustBe 3
      result.startIndex mustBe 1
      result.totalCount mustBe 20

      result.pagination.items.nonEmpty mustBe true
      result.pagination.previous mustBe None
      result.pagination.next mustBe defined
      result.pagination.next.value.href must include("page=2")
    }

    "return the second page of items with correct metadata" in {
      val items = (1 to 20).map(_.toString)

      val result =
        service.paginate(
          allItems = items,
          currentPage = 2,
          recordsPerPage = 8,
          baseUrl = "/subcontractors/test/your-subcontractors"
        )

      result.items mustBe Seq("9", "10", "11", "12", "13", "14", "15", "16")
      result.currentPage mustBe 2
      result.totalPages mustBe 3
      result.startIndex mustBe 9
      result.totalCount mustBe 20

      result.pagination.previous mustBe defined
      result.pagination.previous.value.href must include("page=1")

      result.pagination.next mustBe defined
      result.pagination.next.value.href must include("page=3")
    }

    "return the final page of items with correct metadata" in {
      val items = (1 to 20).map(_.toString)

      val result =
        service.paginate(
          allItems = items,
          currentPage = 3,
          recordsPerPage = 8,
          baseUrl = "/subcontractors/test/your-subcontractors"
        )

      result.items mustBe Seq("17", "18", "19", "20")
      result.currentPage mustBe 3
      result.totalPages mustBe 3
      result.startIndex mustBe 17
      result.totalCount mustBe 20

      result.pagination.previous mustBe defined
      result.pagination.previous.value.href must include("page=2")

      result.pagination.next mustBe None
    }

    "clamp currentPage to 1 when page is less than 1" in {
      val items = (1 to 20).map(_.toString)

      val result =
        service.paginate(
          allItems = items,
          currentPage = 0,
          recordsPerPage = 8,
          baseUrl = "/subcontractors/test/your-subcontractors"
        )

      result.currentPage mustBe 1
      result.items mustBe Seq("1", "2", "3", "4", "5", "6", "7", "8")
    }

    "clamp currentPage to totalPages when page exceeds total pages" in {
      val items = (1 to 20).map(_.toString)

      val result =
        service.paginate(
          allItems = items,
          currentPage = 99,
          recordsPerPage = 8,
          baseUrl = "/subcontractors/test/your-subcontractors"
        )

      result.currentPage mustBe 3
      result.items mustBe Seq("17", "18", "19", "20")
    }

    "return empty pagination view model when there is only one page" in {
      val items = (1 to 5).map(_.toString)

      val result =
        service.paginate(
          allItems = items,
          currentPage = 1,
          recordsPerPage = 8,
          baseUrl = "/subcontractors/test/your-subcontractors"
        )

      result.items mustBe Seq("1", "2", "3", "4", "5")
      result.totalPages mustBe 1
      result.pagination.items mustBe empty
      result.pagination.previous mustBe None
      result.pagination.next mustBe None
    }

    "return startIndex as 0 when there are no items" in {
      val result =
        service.paginate(
          allItems = Seq.empty[String],
          currentPage = 1,
          recordsPerPage = 8,
          baseUrl = "/subcontractors/test/your-subcontractors"
        )

      result.items mustBe empty
      result.currentPage mustBe 1
      result.totalPages mustBe 1
      result.startIndex mustBe 0
      result.totalCount mustBe 0
      result.pagination.items mustBe empty
    }

    "include query string in pagination URLs" in {
      val items = (1 to 20).map(_.toString)

      val result =
        service.paginate(
          allItems = items,
          currentPage = 2,
          recordsPerPage = 8,
          baseUrl = "/subcontractors/test/your-subcontractors",
          queryString = "searchTerm=Alan&verificationStatus=verified&taxTreatment=gross"
        )

      result.pagination.previous mustBe defined
      result.pagination.next mustBe defined

      result.pagination.previous.value.href must include("page=1")
      result.pagination.previous.value.href must include("searchTerm=Alan")
      result.pagination.previous.value.href must include("verificationStatus=verified")
      result.pagination.previous.value.href must include("taxTreatment=gross")

      result.pagination.next.value.href must include("page=3")
      result.pagination.next.value.href must include("searchTerm=Alan")
      result.pagination.next.value.href must include("verificationStatus=verified")
      result.pagination.next.value.href must include("taxTreatment=gross")
    }

    "show all page numbers when totalPages is 5 or less" in {
      val items = (1 to 40).map(_.toString) // 5 pages with 8 per page

      val result =
        service.paginate(
          allItems = items,
          currentPage = 3,
          recordsPerPage = 8,
          baseUrl = "/subcontractors/test/your-subcontractors"
        )

      val pageNumbers =
        result.pagination.items.filterNot(_.ellipsis).map(_.number)

      pageNumbers mustBe Seq("1", "2", "3", "4", "5")
    }

    "show leading pages and trailing ellipsis when current page is 3 or less and totalPages is greater than 5" in {
      val items = (1 to 80).map(_.toString) // 10 pages

      val result =
        service.paginate(
          allItems = items,
          currentPage = 2,
          recordsPerPage = 8,
          baseUrl = "/subcontractors/test/your-subcontractors"
        )

      val paginationItems = result.pagination.items

      paginationItems.map(_.number) mustBe Seq("1", "2", "3", "4", "", "10")
      paginationItems.count(_.ellipsis) mustBe 1
    }

    "show middle pagination with ellipsis on both sides when current page is in the middle" in {
      val items = (1 to 80).map(_.toString) // 10 pages

      val result =
        service.paginate(
          allItems = items,
          currentPage = 5,
          recordsPerPage = 8,
          baseUrl = "/subcontractors/test/your-subcontractors"
        )

      val paginationItems = result.pagination.items

      paginationItems.map(_.number) mustBe Seq("1", "", "4", "5", "6", "", "10")
      paginationItems.count(_.ellipsis) mustBe 2
    }

    "show trailing pages with leading ellipsis when current page is near the end" in {
      val items = (1 to 80).map(_.toString) // 10 pages

      val result =
        service.paginate(
          allItems = items,
          currentPage = 9,
          recordsPerPage = 8,
          baseUrl = "/subcontractors/test/your-subcontractors"
        )

      val paginationItems = result.pagination.items

      paginationItems.map(_.number) mustBe Seq("1", "", "7", "8", "9", "10")
      paginationItems.count(_.ellipsis) mustBe 1
    }

    "mark the current page item as current" in {
      val items = (1 to 80).map(_.toString)

      val result =
        service.paginate(
          allItems = items,
          currentPage = 5,
          recordsPerPage = 8,
          baseUrl = "/subcontractors/test/your-subcontractors"
        )

      val currentItems =
        result.pagination.items.filter(_.current)

      currentItems.size mustBe 1
      currentItems.head.number mustBe "5"
    }
  }
}
