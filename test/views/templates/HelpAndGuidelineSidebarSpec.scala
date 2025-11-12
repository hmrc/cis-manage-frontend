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
import org.scalatest.matchers.should.Matchers.*
import views.html.templates.HelpAndGuidelineSidebar
import config.FrontendAppConfig

class HelpAndGuidelineSidebarSpec extends SpecBase {

  private val template = app.injector.instanceOf[HelpAndGuidelineSidebar]

  "HelpAndGuidelineSidebar" - {

    "renders aside with heading and three help links" in {
      implicit val msgs: play.api.i18n.Messages = messages(app)
      implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

      val html = template()
      val doc  = Jsoup.parse(html.body)

      val aside = doc.selectFirst("aside.app-related-items[role=complementary]")
      aside                           should not be null
      aside.attr("aria-labelledby") shouldBe "subsection-title"

      val h2 = aside.selectFirst("h2.govuk-heading-s#subsection-title")
      h2          should not be null
      h2.text() shouldBe msgs("agent.landing.h2.help")

      val links = aside.select("a.govuk-link")
      links.size() shouldBe 3

      links.get(0).attr("href") shouldBe appConfig.cisHelpWhatIsUrl
      links.get(0).text()       shouldBe msgs("agent.landing.help.link1")

      links.get(1).attr("href") shouldBe appConfig.cisHelpMonthlyUrl
      links.get(1).text()       shouldBe msgs("agent.landing.help.link2")

      links.get(2).attr("href") shouldBe appConfig.cisHelp340Url
      links.get(2).text()       shouldBe msgs("agent.landing.help.link3")
    }
  }
}
