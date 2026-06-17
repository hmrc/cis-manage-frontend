package controllers.subcontractors

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.subcontractors.SubcontractorDeletedConfirmationView

class SubcontractorDeletedConfirmationControllerSpec extends SpecBase {

  "SubcontractorDeletedConfirmationController" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.subcontractors.routes.SubcontractorDeletedConfirmationController.onPageLoad().url
          )

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubcontractorDeletedConfirmationView]

        val expectedSubcontractorName = "subcontractor Name"
        val expectedUrl               = "#"
        val expectedSurveyUrl         = "#"

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(expectedSubcontractorName, expectedUrl, expectedSurveyUrl)(
            request,
            messages(application)
          ).toString
      }
    }
  }
}
