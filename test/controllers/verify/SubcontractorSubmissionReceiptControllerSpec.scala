package controllers.verify

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.verify.SubcontractorSubmissionReceiptView

class SubcontractorSubmissionReceiptControllerSpec extends SpecBase {

  "SubcontractorSubmissionReceipt Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.verify.routes.SubcontractorSubmissionReceiptController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubcontractorSubmissionReceiptView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
