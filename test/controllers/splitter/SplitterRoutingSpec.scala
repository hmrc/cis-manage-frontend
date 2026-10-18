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

package controllers.splitter

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class SplitterRoutingSpec extends SpecBase {

  private def buildApplication() = applicationBuilder(userAnswers = None)
    .configure("play.http.router" -> "prod.Routes")
    .build()

  "prod routes" - {

    "must send /construction-ind-scheme paths to the core splitter" in {
      val application = buildApplication()
      running(application) {
        val result = route(application, FakeRequest(GET, "/construction-ind-scheme/some/path")).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/construction-ind-legacy/some/path"
      }
    }

    "must send /cis-file paths to the file splitter" in {
      val application = buildApplication()
      running(application) {
        val result = route(application, FakeRequest(GET, "/cis-file/some/path")).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/cis-file-legacy/some/path"
      }
    }

    "must send the bare /construction-ind-scheme entry point (no path) to the core splitter" in {
      val application = buildApplication()
      running(application) {
        val result = route(application, FakeRequest(GET, "/construction-ind-scheme")).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must send /construction-ind-scheme/ (trailing slash) to the core splitter" in {
      val application = buildApplication()
      running(application) {
        val result = route(application, FakeRequest(GET, "/construction-ind-scheme/")).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must still serve the management routes" in {
      val application = buildApplication()
      running(application) {
        val result = route(application, FakeRequest(GET, "/construction-industry-scheme/management")).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.IntroductionController.onPageLoad().url
      }
    }
  }
}
