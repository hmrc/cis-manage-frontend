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
import models.NormalMode
import models.subcontractors.DeleteSubcontractorJourneyData
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.CisIdPage
import pages.subcontractors.{DeleteSubcontractorJourneyPage, DeleteSubcontractorYesNoPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SubcontractorService

import scala.concurrent.Future

class DeleteSubcontractorControllerSpec extends SpecBase with MockitoSugar {

  private val cisId             = "cis-123"
  private val subcontractorName = "ABC Contractors"
  private val subbieResourceRef = 10L

  lazy val deleteSubcontractorRoute: String = "/subcontractors/delete-subcontractor/submit"
  private val journeyData =
    DeleteSubcontractorJourneyData(
      subcontractorName = subcontractorName,
      subbieResourceRef = subbieResourceRef,
      subcontractorCanBeDeleted = true
    )

  "DeleteSubcontractorController" - {

    "must delete the subcontractor and redirect to confirmation page when answer is yes" in {

      val mockSubcontractorService = mock[SubcontractorService]

      when(
        mockSubcontractorService.deleteSubcontractor(
          eqTo(cisId),
          eqTo(subbieResourceRef)
        )(any())
      ).thenReturn(Future.unit)

      val userAnswers =
        emptyUserAnswers
          .set(CisIdPage, cisId)
          .success
          .value
          .set(DeleteSubcontractorJourneyPage, journeyData)
          .success
          .value
          .set(DeleteSubcontractorYesNoPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, deleteSubcontractorRoute)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.subcontractors.routes.SubcontractorDeletedConfirmationController
            .onPageLoad()
            .url

        verify(mockSubcontractorService)
          .deleteSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any())
      }
    }

    "must redirect to subcontractor list page when answer is no" in {

      val mockSubcontractorService = mock[SubcontractorService]

      val userAnswers =
        emptyUserAnswers
          .set(CisIdPage, cisId)
          .success
          .value
          .set(DeleteSubcontractorJourneyPage, journeyData)
          .success
          .value
          .set(DeleteSubcontractorYesNoPage, false)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, deleteSubcontractorRoute)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.subcontractors.routes.SubcontractorsListController
            .onPageLoad(cisId, NormalMode)
            .url
      }
    }

    "must redirect to Journey Recovery when journey data is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdPage, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {

        val request =
          FakeRequest(POST, deleteSubcontractorRoute)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery when yes/no answer is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdPage, cisId)
          .success
          .value
          .set(DeleteSubcontractorJourneyPage, journeyData)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {

        val request =
          FakeRequest(POST, deleteSubcontractorRoute)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }
  }
}
