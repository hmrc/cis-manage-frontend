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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import itutil.{ApplicationWithWiremock, WireMockConstants}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import play.api.http.Status.*
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

class RateLimitedAllowListConnectorSpec
    extends ApplicationWithWiremock
    with Matchers
    with ScalaFutures
    with IntegrationPatience {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override val extraConfig: Map[String, Any] = super.extraConfig ++ Map(
    "microservice.services.rate-limited-allow-list.host" -> WireMockConstants.stubHost,
    "microservice.services.rate-limited-allow-list.port" -> WireMockConstants.stubPort
  )

  val connector: RateLimitedAllowListConnector = app.injector.instanceOf[RateLimitedAllowListConnector]

  private val serviceName  = "cis-management-frontend"
  private val featureName  = "cis-private-beta-2026"
  private val identifier   = "754/EZ00047"
  private val expectedPath = s"/rate-limited-allow-list/services/$serviceName/features/$featureName"
  private val expectedBody = s"""{"identifier":"$identifier"}"""

  "checkAllowList" should {

    "return true when the service responds with included: true" in {
      stubFor(
        post(urlPathEqualTo(expectedPath))
          .withHeader("Content-Type", containing("application/json"))
          .withRequestBody(equalToJson(expectedBody))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody("""{"included":true}""")
          )
      )

      val result = connector.checkAllowList(serviceName, featureName, identifier).futureValue
      result mustBe true
    }

    "return false when the service responds with included: false" in {
      stubFor(
        post(urlPathEqualTo(expectedPath))
          .withHeader("Content-Type", containing("application/json"))
          .withRequestBody(equalToJson(expectedBody))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody("""{"included":false}""")
          )
      )

      val result = connector.checkAllowList(serviceName, featureName, identifier).futureValue
      result mustBe false
    }

    "propagate an upstream error when the service returns 500" in {
      stubFor(
        post(urlPathEqualTo(expectedPath))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("boom")
          )
      )

      val ex = connector.checkAllowList(serviceName, featureName, identifier).failed.futureValue
      ex mustBe a[UpstreamErrorResponse]
      ex.asInstanceOf[UpstreamErrorResponse].statusCode mustBe INTERNAL_SERVER_ERROR
    }

    "propagate an upstream error when the service returns 404" in {
      stubFor(
        post(urlPathEqualTo(expectedPath))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
              .withBody("""{"message":"feature not found"}""")
          )
      )

      val ex = connector.checkAllowList(serviceName, featureName, identifier).failed.futureValue
      ex mustBe a[UpstreamErrorResponse]
      ex.asInstanceOf[UpstreamErrorResponse].statusCode mustBe NOT_FOUND
    }

    "send the correct service name and feature name in the URL path" in {
      val altService  = "other-service"
      val altFeature  = "other-feature"
      val altPath     = s"/rate-limited-allow-list/services/$altService/features/$altFeature"

      stubFor(
        post(urlPathEqualTo(altPath))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody("""{"included":true}""")
          )
      )

      connector.checkAllowList(altService, altFeature, identifier).futureValue mustBe true

      verify(postRequestedFor(urlPathEqualTo(altPath)))
    }
  }
}
