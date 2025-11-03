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

package controllers.agent

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import org.scalatest.matchers.should.Matchers.*
import views.html.agent.AgentLandingView
import config.FrontendAppConfig
import java.time.{LocalDate, YearMonth}

class AgentLandingControllerSpec extends SpecBase {

  "AgentLanding Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val appConfig: FrontendAppConfig =
          application.injector.instanceOf[FrontendAppConfig]

        val request = FakeRequest(GET, routes.AgentLandingController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentLandingView]

        val expected = view(
          clientName = "ABC Construction Ltd",
          employerRef = "123/AB45678",
          utr = "1234567890",
          returnsDueCount = 1,
          returnsDueBy = LocalDate.of(2025, 10, 19),
          newNoticesCount = 2,
          lastSubmittedDate = LocalDate.of(2025, 9, 19),
          lastSubmittedTaxMonth = YearMonth.of(2025, 8)
        )(request, appConfig, messages(application))

        status(result)          shouldBe OK
        contentType(result)       should contain(HTML)
        contentAsString(result) shouldBe expected.toString
      }
    }
  }
}
