/*
 * Copyright 2025 HM Revenue & Customs
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

package services

import connectors.ConstructionIndustrySchemeConnector
import models.Scheme
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PrepopServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = global

  "PrepopService.prepopulateContractorKnownFacts" should {

    "delegate to the connector with the same parameters and complete successfully" in {
      val mockConnector = mock[ConstructionIndustrySchemeConnector]
      val service       = new PrepopService(mockConnector)

      val instanceId      = "CIS-123"
      val taxOfficeNumber = "163"
      val taxOfficeRef    = "AB0063"

      when(
        mockConnector.prepopulateContractorKnownFacts(
          any[String],
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(Future.unit)

      val result = service
        .prepopulateContractorKnownFacts(
          instanceId = instanceId,
          taxOfficeNumber = taxOfficeNumber,
          taxOfficeReference = taxOfficeRef
        )

      result.futureValue mustBe ()

      verify(mockConnector)
        .prepopulateContractorKnownFacts(
          eqTo(instanceId),
          eqTo(taxOfficeNumber),
          eqTo(taxOfficeRef)
        )(any[HeaderCarrier])

      verifyNoMoreInteractions(mockConnector)
    }

    "propagate failures from the connector" in {
      val mockConnector = mock[ConstructionIndustrySchemeConnector]
      val service       = new PrepopService(mockConnector)

      val instanceId      = "CIS-123"
      val taxOfficeNumber = "163"
      val taxOfficeRef    = "AB0063"

      when(
        mockConnector.prepopulateContractorKnownFacts(
          any[String],
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service
          .prepopulateContractorKnownFacts(
            instanceId = instanceId,
            taxOfficeNumber = taxOfficeNumber,
            taxOfficeReference = taxOfficeRef
          )
          .futureValue
      }

      ex.getMessage must include("boom")

      verify(mockConnector)
        .prepopulateContractorKnownFacts(
          eqTo(instanceId),
          eqTo(taxOfficeNumber),
          eqTo(taxOfficeRef)
        )(any[HeaderCarrier])

      verifyNoMoreInteractions(mockConnector)
    }
  }

  "PrepopService.prepopulate" should {

    "return true when connector call succeeds" in {
      val mockConnector = mock[ConstructionIndustrySchemeConnector]
      val service       = new PrepopService(mockConnector)

      when(
        mockConnector.prepopulateContractorAndSubcontractors(any[String], any[String], any[String])(any[HeaderCarrier])
      ).thenReturn(Future.unit)

      val result = service.prepopulate("163", "AB0063", "CIS-123").futureValue
      result mustBe true

      verify(mockConnector)
        .prepopulateContractorAndSubcontractors(eqTo("163"), eqTo("AB0063"), eqTo("CIS-123"))(any[HeaderCarrier])

      verifyNoMoreInteractions(mockConnector)
    }

    "return false when connector call fails" in {
      val mockConnector = mock[ConstructionIndustrySchemeConnector]
      val service       = new PrepopService(mockConnector)

      when(
        mockConnector.prepopulateContractorAndSubcontractors(any[String], any[String], any[String])(any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val result = service.prepopulate("163", "AB0063", "CIS-123").futureValue
      result mustBe false

      verify(mockConnector)
        .prepopulateContractorAndSubcontractors(eqTo("163"), eqTo("AB0063"), eqTo("CIS-123"))(any[HeaderCarrier])

      verifyNoMoreInteractions(mockConnector)
    }
  }

  "PrepopService.getScheme" should {

    "delegate to the connector" in {
      val mockConnector = mock[ConstructionIndustrySchemeConnector]
      val service       = new PrepopService(mockConnector)

      val scheme = Scheme(123, "CIS-123", None, None, None, None)

      when(mockConnector.getScheme(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(scheme)))

      service.getScheme("CIS-123").futureValue mustBe Some(scheme)

      verify(mockConnector).getScheme(eqTo("CIS-123"))(any[HeaderCarrier])
      verifyNoMoreInteractions(mockConnector)
    }
  }

  "PrepopService.determineLandingDestination" should {

    val mockConnector = mock[ConstructionIndustrySchemeConnector]
    val service       = new PrepopService(mockConnector)

    val targetCall: Call                    = controllers.routes.ReturnsLandingController.onPageLoad("CIS-123")
    val addContractorDetailsCall: Call      = controllers.routes.AddContractorDetailsController.onPageLoad()
    val checkSubcontractorRecordsCall: Call =
      controllers.routes.CheckSubcontractorRecordsController.onPageLoad("163", "AB0063", "CIS-123", "returnDue")

    "return targetCall when prePopSuccessful is Y" in {
      val scheme = Scheme(1, "CIS-123", None, None, Some("Y"), None)
      service.determineLandingDestination(
        targetCall,
        "CIS-123",
        scheme,
        addContractorDetailsCall,
        checkSubcontractorRecordsCall
      ) mustBe targetCall
    }

    "return addContractorDetailsCall when exactly one of name/utr is present" in {
      val schemeNameOnly = Scheme(1, "CIS-123", None, Some("Name"), None, None)
      service.determineLandingDestination(
        targetCall,
        "CIS-123",
        schemeNameOnly,
        addContractorDetailsCall,
        checkSubcontractorRecordsCall
      ) mustBe addContractorDetailsCall

      val schemeUtrOnly = Scheme(1, "CIS-123", Some("123"), None, None, None)
      service.determineLandingDestination(
        targetCall,
        "CIS-123",
        schemeUtrOnly,
        addContractorDetailsCall,
        checkSubcontractorRecordsCall
      ) mustBe addContractorDetailsCall
    }

    "return checkSubcontractorRecordsCall when neither name nor utr is present and subCount is 0" in {
      val scheme = Scheme(1, "CIS-123", None, None, None, Some(0))
      service.determineLandingDestination(
        targetCall,
        "CIS-123",
        scheme,
        addContractorDetailsCall,
        checkSubcontractorRecordsCall
      ) mustBe checkSubcontractorRecordsCall
    }

    "return addContractorDetailsCall when neither name nor utr is present and subCount > 0" in {
      val scheme = Scheme(1, "CIS-123", None, None, None, Some(2))
      service.determineLandingDestination(
        targetCall,
        "CIS-123",
        scheme,
        addContractorDetailsCall,
        checkSubcontractorRecordsCall
      ) mustBe addContractorDetailsCall
    }
  }
}
