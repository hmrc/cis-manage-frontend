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
import models.requests.GetSubmittedVerificationsRequest
import models.response.GetSubmittedVerificationsResponse
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class VerificationServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val connector = mock[ConstructionIndustrySchemeConnector]
  private val service   = new VerificationService(connector)

  "VerificationService" - {

    "getSubmittedVerifications" - {

      "must call the connector with the submitted verifications request and return the response" in {
        val instanceId = "900063"

        val response = GetSubmittedVerificationsResponse(
          scheme = Seq.empty,
          subcontractors = Seq.empty,
          verificationBatches = Seq.empty,
          verifications = Seq.empty,
          submissions = Seq.empty
        )

        val request = GetSubmittedVerificationsRequest(instanceId)

        when(
          connector.getSubmittedVerifications(
            eqTo(request)
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(response))

        val result =
          Await.result(
            service.getSubmittedVerifications(instanceId),
            5.seconds
          )

        result mustBe response

        verify(connector).getSubmittedVerifications(
          eqTo(request)
        )(any[HeaderCarrier])
      }
    }
  }
}