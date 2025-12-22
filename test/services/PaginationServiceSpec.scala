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

package services

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import viewmodels.agent.{ClientListViewModel, ClientStatus}

class PaginationServiceSpec extends AnyWordSpec with Matchers {

  private val service = new PaginationService()
  private val baseUrl = "/test-url"

  private def createClient(id: String, name: String): ClientListViewModel =
    ClientListViewModel(
      uniqueId = id,
      clientName = name,
      employerReference = s"123/AB$id",
      clientReference = s"REF-$id",
      clientStatus = ClientStatus.Active
    )

  "PaginationService.paginateClientList" should {

    "return empty pagination when there are no clients" in {
      val result = service.paginateClientList(Seq.empty, 1, baseUrl)

      result.paginatedData mustBe empty
      result.totalRecords mustBe 0
      result.currentPage mustBe 1
      result.totalPages mustBe 0
      result.paginationViewModel.items mustBe empty
      result.paginationViewModel.previous mustBe None
      result.paginationViewModel.next mustBe None
    }

    "return empty pagination when there are 10 or fewer clients" in {
      val clients = (1 to 10).map(i => createClient(s"$i", s"Client $i"))
      val result  = service.paginateClientList(clients, 1, baseUrl)

      result.paginatedData mustBe clients
      result.totalRecords mustBe 10
      result.currentPage mustBe 1
      result.totalPages mustBe 1
      result.paginationViewModel.items mustBe empty
      result.paginationViewModel.previous mustBe None
      result.paginationViewModel.next mustBe None
    }

    "paginate 11 clients into 2 pages" in {
      val clients = (1 to 11).map(i => createClient(s"$i", s"Client $i"))
      val result  = service.paginateClientList(clients, 1, baseUrl)

      result.paginatedData.length mustBe 10
      result.totalRecords mustBe 11
      result.currentPage mustBe 1
      result.totalPages mustBe 2
      result.paginationViewModel.items.length mustBe 2
      result.paginationViewModel.previous mustBe None
      result.paginationViewModel.next.isDefined mustBe true
    }

    "return correct page for page 2" in {
      val clients = (1 to 25).map(i => createClient(s"$i", s"Client $i"))
      val result  = service.paginateClientList(clients, 2, baseUrl)

      result.paginatedData.length mustBe 10
      result.paginatedData.head.uniqueId mustBe "11"
      result.totalRecords mustBe 25
      result.currentPage mustBe 2
      result.totalPages mustBe 3
      result.paginationViewModel.previous.isDefined mustBe true
      result.paginationViewModel.next.isDefined mustBe true
    }

    "return last page correctly when not full" in {
      val clients = (1 to 25).map(i => createClient(s"$i", s"Client $i"))
      val result  = service.paginateClientList(clients, 3, baseUrl)

      result.paginatedData.length mustBe 5
      result.paginatedData.head.uniqueId mustBe "21"
      result.totalRecords mustBe 25
      result.currentPage mustBe 3
      result.totalPages mustBe 3
      result.paginationViewModel.previous.isDefined mustBe true
      result.paginationViewModel.next mustBe None
    }

    "validate and clamp currentPage to valid range" in {
      val clients = (1 to 25).map(i => createClient(s"$i", s"Client $i"))

      val resultPage0 = service.paginateClientList(clients, 0, baseUrl)
      resultPage0.currentPage mustBe 1

      val resultPageNegative = service.paginateClientList(clients, -5, baseUrl)
      resultPageNegative.currentPage mustBe 1

      val resultPageTooHigh = service.paginateClientList(clients, 100, baseUrl)
      resultPageTooHigh.currentPage mustBe 3
    }

    "generate correct pagination items for first page" in {
      val clients = (1 to 50).map(i => createClient(s"$i", s"Client $i"))
      val result  = service.paginateClientList(clients, 1, baseUrl)

      result.paginationViewModel.items.head.number mustBe "1"
      result.paginationViewModel.items.head.current mustBe true
      result.paginationViewModel.previous mustBe None
      result.paginationViewModel.next.isDefined mustBe true
    }

    "generate correct pagination items for middle page" in {
      val clients = (1 to 50).map(i => createClient(s"$i", s"Client $i"))
      val result  = service.paginateClientList(clients, 3, baseUrl)

      result.paginationViewModel.items.exists(_.current) mustBe true
      result.paginationViewModel.previous.isDefined mustBe true
      result.paginationViewModel.next.isDefined mustBe true
    }

    "generate correct pagination items for last page" in {
      val clients = (1 to 50).map(i => createClient(s"$i", s"Client $i"))
      val result  = service.paginateClientList(clients, 5, baseUrl)

      result.paginationViewModel.items.last.number mustBe "5"
      result.paginationViewModel.items.last.current mustBe true
      result.paginationViewModel.previous.isDefined mustBe true
      result.paginationViewModel.next mustBe None
    }

    "generate correct URLs with page query parameter" in {
      val clients = (1 to 25).map(i => createClient(s"$i", s"Client $i"))
      val result  = service.paginateClientList(clients, 2, baseUrl)

      result.paginationViewModel.previous.get.href mustBe s"$baseUrl?page=1"
      result.paginationViewModel.next.get.href mustBe s"$baseUrl?page=3"
      result.paginationViewModel.items.head.href mustBe s"$baseUrl?page=1"
    }

    "handle 100 clients correctly" in {
      val clients = (1 to 100).map(i => createClient(s"$i", s"Client $i"))
      val result  = service.paginateClientList(clients, 1, baseUrl)

      result.totalRecords mustBe 100
      result.totalPages mustBe 10
      result.paginatedData.length mustBe 10
      result.paginationViewModel.items.nonEmpty mustBe true
      result.paginationViewModel.items.exists(_.current) mustBe true
    }
  }
}
