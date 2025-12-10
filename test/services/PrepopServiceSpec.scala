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
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
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
}
