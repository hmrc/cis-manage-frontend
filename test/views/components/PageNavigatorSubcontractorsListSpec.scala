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

package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.test.FakeRequest
import viewmodels.govuk.PaginationFluency._
import views.html.components.PageNavigatorSubcontractorsList

class PageNavigatorSubcontractorsListSpec extends SpecBase with Matchers {

  "PageNavigatorSubcontractorsList" - {

    "must render nothing when pagination items are empty" in new Setup {

      val html = view(PaginationViewModel())

      val doc: Document = Jsoup.parse(html.body)

      doc.select("nav.govuk-pagination").size() mustBe 0
    }

    "must render previous and next links when provided" in new Setup {

      val pagination =
        PaginationViewModel(
          items = Seq(
            PaginationItemViewModel("1", "/page/1"),
            PaginationItemViewModel("2", "/page/2").withCurrent(true),
            PaginationItemViewModel("3", "/page/3")
          ),
          previous = Some(PaginationLinkViewModel("/page/1").withText("site.pagination.previous")),
          next = Some(PaginationLinkViewModel("/page/3").withText("site.pagination.next"))
        )

      val html = view(pagination)
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-pagination__prev a").attr("href") mustBe "/page/1"
      doc.select(".govuk-pagination__prev a").attr("rel") mustBe "prev"

      doc.select(".govuk-pagination__next a").attr("href") mustBe "/page/3"
      doc.select(".govuk-pagination__next a").attr("rel") mustBe "next"
    }

    "must render page number links" in new Setup {

      val pagination =
        PaginationViewModel(
          items = Seq(
            PaginationItemViewModel("1", "/page/1"),
            PaginationItemViewModel("2", "/page/2").withCurrent(true),
            PaginationItemViewModel("3", "/page/3")
          )
        )

      val html = view(pagination)
      val doc = Jsoup.parse(html.body)

      val pageLinks = doc.select(".govuk-pagination__list .govuk-pagination__link")

      pageLinks.size() mustBe 3
      pageLinks.get(0).text() mustBe "1"
      pageLinks.get(0).attr("href") mustBe "/page/1"

      pageLinks.get(1).text() mustBe "2"
      pageLinks.get(1).attr("href") mustBe "/page/2"

      pageLinks.get(2).text() mustBe "3"
      pageLinks.get(2).attr("href") mustBe "/page/3"
    }

    "must render the current page with aria-current page" in new Setup {

      val pagination =
        PaginationViewModel(
          items = Seq(
            PaginationItemViewModel("1", "/page/1"),
            PaginationItemViewModel("2", "/page/2").withCurrent(true),
            PaginationItemViewModel("3", "/page/3")
          )
        )

      val html = view(pagination)
      val doc = Jsoup.parse(html.body)

      val currentItem = doc.select(".govuk-pagination__item--current a")

      currentItem.size() mustBe 1
      currentItem.text() mustBe "2"
      currentItem.attr("aria-current") mustBe "page"
    }

    "must render ellipsis when pagination item is ellipsis" in new Setup {

      val pagination =
        PaginationViewModel(
          items = Seq(
            PaginationItemViewModel("1", "/page/1"),
            PaginationItemViewModel.ellipsis(),
            PaginationItemViewModel("10", "/page/10")
          )
        )

      val html = view(pagination)
      val doc = Jsoup.parse(html.body)

      val ellipsis = doc.select(".govuk-pagination__item--ellipses")

      ellipsis.size() mustBe 1
      ellipsis.text() mustBe "⋯"
    }

    "must render the correct aria-label for page links" in new Setup {

      val pagination =
        PaginationViewModel(
          items = Seq(
            PaginationItemViewModel("1", "/page/1"),
            PaginationItemViewModel("2", "/page/2").withCurrent(true)
          )
        )

      val html = view(pagination)
      val doc = Jsoup.parse(html.body)

      val pageLinks = doc.select(".govuk-pagination__list .govuk-pagination__link")

      pageLinks.get(0).attr("aria-label") mustBe "Page 1"
      pageLinks.get(1).attr("aria-label") mustBe "Page 2"
    }

    "must render pagination landmark label" in new Setup {

      val pagination =
        PaginationViewModel(
          items = Seq(
            PaginationItemViewModel("1", "/page/1"),
            PaginationItemViewModel("2", "/page/2")
          )
        )

      val html = view(pagination)
      val doc = Jsoup.parse(html.body)

      doc.select("nav.govuk-pagination").attr("aria-label") mustBe messages(pagination.landmarkLabel)
    }
  }

  trait Setup {

    val app: Application = applicationBuilder().build()

    implicit val request: FakeRequest[_] =
      FakeRequest()

    implicit val messages: Messages =
      MessagesImpl(Lang.defaultLang, app.injector.instanceOf[MessagesApi])

    val view: PageNavigatorSubcontractorsList =
      app.injector.instanceOf[PageNavigatorSubcontractorsList]
  }
}
