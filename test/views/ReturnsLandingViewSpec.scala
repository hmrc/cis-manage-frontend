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

package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import viewmodels.ReturnLandingViewModel
import views.html.ReturnsLandingView

class ReturnsLandingViewSpec extends SpecBase {

  "ReturnsLandingView" - {
    "must render the page with the correct html elements" in new Setup {
      val html: HtmlFormat.Appendable = view(contractorName, returnsList)
      val doc: Document               = Jsoup.parse(html.body)

      doc.title                                 must include(messages("returnsLanding.title"))
      doc.select("h1").text                     must include(messages("returnsLanding.heading"))
      doc.select("p").text                      must include(messages("returnsLanding.banner.header"))
      doc.select("p").text                      must include(messages("returnsLanding.banner.p1"))
      doc.getElementsByClass("govuk-link").text must include(messages("returnsLanding.banner.p2.link1"))
      doc.select("p").text                      must include(messages("returnsLanding.banner.p2.text"))
      doc.getElementsByClass("govuk-link").text must include(messages("returnsLanding.banner.p2.link2"))

      doc.getElementsByClass("govuk-link").text must include(messages("returnsLanding.fileStandardReturn.h3.link"))
      doc.select("p").text                      must include(messages("returnsLanding.fileStandardReturn.p1"))
      doc.getElementsByClass("govuk-link").text must include(messages("returnsLanding.fileNilReturn.h3.link"))
      doc.select("p").text                      must include(messages("returnsLanding.fileNilReturn.p1"))
      doc.getElementsByClass("govuk-link").text must include(messages("returnsLanding.amendReturn.h3.link"))
      doc.select("p").text                      must include(messages("returnsLanding.amendReturn.p1"))
      doc.getElementsByClass("govuk-link").text must include(messages("returnsLanding.paymentsAndDeductions.h3.link"))
      doc.select("p").text                      must include(messages("returnsLanding.paymentsAndDeductions.p1"))
      doc.getElementsByClass("govuk-link").text must include(messages("returnsLanding.noticesAndStatements.h3.link"))
      doc.select("p").text                      must include(messages("returnsLanding.noticeAndStatements.p1"))

      doc.select("h2").text must include(messages("returnsLanding.recentCisReturns.h2"))
      doc.select("th").text must include(messages("returnsLanding.taxMonth.th"))
      doc.select("th").text must include(messages("returnsLanding.type.th"))
      doc.select("th").text must include(messages("returnsLanding.lastUpdated.th"))
      doc.select("th").text must include(messages("returnsLanding.status.th"))
      doc.select("th").text must include(messages("site.delete"))

      doc.getElementsByClass("govuk-link").text must include(messages("returnsLanding.viewReturnsHistory.link"))

      doc.select("h2").text                     must include(messages("returnsLanding.aside.h2"))
      doc.getElementsByClass("govuk-link").text must include(messages("returnsLanding.aside.link1"))
      doc.getElementsByClass("govuk-link").text must include(messages("returnsLanding.aside.link2"))
      doc.getElementsByClass("govuk-link").text must include(messages("returnsLanding.aside.link3"))
      doc.getElementsByClass("govuk-link").text must include(messages("returnsLanding.aside.link4"))
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: ReturnsLandingView                  =
      app.injector.instanceOf[ReturnsLandingView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
    val contractorName                            = "ABC Ltd..."
    val returnsList                               = Seq(
      ReturnLandingViewModel("August 2025", "Standard", "19 September 2025", "Accepted"),
      ReturnLandingViewModel("July 2025", "Nil", "19 August 2025", "Accepted"),
      ReturnLandingViewModel("June 2025", "Standard", "18 July 2025", "Accepted")
    )
  }
}
