package controllers.subcontractors

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.subcontractors.NoSubcontractorsExistView

class NoSubcontractorsExistControllerSpec extends SpecBase {

  "NoSubcontractorsExist Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCisId)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.subcontractors.routes.NoSubcontractorsExistController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NoSubcontractorsExistView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("1")(request, messages(application)).toString
      }
    }

    "throw IllegalStateException when cisId is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, controllers.subcontractors.routes.NoSubcontractorsExistController.onPageLoad().url)

        val controller = application.injector.instanceOf[NoSubcontractorsExistController]

        val exception = controller.onPageLoad()(request).failed.futureValue

        exception mustBe a[IllegalStateException]
        exception.getMessage mustBe "cisId missing from userAnswers"
      }
    }
  }
}
