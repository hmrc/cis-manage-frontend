/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.subcontractors

import base.SpecBase
import controllers.routes
import forms.subcontractors.DeleteSubcontractorYesNoFormProvider
import models.subcontractors.DeleteSubcontractorJourneyData
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.CisIdPage
import pages.subcontractors.{DeleteSubcontractorJourneyPage, DeleteSubcontractorYesNoPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.subcontractors.DeleteSubcontractorYesNoView

import scala.concurrent.Future

class DeleteSubcontractorYesNoControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider                       = new DeleteSubcontractorYesNoFormProvider()
  val form                               = formProvider()
  val subcontractorName                  = "subcontractor Name"
  val cisId                              = "1"
  lazy val deleteSubcontractorYesNoRoute =
    controllers.subcontractors.routes.DeleteSubcontractorYesNoController.onPageLoad().url

  private val journeyData =
    DeleteSubcontractorJourneyData(
      subcontractorName = subcontractorName,
      subbieResourceRef = 10L,
      subcontractorCanBeDeleted = true
    )

  private val userAnswersWithJourney =
    emptyUserAnswers
      .set(CisIdPage, cisId)
      .success
      .value
      .set(DeleteSubcontractorJourneyPage, journeyData)
      .success
      .value
  "DeleteSubcontractorYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithJourney)).build()

      running(application) {
        val request = FakeRequest(GET, deleteSubcontractorYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeleteSubcontractorYesNoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(subcontractorName, form, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(DeleteSubcontractorJourneyPage, journeyData)
          .success
          .value
          .set(DeleteSubcontractorYesNoPage, true)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, deleteSubcontractorYesNoRoute)

        val view = application.injector.instanceOf[DeleteSubcontractorYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          subcontractorName,
          form.fill(true),
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must redirect to DeleteSubcontractorController when Yes is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithJourney))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, deleteSubcontractorYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.subcontractors.routes.DeleteSubcontractorController
            .onPageLoad()
            .url
      }
    }

    "must redirect to SubcontractorsListController when No is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithJourney))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, deleteSubcontractorYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.subcontractors.routes.SubcontractorsListController
            .onPageLoad(cisId, NormalMode)
            .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithJourney)).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteSubcontractorYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeleteSubcontractorYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(subcontractorName, boundForm, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, deleteSubcontractorYesNoRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to CannotDeleteSubcontractorController when canBeDeleted is false" in {

      val userAnswers =
        emptyUserAnswers
          .set(
            DeleteSubcontractorJourneyPage,
            DeleteSubcontractorJourneyData(
              subcontractorName = subcontractorName,
              subbieResourceRef = 10L,
              subcontractorCanBeDeleted = false
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, deleteSubcontractorYesNoRoute)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.subcontractors.routes.CannotDeleteSubcontractorController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteSubcontractorYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
