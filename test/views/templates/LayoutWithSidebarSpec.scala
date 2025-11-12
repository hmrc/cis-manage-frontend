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

package views.templates

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import views.html.templates.LayoutWithSidebar

class LayoutWithSidebarSpec extends SpecBase {

  private val template = app.injector.instanceOf[LayoutWithSidebar]

  "LayoutWithSidebar" - {

    "renders title, beforeContent, main grid with content & sidebar, back link, timeout dialog and sign out when enabled" in {
      implicit val req  = FakeRequest(GET, "/some-page")
      implicit val msgs = messages(app)

      val pageTitle   = "Test page"
      val before      = Some(Html("""<div class="before">Before content</div>"""))
      val sidebar     = Some(Html("""<p class="govuk-body">Sidebar block</p>"""))
      val mainContent = Html("""<p class="govuk-body">Main block</p>""")

      val html = template(
        pageTitle = pageTitle,
        showBackLink = true,
        timeout = true,
        showSignOut = true,
        showHmrcBanner = false,
        beforeContent = before,
        sidebar = sidebar
      )(mainContent)

      val doc: Document = Jsoup.parse(html.body)

      doc.text() should include(pageTitle)

      doc.selectFirst(".before").text() shouldBe "Before content"

      val twoThirds = doc.selectFirst(".govuk-grid-column-two-thirds")
      twoThirds                                      should not be null
      twoThirds.selectFirst("p.govuk-body").text() shouldBe "Main block"

      val oneThird = doc.selectFirst(".govuk-grid-column-one-third")
      oneThird                                      should not be null
      oneThird.selectFirst("p.govuk-body").text() shouldBe "Sidebar block"

      val keepAliveUrl = controllers.routes.KeepAliveController.keepAlive().url
      val signOutUrl   = controllers.auth.routes.AuthController.signOut().url
      doc.outerHtml() should include(keepAliveUrl)
      doc.outerHtml() should include(signOutUrl)
    }

    "can hide back link, timeout, and sign out when disabled" in {
      implicit val req  = FakeRequest(GET, "/some-page")
      implicit val msgs = messages(app)

      val html = template(
        pageTitle = "No chrome",
        showBackLink = false,
        timeout = false,
        showSignOut = false,
        showHmrcBanner = false,
        beforeContent = None,
        sidebar = None
      )(Html("""<p class="govuk-body">Only content</p>"""))

      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-back-link").size() shouldBe 0

      val keepAliveUrl = controllers.routes.KeepAliveController.keepAlive().url
      val signOutUrl   = controllers.auth.routes.AuthController.signOut().url
      doc.outerHtml() should not include keepAliveUrl
      doc.outerHtml() should not include signOutUrl

      doc.selectFirst(".govuk-grid-column-two-thirds .govuk-body").text() shouldBe "Only content"
    }
  }
}
