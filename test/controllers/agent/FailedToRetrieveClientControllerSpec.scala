package controllers.agent

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.agent.FailedToRetrieveClientView

class FailedToRetrieveClientControllerSpec extends SpecBase {

  "FailedToRetrieveClient Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.agent.routes.FailedToRetrieveClientController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FailedToRetrieveClientView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }
  }
}
