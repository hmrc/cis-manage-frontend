package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ContractorDashboardView

class ContractorDashboardControllerSpec extends SpecBase {

  "ContractorDashboard Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ContractorDashboardController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContractorDashboardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(ContractorDashboardController.viewModel())(request, messages(application)).toString
      }
    }
  }
}
