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

import base.SpecBase
import connectors.ConstructionIndustrySchemeConnector
import models.response.GetSubcontractorListResponse
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.*

class SubcontractorServiceSpec extends SpecBase with MockitoSugar {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val instanceId = "instance-id"

  "SubcontractorService" - {

    "must return the subcontractor list from the connector" in {
      val connector = mock[ConstructionIndustrySchemeConnector]
      val service   = new SubcontractorService(connector)

      val expectedResponse =
        GetSubcontractorListResponse(
          subcontractors = Seq.empty
        )

      when(connector.getSubcontractorList(instanceId))
        .thenReturn(Future.successful(expectedResponse))

      val result =
        Await.result(
          service.getSubcontractorList(instanceId),
          5.seconds
        )

      result mustEqual expectedResponse

      verify(connector).getSubcontractorList(instanceId)
    }
  }
}
