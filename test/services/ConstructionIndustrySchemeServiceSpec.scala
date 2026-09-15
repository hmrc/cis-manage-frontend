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
import models.agent.{ClientListStatus, HasClientResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ConstructionIndustrySchemeServiceSpec extends SpecBase {

  given hc: HeaderCarrier = HeaderCarrier()

  given ec: ExecutionContext = ExecutionContext.global

  val connector: ConstructionIndustrySchemeConnector =
    mock(classOf[ConstructionIndustrySchemeConnector])

  val service =
    new ConstructionIndustrySchemeService(connector)

  "startClientListRetrieval" - {

    "should return the status from the connector response" in {
      when(connector.startClientList(using any[HeaderCarrier]))
        .thenReturn(
          Future.successful(
            GetClientListStatusResponse(ClientListStatus.Succeeded)
          )
        )

      service.startClientListRetrieval.futureValue mustBe
        ClientListStatus.Succeeded
    }
  }

  "getClientListStatus" - {

    "should return the status from the connector response" in {
      when(connector.getClientListStatus(using any[HeaderCarrier]))
        .thenReturn(
          Future.successful(
            GetClientListStatusResponse(ClientListStatus.InProgress)
          )
        )

      service.getClientListStatus.futureValue mustBe
        ClientListStatus.InProgress
    }
  }

  "hasClient" - {

    "should return the hasClient value from the connector response" in {
      when(
        connector.hasClient(
          "163",
          "AB0063"
        )(using hc)
      ).thenReturn(
        Future.successful(
          HasClientResponse(hasClient = true)
        )
      )

      service.hasClient("163", "AB0063").futureValue mustBe true
    }
  }
}
