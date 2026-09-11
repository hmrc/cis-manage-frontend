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

import base.UnitSpec
import connectors.ConstructionIndustrySchemeConnector
import models.requests.GetSubmittedVerificationsRequest
import models.response.GetSubmittedVerificationsResponse
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, verifyNoMoreInteractions, when}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class VerificationServiceSpec extends UnitSpec with BeforeAndAfterEach {
  import repositories.VerificationHistoryCache

  private val cache            = mock[VerificationHistoryCache]
  private val connector        = mock[ConstructionIndustrySchemeConnector]
  private val dataBuilder      = new VerificationHistoryService
  private val serviceUnderTest = new VerificationService(cache, connector, dataBuilder)

  "getSubmittedVerifications must fetch verification history from" - {
    val instanceId   = "900063"
    val response     = GetSubmittedVerificationsResponse(
      scheme = Seq.empty,
      subcontractors = Seq.empty,
      verificationBatches = Seq.empty,
      verifications = Seq.empty,
      submissions = Seq.empty
    )
    val expectedData = dataBuilder.toVerificationHistoryData(response)

    "connector and populate the cache in non-blocking fashion (i.e. succeed even if cache fails) when" - {
      val request = GetSubmittedVerificationsRequest(instanceId)

      "cache is empty" in {
        when(cache.getFromCache(any)) thenReturn Future.successful(None)
        when(cache.putCache(any)(any)(any)) thenReturn Future.failed(new Exception("Failed to put in cache"))
        when(connector.getSubmittedVerifications(any)(any)) thenReturn Future.successful(response)

        val result = serviceUnderTest.getSubmittedVerifications(instanceId).futureValue
        result mustBe expectedData

        verify(cache).getFromCache(instanceId)
        verify(cache).putCache(eqTo(instanceId))(eqTo(expectedData))(any)
        verify(connector).getSubmittedVerifications(eqTo(request))(any[HeaderCarrier])
      }

      "cache throws an exception" in {
        when(cache.getFromCache(any)) thenReturn Future.failed(new Exception("Failed to get from cache"))
        when(cache.putCache(any)(any)(any)) thenReturn Future.failed(new Exception("Failed to put in cache"))
        when(connector.getSubmittedVerifications(any)(any)) thenReturn Future.successful(response)

        val result = serviceUnderTest.getSubmittedVerifications(instanceId).futureValue
        result mustBe expectedData

        verify(cache).getFromCache(instanceId)
        verify(cache).putCache(eqTo(instanceId))(eqTo(expectedData))(any)
        verify(connector).getSubmittedVerifications(eqTo(request))(any[HeaderCarrier])
      }
    }

    "cache when it is non-empty" in {
      when(cache.getFromCache(any)) thenReturn Future.successful(Some(expectedData))

      val result = serviceUnderTest.getSubmittedVerifications(instanceId).futureValue
      result mustBe expectedData

      verify(cache).getFromCache(instanceId)
    }
  }

  override def afterEach(): Unit =
    verifyNoMoreInteractions(cache, connector)
    reset(cache, connector)
}
