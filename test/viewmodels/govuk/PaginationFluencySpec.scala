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

package viewmodels.govuk

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PaginationFluencySpec extends AnyWordSpec with Matchers {

  "PaginationItemViewModel" should {

    "set visually hidden text" in {
      val item =
        PaginationFluency
          .PaginationItemViewModel(
            number = "2",
            href = "/test-url?page=2"
          )
          .withVisuallyHiddenText("site.pagination.goToPage")

      item.visuallyHiddenText mustBe Some("site.pagination.goToPage")
    }

    "set current page" in {
      val item =
        PaginationFluency
          .PaginationItemViewModel(
            number = "2",
            href = "/test-url?page=2"
          )
          .withCurrent(true)

      item.current mustBe true
    }

    "set attributes" in {
      val item =
        PaginationFluency
          .PaginationItemViewModel(
            number = "2",
            href = "/test-url?page=2"
          )
          .withAttributes(Map("data-test" -> "page-2"))

      item.attributes mustBe Map("data-test" -> "page-2")
    }

    "create an ellipsis item without visually hidden text" in {
      val item = PaginationFluency.PaginationItemViewModel.ellipsis()

      item.number mustBe empty
      item.href mustBe empty
      item.visuallyHiddenText mustBe None
      item.current mustBe false
      item.ellipsis mustBe true
      item.attributes mustBe empty
    }
  }

  "PaginationLinkViewModel" should {

    "set text" in {
      val link =
        PaginationFluency
          .PaginationLinkViewModel("/test-url?page=2")
          .withText("site.pagination.next")

      link.text mustBe Some("site.pagination.next")
    }

    "set html" in {
      val link =
        PaginationFluency
          .PaginationLinkViewModel("/test-url?page=2")
          .withHtml("<span>Next</span>")

      link.html mustBe Some("<span>Next</span>")
    }

    "set label text" in {
      val link =
        PaginationFluency
          .PaginationLinkViewModel("/test-url?page=2")
          .withLabelText("site.pagination.next")

      link.labelText mustBe Some("site.pagination.next")
    }

    "set aria label" in {
      val link =
        PaginationFluency
          .PaginationLinkViewModel("/test-url?page=2")
          .withAriaLabel("Go to page 2")

      link.attributes must contain(
        "aria-label" -> "Go to page 2"
      )
    }

    "set additional attributes" in {
      val link =
        PaginationFluency
          .PaginationLinkViewModel("/test-url?page=2")
          .withAttributes(Map("data-test" -> "next"))

      link.attributes mustBe Map("data-test" -> "next")
    }
  }

  "PaginationViewModel" should {

    "create an empty pagination view model" in {
      val pagination = PaginationFluency.PaginationViewModel()

      pagination.items mustBe empty
      pagination.previous mustBe None
      pagination.next mustBe None
      pagination.landmarkLabel mustBe "site.pagination.landmark"
      pagination.classes mustBe empty
      pagination.attributes mustBe empty
    }

    "set items" in {
      val item =
        PaginationFluency.PaginationItemViewModel(
          number = "1",
          href = "/test-url?page=1"
        )

      val pagination =
        PaginationFluency
          .PaginationViewModel()
          .withItems(Seq(item))

      pagination.items mustBe Seq(item)
    }

    "set previous link" in {
      val previous =
        PaginationFluency.PaginationLinkViewModel("/test-url?page=1")

      val pagination =
        PaginationFluency
          .PaginationViewModel()
          .withPrevious(previous)

      pagination.previous mustBe Some(previous)
    }

    "set next link" in {
      val next =
        PaginationFluency.PaginationLinkViewModel("/test-url?page=3")

      val pagination =
        PaginationFluency
          .PaginationViewModel()
          .withNext(next)

      pagination.next mustBe Some(next)
    }

    "set landmark label" in {
      val pagination =
        PaginationFluency
          .PaginationViewModel()
          .withLandmarkLabel("site.pagination.landmark")

      pagination.landmarkLabel mustBe "site.pagination.landmark"
    }

    "set classes" in {
      val pagination =
        PaginationFluency
          .PaginationViewModel()
          .withClasses("custom-class")

      pagination.classes mustBe "custom-class"
    }

    "set attributes" in {
      val pagination =
        PaginationFluency
          .PaginationViewModel()
          .withAttributes(Map("data-test" -> "pagination"))

      pagination.attributes mustBe Map("data-test" -> "pagination")
    }
  }
}
