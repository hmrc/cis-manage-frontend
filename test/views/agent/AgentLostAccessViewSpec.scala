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

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.agent.AgentLostAccessView

class AgentLostAccessViewSpec extends SpecBase {

  "AgentLostAccessView" - {

    "must render the page with the correct title, heading and paragraph" in new Setup {
      private val html: HtmlFormat.Appendable = view()
      private val doc: Document               = Jsoup.parse(html.body)

      doc.title             must include(messages("agentLostAccess.title"))
      doc.select("h1").text must include(messages("agentLostAccess.heading"))
    }
  }

  trait Setup {
    private val app: Application       = applicationBuilder().build()
    val view: AgentLostAccessView = app.injector.instanceOf[AgentLostAccessView]
    implicit val request: Request[_]   = FakeRequest()
    implicit val messages: Messages    = MessagesImpl(Lang.defaultLang, app.injector.instanceOf[MessagesApi])
  }
}
