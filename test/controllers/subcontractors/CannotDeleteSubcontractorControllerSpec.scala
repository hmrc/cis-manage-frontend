package controllers.subcontractors

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.subcontractors.CannotDeleteSubcontractorView

class CannotDeleteSubcontractorControllerSpec extends SpecBase {

  "CannotDeleteSubcontractorController" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, controllers.subcontractors.routes.CannotDeleteSubcontractorController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CannotDeleteSubcontractorView]

        val expectedSubcontractorName = "subcontractor Name"
        val expectedUrl               = "#"

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(expectedSubcontractorName, expectedUrl)(request, messages(application)).toString
      }
    }
  }
}
