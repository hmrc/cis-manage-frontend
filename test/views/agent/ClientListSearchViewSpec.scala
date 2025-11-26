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

package views.agent

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

import base.SpecBase
import forms.ClientListSearchFormProvider
import models.agent.ClientListFormData
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import viewmodels.agent.ClientStatus.Active
import viewmodels.agent.{ClientListViewModel, SearchByList}
import views.ViewSpecGetters
import views.html.agent.ClientListSearchView

class ClientListSearchViewSpec extends SpecBase with Matchers with ViewSpecGetters {

  "ClientListSearchView" - {

    "must render the page with heading, paragraph, input and button" in new Setup {
      val html: HtmlFormat.Appendable = view(form = form, searchByOptions = searchOptions, clientList = clientList)
      val doc: Document               = Jsoup.parse(html.body)
      doc.title                must include(messages("agent.clientListSearch.title"))
      doc.select("h1").text mustBe messages("agent.clientListSearch.heading")
      doc.select("label").text must include(messages("agent.clientListSearch.searchBy.label"))

      def validateSelectValues(document: Document, searchByOptions: Seq[SearchByList], numberOfSets: Int = 1): Unit = {
        val elements: List[Element] = getElementsBySelector(document, "option")
        elements.size                     shouldBe searchByOptions.size + 1
        elements.map(_.attr("value")).toSet should contain allElementsOf searchByOptions.map(_.value)
      }

      validateSelectValues(doc, searchOptions, 3)

      doc.select("label").text                  must include(messages("agent.clientListSearch.searchFilter.label"))
      doc.select("button[type=submit]").text mustBe messages("site.search")
      doc.getElementsByClass("govuk-link").text must include(messages("agent.clientListSearch.clearSearch"))

      val hint: Element = getElementByClass(doc, "govuk-hint")
      hint.text() mustBe messages("agent.clientListSearch.searchFilter.label.hint")

      doc.getElementById("table-heading").text mustBe messages("agent.clientListSearch.table.caption")

    }

    "must show error summary and messages when form has errors" in new Setup {
      val boundWithError: Form[ClientListFormData] = form.bind(Map("searchBy" -> "", "searchFilter" -> ""))
      val html: HtmlFormat.Appendable              =
        view(form = boundWithError, searchByOptions = searchOptions, clientList = clientList)
      val doc: Document                            = Jsoup.parse(html.body)

      doc.title must startWith(messages("error.title.prefix"))
      doc.select(".govuk-error-summary").size() mustBe 1
      val expected: String = messages("agent.clientListSearch.searchBy.error.required")
      doc.text() must include(expected)
    }
  }

  trait Setup {
    val app: Application                           = applicationBuilder().build()
    val view: ClientListSearchView                 = app.injector.instanceOf[ClientListSearchView]
    val formProvider: ClientListSearchFormProvider = app.injector.instanceOf[ClientListSearchFormProvider]
    val form: Form[ClientListFormData]             = formProvider()
    val searchOptions: Seq[SearchByList]           = SearchByList.searchByOptions
    val clientList: Seq[ClientListViewModel]       = Seq(
      ClientListViewModel("ABC Construction Ltd", "123/AB45678", "AOR-001", Active, "unique-id-1"),
      ClientListViewModel("ABC Property Services", "789/EF23456", "AOR-002", Active, "unique-id-2"),
      ClientListViewModel("Capital Construction Group", "345/IJ67890", "AOR-003", Active, "unique-id-3")
    )
    implicit val request: play.api.mvc.Request[_]  = FakeRequest()
    implicit val messages: Messages                = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
