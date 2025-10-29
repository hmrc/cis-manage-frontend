package controllers.agent

import base.SpecBase
import controllers.routes
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.agent.NoAuthorisedClientsView

class NoAuthorisedClientsControllerSpec extends SpecBase {

  "NoAuthorisedClients Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.agent.routes.NoAuthorisedClientsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NoAuthorisedClientsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
