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
import org.scalatest.TryValues
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}

import scala.concurrent.{ExecutionContext, Future}

class ConstructionIndustrySchemeServiceSpec extends SpecBase with TryValues {

  given hc: HeaderCarrier                            = HeaderCarrier()
  given ec: ExecutionContext                         = scala.concurrent.ExecutionContext.global
  val connector: ConstructionIndustrySchemeConnector = mock(classOf[ConstructionIndustrySchemeConnector])

  val service = new ConstructionIndustrySchemeService(connector)

  "getClientListStatus" - {
    "should get the status from the connector response" in {
      when(connector.getClientListStatus(using any[HeaderCarrier]))
        .thenReturn(Future.successful(GetClientListStatusResponse("success")))

      val status = service.getClientListStatus.futureValue

      status mustBe "succsss"
    }
  }

}
