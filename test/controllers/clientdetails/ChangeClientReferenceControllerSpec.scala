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

package controllers.clientdetails

import base.SpecBase
import controllers.actions.{ClientListStatusGuard, HasClientGuard}
import controllers.routes
import forms.clientdetails.ChangeClientReferenceFormProvider
import models.{CisTaxpayerSearchResult, NormalMode, UserAnswers}
import models.requests.{DataRequest, IdentifierRequest}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AgentClientsPage
import pages.clientdetails.ChangeClientReferencePage
import play.api.inject.bind
import play.api.mvc.{ActionFilter, Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.ManageService
import views.html.clientdetails.ChangeClientReferenceView
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ChangeClientReferenceControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider                  = new ChangeClientReferenceFormProvider()
  val form                          = formProvider()
  val uniqueId                      = "123456"
  val mockMangeService              = mock[ManageService]
  val mockSessionRepository         = mock[SessionRepository]
  implicit val ec: ExecutionContext = ExecutionContext.global

  lazy val changeClientReferenceRoute: String =
    controllers.clientdetails.routes.ChangeClientReferenceController.onPageLoad(uniqueId, NormalMode).url

  private val mockClientListStatusGuard = mock[ClientListStatusGuard]
  private val mockHasClientGuard        = mock[HasClientGuard]
  private val mockManageService         = mock[ManageService]

  private val passThroughIdentifierFilter =
    new ActionFilter[IdentifierRequest] {
      override protected def executionContext: ExecutionContext = ec

      override protected def filter[A](
        request: IdentifierRequest[A]
      ): Future[Option[Result]] =
        Future.successful(None)
    }

  private val passThroughDataFilter =
    new ActionFilter[DataRequest] {
      override protected def executionContext: ExecutionContext = ec

      override protected def filter[A](
        request: DataRequest[A]
      ): Future[Option[Result]] =
        Future.successful(None)
    }

  private val passThroughClientListStatusGuard =
    new ActionFilter[IdentifierRequest] {
      override protected def executionContext: ExecutionContext                               = ExecutionContext.global
      override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = Future.successful(None)
    }

  private val passThroughHasClientGuard =
    new ActionFilter[DataRequest] {
      override protected def executionContext: ExecutionContext                         = ExecutionContext.global
      override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] = Future.successful(None)
    }

  when(mockClientListStatusGuard.groupB(any[Call])).thenReturn(passThroughClientListStatusGuard)
  when(mockHasClientGuard.currentClient).thenReturn(passThroughHasClientGuard)

  private val guardBindings = Seq(
    bind[ClientListStatusGuard].toInstance(mockClientListStatusGuard),
    bind[HasClientGuard].toInstance(mockHasClientGuard)
  )

  private def mockGuards(): Unit = {
    when(mockClientListStatusGuard.groupB(any[Call]))
      .thenReturn(passThroughIdentifierFilter)

    when(mockHasClientGuard.forInstanceId(any[String]))
      .thenReturn(passThroughDataFilter)
  }

  private def userAnswersWithClient: UserAnswers =
    UserAnswers(userAnswersId)
      .set(AgentClientsPage, client)
      .success
      .value

  val client =
    List(
      CisTaxpayerSearchResult(
        uniqueId = "123",
        taxOfficeNumber = "111",
        taxOfficeRef = "test111",
        agentOwnRef = Option("TEST LTD"),
        schemeName = Option("ABCD"),
        utr = Option("ABCD")
      )
    )

  "ChangeClientReference Controller" - {

    "must return OK and the correct view for a GET" in {
      mockGuards()

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithClient))
          .overrides(
            bind[ClientListStatusGuard].toInstance(mockClientListStatusGuard),
            bind[HasClientGuard].toInstance(mockHasClientGuard),
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[ManageService].toInstance(mockManageService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, changeClientReferenceRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChangeClientReferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, uniqueId, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      mockGuards()
      val userAnswers = UserAnswers(userAnswersId).set(ChangeClientReferencePage, "answer").success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ClientListStatusGuard].toInstance(mockClientListStatusGuard),
            bind[HasClientGuard].toInstance(mockHasClientGuard),
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[ManageService].toInstance(mockManageService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, changeClientReferenceRoute)

        val view = application.injector.instanceOf[ChangeClientReferenceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), uniqueId, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      mockGuards()
      val mockSessionRepository = mock[SessionRepository]
      val client                = List(
        CisTaxpayerSearchResult(
          uniqueId = "123456",
          taxOfficeNumber = "111",
          taxOfficeRef = "test111",
          agentOwnRef = Option("TEST LTD"),
          schemeName = Option("ABCD"),
          utr = Option("ABCD")
        )
      )

      when(
        mockMangeService.updateClient(any, any, any)(using any[HeaderCarrier])
      ).thenReturn(Future.unit)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(
          userAnswers = Some(
            emptyUserAnswers
              .set(AgentClientsPage, client)
              .success
              .value
              .set(ChangeClientReferencePage, "clientOwnRef")
              .success
              .value
          ),
          additionalBindings = guardBindings ++ Seq(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[ManageService].toInstance(mockMangeService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
        ).build()

      running(application) {
        val request =
          FakeRequest(POST, changeClientReferenceRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.clientdetails.routes.ClientRefUpdateConfirmationController
          .onPageLoad()
          .url
      }
    }

    "must redirect to the system error controller page when connector service returns integer/exception instead of Unit" in {
      mockGuards()
      val mockSessionRepository = mock[SessionRepository]
      val client                = List(
        CisTaxpayerSearchResult(
          uniqueId = "123456",
          taxOfficeNumber = "111",
          taxOfficeRef = "test111",
          agentOwnRef = Option("TEST LTD"),
          schemeName = Option("ABCD"),
          utr = Option("ABCD")
        )
      )

      when(
        mockMangeService.updateClient(any, any, any)(using any[HeaderCarrier])
      ).thenReturn(Future(1))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(
          userAnswers = Some(
            emptyUserAnswers
              .set(AgentClientsPage, client)
              .success
              .value
              .set(ChangeClientReferencePage, "clientOwnRef")
              .success
              .value
          ),
          additionalBindings = guardBindings ++ Seq(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[ManageService].toInstance(mockMangeService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
        ).build()

      running(application) {
        val request =
          FakeRequest(POST, changeClientReferenceRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController
          .onPageLoad()
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      mockGuards()
      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = guardBindings
      ).build()

      running(application) {
        val request =
          FakeRequest(POST, changeClientReferenceRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ChangeClientReferenceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, uniqueId, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      mockGuards()
      val application = applicationBuilder(
        userAnswers = None,
        additionalBindings = guardBindings
      ).build()

      running(application) {
        val request = FakeRequest(GET, changeClientReferenceRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to system error controller for a POST if no existing data is found" in {
      mockGuards()
      val application = applicationBuilder(
        userAnswers = None,
        additionalBindings = guardBindings
      ).build()

      running(application) {
        val request =
          FakeRequest(POST, changeClientReferenceRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        result.futureValue.header.headers
          .get("Location")
          .value
          .contains(controllers.routes.SystemErrorController.onPageLoad().url)
      }
    }
  }
}
