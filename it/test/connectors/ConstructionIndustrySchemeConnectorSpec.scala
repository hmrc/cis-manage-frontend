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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import models.GetClientListStatusResponse
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}

import scala.concurrent.ExecutionContext.Implicits.global

class ConstructionIndustrySchemeConnectorSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with WireMockSupport
    with HttpClientV2Support {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      "microservice.services.construction-industry-scheme.host" -> wireMockHost,
      "microservice.services.construction-industry-scheme.port" -> wireMockPort
    )
    .build()

  private lazy val connector: ConstructionIndustrySchemeConnector =
    app.injector.instanceOf[ConstructionIndustrySchemeConnector]

  "getClientListStatus" - {

    val url = "/cis/agent/client-list/retrieval/start"

    "must return GetClientListStatusResponse when the server returns 200 OK with valid JSON" in {

      val responseJson = Json.obj("result" -> "succeeded")

      wireMockServer.stubFor(
        post(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(responseJson.toString())
          )
      )

      val result = connector.getClientListStatus.futureValue

      result mustBe GetClientListStatusResponse("succeeded")

      wireMockServer.verify(
        postRequestedFor(urlEqualTo(url))
      )
    }

    "must return GetClientListStatusResponse for different status values" in {

      val statuses = Seq("succeded", "in-progress", "failed", "system-error")

      statuses.foreach { status =>
        val responseJson = Json.obj("result" -> status)

        wireMockServer.stubFor(
          post(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(responseJson.toString())
            )
        )

        val result = connector.getClientListStatus.futureValue

        result.result mustBe status
      }
    }

    "must handle 4xx client errors" in {

      wireMockServer.stubFor(
        post(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(400)
              .withBody("Bad Request")
          )
      )

      connector.getClientListStatus.failed.futureValue
    }

    "must handle 5xx server errors" in {

      wireMockServer.stubFor(
        post(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(500)
              .withBody("Internal Server Error")
          )
      )

      connector.getClientListStatus.failed.futureValue
    }

    "must handle invalid JSON response" in {

      wireMockServer.stubFor(
        post(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody("{invalid json}")
          )
      )

      connector.getClientListStatus.failed.futureValue
    }

    "must handle missing required fields in JSON" in {

      val responseJson = Json.obj("wrongField" -> "value")

      wireMockServer.stubFor(
        post(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(responseJson.toString())
          )
      )

      connector.getClientListStatus.failed.futureValue
    }
  }
}
