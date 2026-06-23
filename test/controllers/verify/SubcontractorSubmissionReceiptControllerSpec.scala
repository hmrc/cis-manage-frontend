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
