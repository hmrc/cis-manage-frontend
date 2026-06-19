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

import javax.inject.{Inject, Singleton}
import viewmodels.govuk.PaginationFluency.*
import viewmodels.subcontractors.SubcontractorsListConstants

object PaginationSubcontractorsListService {
  case class PaginatedResult[T](
    items: Seq[T],
    pagination: PaginationViewModel,
    currentPage: Int,
    totalPages: Int,
    startIndex: Int,
    totalCount: Int
  )
}

@Singleton
class PaginationSubcontractorsListService @Inject() () {
  import PaginationSubcontractorsListService.*

  private val defaultRecordsPerPage = SubcontractorsListConstants.RecordsPerPage

  private def buildUrl(
    baseUrl: String,
    pageParam: String,
    page: Int,
    queryString: String
  ): String = {
    val separator =
      if (baseUrl.contains("?")) "&" else "?"

    val withPage =
      s"$baseUrl$separator$pageParam=$page"

    if (queryString.trim.isEmpty) withPage
    else s"$withPage&$queryString"
  }

  def paginate[T](
    allItems: Seq[T],
    currentPage: Int,
    recordsPerPage: Int = defaultRecordsPerPage,
    baseUrl: String,
    pageParam: String = "page",
    queryString: String = ""
  ): PaginatedResult[T] = {

    val totalCount =
      allItems.size

    val totalPages =
      math.ceil(totalCount.toDouble / recordsPerPage).toInt.max(1)

    val page =
      currentPage.max(1).min(totalPages)

    val start =
      (page - 1) * recordsPerPage

    val end =
      start + recordsPerPage

    val pageItems =
      allItems.slice(start, end)

    PaginatedResult(
      items = pageItems,
      pagination = buildPagination(page, totalPages, baseUrl, pageParam, queryString),
      currentPage = page,
      totalPages = totalPages,
      startIndex = if (totalCount == 0) 0 else start + 1,
      totalCount = totalCount
    )
  }

  private def buildPagination(
    page: Int,
    totalPages: Int,
    baseUrl: String,
    pageParam: String,
    queryString: String
  ): PaginationViewModel =
    if (totalPages <= 1) {
      PaginationViewModel()
    } else {

      def url(p: Int): String =
        buildUrl(baseUrl, pageParam, p, queryString)

      def pageItem(p: Int): PaginationItemViewModel =
        PaginationItemViewModel(
          number = p.toString,
          href = url(p)
        ).withCurrent(p == page)

      val items: Seq[PaginationItemViewModel] =
        if (totalPages <= 5) {
          (1 to totalPages).map(pageItem)
        } else if (page <= 3) {
          Seq(
            pageItem(1),
            pageItem(2),
            pageItem(3),
            pageItem(4),
            PaginationItemViewModel.ellipsis(),
            pageItem(totalPages)
          )
        } else if (page >= totalPages - 2) {
          Seq(
            pageItem(1),
            PaginationItemViewModel.ellipsis(),
            pageItem(totalPages - 3),
            pageItem(totalPages - 2),
            pageItem(totalPages - 1),
            pageItem(totalPages)
          )
        } else {
          Seq(
            pageItem(1),
            PaginationItemViewModel.ellipsis(),
            pageItem(page - 1),
            pageItem(page),
            pageItem(page + 1),
            PaginationItemViewModel.ellipsis(),
            pageItem(totalPages)
          )
        }

      PaginationViewModel()
        .withItems(items)
        .copy(
          previous = if (page > 1) {
            Some(PaginationLinkViewModel(url(page - 1)).withText("site.pagination.previous"))
          } else {
            None
          },
          next = if (page < totalPages) {
            Some(PaginationLinkViewModel(url(page + 1)).withText("site.pagination.next"))
          } else {
            None
          }
        )
    }
}
