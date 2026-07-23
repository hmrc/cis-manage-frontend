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

package controllers.agent

import base.SpecBase
import config.FrontendAppConfig
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.agent.AgentLostAccessView

class AgentLostAccessControllerSpec extends SpecBase {

  private val agentCode = "agentCode"

  "AgentLostAccess Controller" - {

    "when an agent code is returned" - {

      "must return OK and the correct view for a GET" in {

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers),
            agentCode = Some(agentCode)
          ).build()

        running(application) {
          val request =
            FakeRequest(
              GET,
              controllers.agent.routes.AgentLostAccessController
                .onPageLoad()
                .url
            )

          val result = route(application, request).value

          val view =
            application.injector.instanceOf[AgentLostAccessView]

          val appConfig =
            application.injector.instanceOf[FrontendAppConfig]

          val expectedUrl =
            appConfig.authoriseClientRequestUrl(agentCode)

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(expectedUrl)(
              request,
              appConfig,
              messages(application)
            ).toString
        }
      }
    }

    "when an agent code is not returned" - {

      "must redirect to the unauthorised agent affinity page" in {

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers),
            agentCode = None
          ).build()

        running(application) {
          val request =
            FakeRequest(
              GET,
              controllers.agent.routes.AgentLostAccessController
                .onPageLoad()
                .url
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.UnauthorisedAgentAffinityController
              .onPageLoad()
              .url
        }
      }
    }
  }
}
