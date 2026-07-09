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

package services

import connectors.ConstructionIndustrySchemeConnector
import models.response.GetSubcontractorForDeleteResponse
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.{ExecutionContext, Future}

class SubcontractorServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar with ScalaFutures {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  private val cisId             = "123"
  private val subbieResourceRef = 10L

  "SubcontractorService#getSubcontractorDeleteStatus" - {

    "must return response from connector when successful" in {

      val mockConnector = mock[ConstructionIndustrySchemeConnector]

      val response =
        GetSubcontractorForDeleteResponse(
          subcontractorName = "Gamma Builders",
          subcontractorCanBeDeleted = true
        )

      when(
        mockConnector.getSubcontractorDeleteStatus(
          eqTo(cisId),
          eqTo(subbieResourceRef)
        )(any())
      ).thenReturn(Future.successful(response))

      val service = new SubcontractorService(mockConnector)

      val result =
        service
          .getSubcontractorDeleteStatus(cisId, subbieResourceRef)
          .futureValue

      result mustBe response

      verify(mockConnector)
        .getSubcontractorDeleteStatus(
          eqTo(cisId),
          eqTo(subbieResourceRef)
        )(any())
    }

    "must propagate failure from connector" in {

      val mockConnector = mock[ConstructionIndustrySchemeConnector]

      when(
        mockConnector.getSubcontractorDeleteStatus(
          eqTo(cisId),
          eqTo(subbieResourceRef)
        )(any())
      ).thenReturn(
        Future.failed(
          new RuntimeException("boom")
        )
      )

      val service = new SubcontractorService(mockConnector)

      val exception =
        service
          .getSubcontractorDeleteStatus(cisId, subbieResourceRef)
          .failed
          .futureValue

      exception.getMessage mustBe "boom"

      verify(mockConnector)
        .getSubcontractorDeleteStatus(
          eqTo(cisId),
          eqTo(subbieResourceRef)
        )(any())
    }
  }
}
