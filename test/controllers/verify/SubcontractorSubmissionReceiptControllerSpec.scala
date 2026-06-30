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

package controllers.verify

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.verify.SubcontractorSubmissionReceiptView

class SubcontractorSubmissionReceiptControllerSpec extends SpecBase {

  "SubcontractorSubmissionReceipt Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCisId)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.verify.routes.SubcontractorSubmissionReceiptController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubcontractorSubmissionReceiptView]

        val testSubmissionTime = "12:00"
        val testSubmissionDate = "18 May 2025"
        val testContractorName = "John Doe"
        val testEmployerRef    = "ABC12345"
        val testIRNumber       = "123456"
        val testCisId          = "1"

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          testSubmissionTime,
          testSubmissionDate,
          testContractorName,
          testEmployerRef,
          testIRNumber,
          testCisId
        )(request, messages(application)).toString
      }
    }
  }
}
