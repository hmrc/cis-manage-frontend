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

package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.FakeRequest
import viewmodels.govuk.PaginationFluency.*
import views.html.components.PageNavigator

class PageNavigatorSpec extends SpecBase with Matchers {

  "PageNavigator component" - {

    "must render basic pagination with page numbers" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/page/1"),
          PaginationItemViewModel("2", "/page/2").withCurrent(true),
          PaginationItemViewModel("3", "/page/3")
        )
      )

      val html = paginationComponent(pagination)
      val doc  = Jsoup.parse(html.body)

      doc.select(".govuk-pagination").size() mustBe 1
      doc.select(".govuk-pagination__list").size() mustBe 1
      doc.select(".govuk-pagination__item").size() mustBe 3
      doc.select(".govuk-pagination__item--current").size() mustBe 1
      doc.select(".govuk-pagination__item--current a").attr("aria-current") mustBe "page"
    }

    "must render pagination with previous link" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/page/1"),
          PaginationItemViewModel("2", "/page/2").withCurrent(true)
        ),
        previous = Some(PaginationLinkViewModel("/page/1").withText("site.pagination.previous"))
      )

      val html = paginationComponent(pagination)
      val doc  = Jsoup.parse(html.body)

      doc.select(".govuk-pagination__prev").size() mustBe 1
      doc.select(".govuk-pagination__prev a").attr("href") mustBe "/page/1"
      doc.select(".govuk-pagination__prev a").attr("rel") mustBe "prev"
    }

    "must render pagination with next link" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/page/1").withCurrent(true),
          PaginationItemViewModel("2", "/page/2")
        ),
        next = Some(PaginationLinkViewModel("/page/2").withText("site.pagination.next"))
      )

      val html = paginationComponent(pagination)
      val doc  = Jsoup.parse(html.body)

      doc.select(".govuk-pagination__next").size() mustBe 1
      doc.select(".govuk-pagination__next a").attr("href") mustBe "/page/2"
      doc.select(".govuk-pagination__next a").attr("rel") mustBe "next"
    }

    "must render pagination with ellipsis" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/page/1"),
          PaginationItemViewModel.ellipsis(),
          PaginationItemViewModel("5", "/page/5").withCurrent(true),
          PaginationItemViewModel("6", "/page/6")
        )
      )

      val html = paginationComponent(pagination)
      val doc  = Jsoup.parse(html.body)

      doc.select(".govuk-pagination__item").size() mustBe 4
      doc.select(".govuk-pagination__item").get(1).text() mustBe "⋯"
    }

    "must render empty pagination when no items provided" in new Setup {
      val pagination = PaginationViewModel()

      val html = paginationComponent(pagination)
      val doc  = Jsoup.parse(html.body)

      doc.select(".govuk-pagination").size() mustBe 1
      doc.select(".govuk-pagination__list").size() mustBe 0
      doc.select(".govuk-pagination__prev").size() mustBe 0
      doc.select(".govuk-pagination__next").size() mustBe 0
    }
  }

  trait Setup {
    val app                                       = applicationBuilder().build()
    val paginationComponent                       = app.injector.instanceOf[PageNavigator]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
