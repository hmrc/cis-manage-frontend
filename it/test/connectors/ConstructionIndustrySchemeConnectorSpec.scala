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

import com.github.tomakehurst.wiremock.client.WireMock.*
import itutil.ApplicationWithWiremock
import models.Scheme
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

class ConstructionIndustrySchemeConnectorSpec extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with ApplicationWithWiremock {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val connector: ConstructionIndustrySchemeConnector = app.injector.instanceOf[ConstructionIndustrySchemeConnector]

  "getCisTaxpayer" should {

    "return CisTaxpayer when BE returns 200 with valid JSON" in {
      stubFor(
        get(urlPathEqualTo("/cis/taxpayer"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """{
                  |  "uniqueId": "123",
                  |  "taxOfficeNumber": "111",
                  |  "taxOfficeRef": "test111",
                  |  "employerName1": "TEST LTD"
                  |}""".stripMargin
              )
          )
      )

      val result = connector.getCisTaxpayer().futureValue
      result.uniqueId mustBe "123"
      result.taxOfficeNumber mustBe "111"
      result.taxOfficeRef mustBe "test111"
      result.employerName1 mustBe Some("TEST LTD")
    }

    "fail when BE returns 200 with invalid JSON" in {
      stubFor(
        get(urlPathEqualTo("/cis/taxpayer"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.getCisTaxpayer().futureValue
      }
      ex.getMessage.toLowerCase must include("uniqueid")
    }

    "propagate an upstream error when BE returns 500" in {
      stubFor(
        get(urlPathEqualTo("/cis/taxpayer"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex = intercept[Exception] {
        connector.getCisTaxpayer().futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }

  "getAllClients" should {

    "return a list of CisTaxpayerSearchResult when BE returns 200 with valid JSON" in {
      stubFor(
        get(urlPathEqualTo("/cis/agent/client-list"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """{
                  |  "clients": [
                  |    {
                  |      "uniqueId": "123",
                  |      "taxOfficeNumber": "111",
                  |      "taxOfficeRef": "test111",
                  |      "agentOwnRef": "abc123",
                  |      "schemeName": "TEST LTD"
                  |    },
                  |    {
                  |      "uniqueId": "456",
                  |      "taxOfficeNumber": "222",
                  |      "taxOfficeRef": "test222",
                  |      "agentOwnRef": "abc456",
                  |      "schemeName": "ANOTHER LTD"
                  |    }
                  |  ]
                  |}""".stripMargin
              )
          )
      )

      val result = connector.getAllClients.futureValue
      result.length mustBe 2
      result.head.uniqueId mustBe "123"
      result.head.taxOfficeNumber mustBe "111"
      result.head.taxOfficeRef mustBe "test111"
      result.head.agentOwnRef mustBe Some("abc123")
      result.head.schemeName mustBe Some("TEST LTD")
      result(1).uniqueId mustBe "456"
      result(1).taxOfficeNumber mustBe "222"
      result(1).taxOfficeRef mustBe "test222"
      result(1).agentOwnRef mustBe Some("abc456")
      result(1).schemeName mustBe Some("ANOTHER LTD")
    }

    "return an empty list when BE returns 200 with empty clients array" in {
      stubFor(
        get(urlPathEqualTo("/cis/agent/client-list"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "clients": [] }""")
          )
      )

      val result = connector.getAllClients.futureValue
      result mustBe empty
    }

    "fail when BE returns 200 with invalid JSON structure" in {
      stubFor(
        get(urlPathEqualTo("/cis/agent/client-list"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.getAllClients.futureValue
      }
      ex.getMessage must (include("clients") or include("NoSuchElementException"))
    }

    "fail when BE returns 200 with invalid client JSON" in {
      stubFor(
        get(urlPathEqualTo("/cis/agent/client-list"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """{
                  |  "clients": [
                  |    { "invalidField": "invalid" }
                  |  ]
                  |}""".stripMargin
              )
          )
      )

      val ex = intercept[Exception] {
        connector.getAllClients.futureValue
      }
      ex.getMessage.toLowerCase must include("nosuchelementexception")
    }

    "propagate an upstream error when BE returns 500" in {
      stubFor(
        get(urlPathEqualTo("/cis/agent/client-list"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex = intercept[Exception] {
        connector.getAllClients.futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }

  "startClientList" should {

    "return GetClientListStatusResponse with 'succeeded' when BE returns succeeded" in {
      stubFor(
        post(urlPathEqualTo("/cis/agent/client-list/retrieval/start"))
          .willReturn(aResponse().withStatus(OK).withBody("""{ "result": "succeeded" }"""))
      )

      val result = connector.startClientList(using hc).futureValue
      result.result mustBe "succeeded"
    }

    "return GetClientListStatusResponse with 'in-progress' when BE returns in-progress" in {
      stubFor(
        post(urlPathEqualTo("/cis/agent/client-list/retrieval/start"))
          .willReturn(aResponse().withStatus(OK).withBody("""{ "result": "in-progress" }"""))
      )

      val result = connector.startClientList(using hc).futureValue
      result.result mustBe "in-progress"
    }

    "return GetClientListStatusResponse with 'failed' when BE returns failed" in {
      stubFor(
        post(urlPathEqualTo("/cis/agent/client-list/retrieval/start"))
          .willReturn(aResponse().withStatus(OK).withBody("""{ "result": "failed" }"""))
      )

      val result = connector.startClientList(using hc).futureValue
      result.result mustBe "failed"
    }

    "return GetClientListStatusResponse with 'initiate-download' when BE returns initiate-download" in {
      stubFor(
        post(urlPathEqualTo("/cis/agent/client-list/retrieval/start"))
          .willReturn(aResponse().withStatus(OK).withBody("""{ "result": "initiate-download" }"""))
      )

      val result = connector.startClientList(using hc).futureValue
      result.result mustBe "initiate-download"
    }

    "propagate an upstream error when BE returns 500" in {
      stubFor(
        post(urlPathEqualTo("/cis/agent/client-list/retrieval/start"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("""{ "result": "system-error" }"""))
      )

      val ex = intercept[Exception] {
        connector.startClientList(using hc).futureValue
      }
      ex.getMessage must include("returned 500")
    }

    "fail when BE returns 200 with invalid JSON" in {
      stubFor(
        post(urlPathEqualTo("/cis/agent/client-list/retrieval/start"))
          .willReturn(aResponse().withStatus(OK).withBody("""{ "unexpectedField": true }"""))
      )

      val ex = intercept[Exception] {
        connector.startClientList(using hc).futureValue
      }
      ex.getMessage.toLowerCase must include("result")
    }
  }

  "getClientListStatus" should {

    "return GetClientListStatusResponse with 'succeeded' when BE returns 200 with succeeded status" in {
      stubFor(
        post(urlPathEqualTo("/cis/agent/client-list/retrieval/status"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "result": "succeeded" }""")
          )
      )

      val result = connector.getClientListStatus(using hc).futureValue
      result.result mustBe "succeeded"
    }

    "return GetClientListStatusResponse with 'in-progress' when BE returns 200 with in-progress status" in {
      stubFor(
        post(urlPathEqualTo("/cis/agent/client-list/retrieval/status"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "result": "in-progress" }""")
          )
      )

      val result = connector.getClientListStatus(using hc).futureValue
      result.result mustBe "in-progress"
    }

    "return GetClientListStatusResponse with 'failed' when BE returns 200 with failed status" in {
      stubFor(
        post(urlPathEqualTo("/cis/agent/client-list/retrieval/status"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "result": "failed" }""")
          )
      )

      val result = connector.getClientListStatus(using hc).futureValue
      result.result mustBe "failed"
    }

    "return GetClientListStatusResponse with 'initiate-download' when BE returns 200 with initiate-download status" in {
      stubFor(
        post(urlPathEqualTo("/cis/agent/client-list/retrieval/status"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "result": "initiate-download" }""")
          )
      )

      val result = connector.getClientListStatus(using hc).futureValue
      result.result mustBe "initiate-download"
    }

    "propagate an upstream error when BE returns 500" in {
      stubFor(
        post(urlPathEqualTo("/cis/agent/client-list/retrieval/status"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("""{ "result": "system-error" }""")
          )
      )

      val ex = intercept[Exception] {
        connector.getClientListStatus(using hc).futureValue
      }
      ex.getMessage must include("returned 500")
    }

    "fail when BE returns 200 with invalid JSON" in {
      stubFor(
        post(urlPathEqualTo("/cis/agent/client-list/retrieval/status"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.getClientListStatus(using hc).futureValue
      }
      ex.getMessage.toLowerCase must include("result")
    }
  }

  "getAgentClientTaxpayer" should {

    "return CisTaxpayer when BE returns 200 with valid JSON" in {
      val taxOfficeNumber = "111"
      val taxOfficeRef = "test111"

      stubFor(
        get(urlPathEqualTo(s"/cis/agent/client-taxpayer/$taxOfficeNumber/$taxOfficeRef"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """{
                  |  "uniqueId": "123",
                  |  "taxOfficeNumber": "111",
                  |  "taxOfficeRef": "test111",
                  |  "employerName1": "TEST LTD"
                  |}""".stripMargin
              )
          )
      )

      val result = connector.getAgentClientTaxpayer(taxOfficeNumber,taxOfficeRef).futureValue

      result.uniqueId mustBe "123"
      result.taxOfficeNumber mustBe "111"
      result.taxOfficeRef mustBe "test111"
      result.employerName1 mustBe Some("TEST LTD")
    }

    "fail when BE returns 200 with invalid JSON" in {
      val taxOfficeNumber = "111"
      val taxOfficeRef = "test111"

      stubFor(
        get(urlPathEqualTo(s"/cis/agent/client-taxpayer/$taxOfficeNumber/$taxOfficeRef"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.getAgentClientTaxpayer(taxOfficeNumber,taxOfficeRef).futureValue
      }

      ex.getMessage.toLowerCase must include("uniqueid")
    }

    "propagate an upstream error when BE returns 500" in {
      val taxOfficeNumber = "111"
      val taxOfficeRef = "test111"

      stubFor(
        get(urlPathEqualTo(s"/cis/agent/client-taxpayer/$taxOfficeNumber/$taxOfficeRef"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("boom")
          )
      )

      val ex = intercept[Exception] {
        connector.getAgentClientTaxpayer(taxOfficeNumber,taxOfficeRef).futureValue
      }

      ex.getMessage must include("returned 500")
    }
  }

  "prepopulateContractorKnownFacts" should {

    "return successfully when BE returns 2xx" in {
      val instanceId = "900063"
      val taxOfficeNumber = "163"
      val taxOfficeRef = "AB0063"

      stubFor(
        post(urlPathEqualTo(s"/cis/contractor-known-facts/prepopulate/$taxOfficeNumber/$taxOfficeRef/$instanceId"))
          .willReturn(
            aResponse()
              .withStatus(NO_CONTENT)
          )
      )

      val result = connector
        .prepopulateContractorKnownFacts(instanceId, taxOfficeNumber, taxOfficeRef)
        .futureValue

      result mustBe ()

    }

    "propagate an upstream error when BE returns 500" in {
      val instanceId = "900063"
      val taxOfficeNumber = "163"
      val taxOfficeRef = "AB0063"

      stubFor(
        post(urlPathEqualTo(s"/cis/contractor-known-facts/prepopulate/$taxOfficeNumber/$taxOfficeRef/$instanceId"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("boom")
          )
      )

      val ex = intercept[Exception] {
        connector.prepopulateContractorKnownFacts(instanceId, taxOfficeNumber, taxOfficeRef).futureValue
      }

      ex.getMessage must include("boom")
    }

    "propagate an upstream error when BE returns PRECONDITION_FAILED" in {
      val instanceId = "900063"
      val taxOfficeNumber = "163"
      val taxOfficeRef = "AB0063"

      stubFor(
        post(urlPathEqualTo(s"/cis/contractor-known-facts/prepopulate/$taxOfficeNumber/$taxOfficeRef/$instanceId"))
          .willReturn(
            aResponse()
              .withStatus(PRECONDITION_FAILED)
              .withBody("""{"message":"CIS taxpayer not found"}""")
          )
      )

      val ex = intercept[Exception] {
        connector.prepopulateContractorKnownFacts(instanceId, taxOfficeNumber, taxOfficeRef).futureValue
      }

      ex.getMessage must include("CIS taxpayer not found")
    }
  }

  "prepopulateContractorAndSubcontractors" should {

    "return successfully when BE returns 204" in {
      val taxOfficeNumber = "163"
      val taxOfficeReference = "AB0063"
      val instanceId = "900063"

      stubFor(
        post(urlPathEqualTo(s"/cis/scheme/prepopulate/$taxOfficeNumber/$taxOfficeReference/$instanceId"))
          .willReturn(aResponse().withStatus(NO_CONTENT))
      )

      connector
        .prepopulateContractorAndSubcontractors(taxOfficeNumber, taxOfficeReference, instanceId)
        .futureValue mustBe ()
    }


    "fail when BE returns non-204" in {
      val taxOfficeNumber = "163"
      val taxOfficeReference = "AB0063"
      val instanceId = "900063"

      stubFor(
        post(urlPathEqualTo(s"/cis/scheme/prepopulate/$taxOfficeNumber/$taxOfficeReference/$instanceId"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex = connector
        .prepopulateContractorAndSubcontractors(taxOfficeNumber, taxOfficeReference, instanceId)
        .failed
        .futureValue

      ex mustBe a[UpstreamErrorResponse]
      ex.asInstanceOf[UpstreamErrorResponse].statusCode mustBe INTERNAL_SERVER_ERROR
      ex.getMessage must include("boom")
    }
  }


  "getScheme" should {

    "return Some(Scheme) when BE returns 200 with valid JSON" in {
      val instanceId = "900063"

      stubFor(
        get(urlPathEqualTo(s"/cis/scheme/$instanceId"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """{
                  |  "schemeId": 123,
                  |  "instanceId": "900063",
                  |  "utr": null,
                  |  "name": null,
                  |  "prePopSuccessful": null,
                  |  "subcontractorCounter": null
                  |}""".stripMargin
              )
          )
      )

      val result = connector.getScheme(instanceId).futureValue
      result mustBe Some(Scheme(123, "900063", None, None, None, None))
    }

    "return None when BE returns 404" in {
      val instanceId = "900063"

      stubFor(
        get(urlPathEqualTo(s"/cis/scheme/$instanceId"))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      connector.getScheme(instanceId).futureValue mustBe None
    }

    "propagate an upstream error when BE returns 500" in {
      val instanceId = "900063"

      stubFor(
        get(urlPathEqualTo(s"/cis/scheme/$instanceId"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex = connector.getScheme(instanceId).failed.futureValue
      ex mustBe a[UpstreamErrorResponse]
      ex.getMessage must include("boom")
    }
  }

  "getUnsubmittedMonthlyReturns" should {

    "return UnsubmittedMonthlyReturnsResponse when BE returns 200 with valid JSON" in {
      val instanceId = "900063"

      stubFor(
        get(urlPathEqualTo(s"/cis/monthly-returns/unsubmitted/$instanceId"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """{
                  |  "unsubmittedCisReturns": [
                  |    {
                  |      "taxYear": 2025,
                  |      "taxMonth": 1,
                  |      "returnType": "Nil",
                  |      "status": "PENDING",
                  |      "lastUpdate": null
                  |    }
                  |  ]
                  |}""".stripMargin
              )
          )
      )

      val result = connector.getUnsubmittedMonthlyReturns(instanceId).futureValue
      result.unsubmittedCisReturns.length mustBe 1
      result.unsubmittedCisReturns.head.taxYear mustBe 2025
      result.unsubmittedCisReturns.head.taxMonth mustBe 1
      result.unsubmittedCisReturns.head.returnType mustBe "Nil"
      result.unsubmittedCisReturns.head.status mustBe "PENDING"
      result.unsubmittedCisReturns.head.lastUpdate mustBe None
    }

    "propagate an upstream error when BE returns 500" in {
      val instanceId = "900063"

      stubFor(
        get(urlPathEqualTo(s"/cis/monthly-returns/unsubmitted/$instanceId"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex = intercept[Exception] {
        connector.getUnsubmittedMonthlyReturns(instanceId).futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }

}