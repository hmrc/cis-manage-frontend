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

package views.verify

import base.SpecBase
import forms.verify.VerificationHistorySelectTaxYearFormProvider
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import models.NormalMode
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.verify.VerificationHistorySelectTaxYearView

class VerificationHistorySelectTaxYearViewSpec extends SpecBase {

  "VerificationHistorySelectTaxYearView" - {

    "must render the page with correct title, heading and button" in new Setup {

      val doc: Document = Jsoup.parse(html.toString)

      doc.title()             must include(messages("verify.verificationHistorySelectTaxYear.title"))
      doc.select("h1").text() must include(messages("verify.verificationHistorySelectTaxYear.heading"))

      doc.select(".govuk-button").text() must include(messages("site.continue"))
    }

    "must render radio buttons for each tax year" in new Setup {

      val doc: Document = Jsoup.parse(html.toString)

      taxYears.zipWithIndex.foreach { case (year, index) =>
        doc.select(s"input[type=radio][value='$year']").size() mustBe 1
        doc.select(s"label[for=value_$index]").text() must include(year)
      }
    }

    "must render 'all tax years' option" in new Setup {

      val doc: Document = Jsoup.parse(html.toString)

      doc.select("input[type=radio][value=all]").size() mustBe 1

      val labels =
        doc.select("label").eachText().toArray.toList

      labels must contain("View all tax years")
    }

    "must show error summary when form has errors" in new Setup {

      val errorForm = form.bind(Map("value" -> ""))

      val errorHtml = view(errorForm, mode, taxYears)
      val doc       = Jsoup.parse(errorHtml.toString)

      doc.select(".govuk-error-summary").size() mustBe 1
    }
  }

  trait Setup {

    val app: Application = applicationBuilder().build()

    val view: VerificationHistorySelectTaxYearView =
      app.injector.instanceOf[VerificationHistorySelectTaxYearView]

    val taxYears: Seq[String] =
      Seq("2021 to 2022", "2022 to 2023", "2023 to 2024")

    val formProvider       = new VerificationHistorySelectTaxYearFormProvider()
    val form: Form[String] = formProvider(taxYears)

    val mode: Mode = NormalMode

    implicit val request: Request[_] = FakeRequest()

    implicit val messages: Messages =
      MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[MessagesApi]
      )

    val html: HtmlFormat.Appendable =
      view(form, mode, taxYears)
  }
}
