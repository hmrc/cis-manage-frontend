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

import base.SpecBase
import connectors.ConstructionIndustrySchemeConnector
import models.GetClientListStatusResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.TryValues
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ConstructionIndustrySchemeServiceSpec extends SpecBase with TryValues {

  given hc: HeaderCarrier                            = HeaderCarrier()
  given ec: ExecutionContext                         = scala.concurrent.ExecutionContext.global
  val connector: ConstructionIndustrySchemeConnector = mock(classOf[ConstructionIndustrySchemeConnector])

  val service = new ConstructionIndustrySchemeService(connector)

  "startClientListRetrieval" - {

    "should get the success status from the connector response" in {
      when(connector.startClientList(using any[HeaderCarrier]))
        .thenReturn(Future.successful(GetClientListStatusResponse("succeeded")))

      val status = service.startClientListRetrieval.futureValue

      status mustBe "succeeded"
    }

    "should get the failed status from the connector response" in {
      when(connector.startClientList(using any[HeaderCarrier]))
        .thenReturn(Future.successful(GetClientListStatusResponse("failed")))

      val status = service.startClientListRetrieval.futureValue

      status mustBe "failed"
    }

    "should get the initiate-download status from the connector response" in {
      when(connector.startClientList(using any[HeaderCarrier]))
        .thenReturn(Future.successful(GetClientListStatusResponse("initiate-download")))

      val status = service.startClientListRetrieval.futureValue

      status mustBe "initiate-download"
    }

    "should get the in-progress status from the connector response" in {
      when(connector.startClientList(using any[HeaderCarrier]))
        .thenReturn(Future.successful(GetClientListStatusResponse("in-progress")))

      val status = service.startClientListRetrieval.futureValue

      status mustBe "in-progress"
    }
  }

  "getClientListStatus" - {
    "should get the success status from the connector response" in {
      when(connector.getClientListStatus(using any[HeaderCarrier]))
        .thenReturn(Future.successful(GetClientListStatusResponse("Success")))

      val status = service.getClientListStatus.futureValue

      status mustBe "Success"
    }

    "should get the failed status from the connector response" in {
      when(connector.getClientListStatus(using any[HeaderCarrier]))
        .thenReturn(Future.successful(GetClientListStatusResponse("Failed")))

      val status = service.getClientListStatus.futureValue

      status mustBe "Failed"
    }

    "should get the initiate download status from the connector response" in {
      when(connector.getClientListStatus(using any[HeaderCarrier]))
        .thenReturn(Future.successful(GetClientListStatusResponse("InitiateDownload")))

      val status = service.getClientListStatus.futureValue

      status mustBe "InitiateDownload"
    }

    "should get the in progress status from the connector response" in {
      when(connector.getClientListStatus(using any[HeaderCarrier]))
        .thenReturn(Future.successful(GetClientListStatusResponse("InProgress")))

      val status = service.getClientListStatus.futureValue

      status mustBe "InProgress"
    }
  }
}
