package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.SuccessfulAutomaticSubcontractorUpdateViewModel
import views.html.SuccessfulAutomaticSubcontractorUpdateView
import org.mockito.Mockito.*

class SuccessfulAutomaticSubcontractorUpdateControllerSpec extends SpecBase {

  "SuccessfulAutomaticSubcontractorUpdate Controller" - {
    "must return OK and the correct view for a GET" in {
      val subcontractorsList: Seq[SuccessfulAutomaticSubcontractorUpdateViewModel] = Seq(
        SuccessfulAutomaticSubcontractorUpdateViewModel("Alice, A", "1111111111", " ", "01 Jan 2014"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Bob, B", "2222222222", " ", "01 Jan 2014"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Dave, D", "4444444444", "V1000000009", "07 May 2015"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Charles, C", "3333333333", "V1000000009", "01 Jan 2014"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Elise, E", "5555555555", "V1000000009", "07 May 2015"),
        SuccessfulAutomaticSubcontractorUpdateViewModel("Frank, F", "6666666666", "V1000000009", "07 Jan 2018")
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.SuccessfulAutomaticSubcontractorUpdateController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SuccessfulAutomaticSubcontractorUpdateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(subcontractorsList)(request, messages(application)).toString
      }
    }
  }
}
