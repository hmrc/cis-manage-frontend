package controllers

import base.SpecBase
import models.UserAnswers
import pages.{CisIdPage, ContractorNamePage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.SubcontractorsLandingPageView

class SubcontractorsLandingPageControllerSpec extends SpecBase {

  "SubcontractorsLandingPage Controller" - {

    "must return OK and the correct view for a GET" in {
      val contractorName: String        = "ABC Construction Ltd"
      lazy val userAnswers: UserAnswers =
        userAnswersWithCisId
          .set(ContractorNamePage, contractorName)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.SubcontractorsLandingPageController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubcontractorsLandingPageView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(contractorName)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must throw IllegalStateException when ContractorNamePage is missing" in {
      lazy val userAnswers: UserAnswers =
        userAnswersWithCisId
          .set(CisIdPage, "some value")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.SubcontractorsLandingPageController.onPageLoad().url)

        val exception = intercept[IllegalStateException] {
          contentAsString(route(application, request).value)
        }

        exception.getMessage mustEqual "contractorName missing from userAnswers"
      }
    }
  }
}
