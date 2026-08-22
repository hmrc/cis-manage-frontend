package controllers.agent

import base.SpecBase
import models.CisTaxpayerSearchResult
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.AgentService

import scala.concurrent.Future

class ClientRemoveControllerSpec extends SpecBase with MockitoSugar {

  val employerRef = "123456"

  val okResponse =
    CisTaxpayerSearchResult(
      uniqueId = "123",
      taxOfficeNumber = "111",
      taxOfficeRef = "test111",
      agentOwnRef = Option("TEST LTD"),
      schemeName = Option("ABCD"),
      utr = Option("ABCD")
    )

    // lazy val routeUrl: String =
    // controllers.agent.routes.ClientRemoveController
    //  .onPageLoad(employerRef).url

  "ClientRemoveControllerSpec" - {

    "must redirect to ClientRemoveYesNoController when clientSearchResultByEmpRef size is 1" in {

      val mockService = mock[AgentService]
      when(
        mockService.getClientsByEmployersReference(
          eqTo(employerRef)
        )(any())
      ).thenReturn(
        Future.successful(List(okResponse))
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AgentService].toInstance(mockService)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.agent.routes.ClientRemoveController
              .onPageLoad(employerRef)
              .url
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url

        verify(mockService)
          .getClientsByEmployersReference(
            eqTo(employerRef)
          )(any())
      }
    }

  }

}
