package controllers.agent

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.agent.RetrievingClientView

class RetrievingClientControllerSpec extends SpecBase {

  "RetrievingClient Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.agent.routes.RetrievingClientController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RetrievingClientView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }
  }
}
