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

import models.*
import models.agent.AgentClientData
import models.history.*
import models.requests.*
import models.response.*
import models.verify.{SubcontractorVerificationData, VerificationHistoryData, VerificationRequestData, VerificationRequestDetailData}
import play.api.Logging
import play.api.http.Status.{NO_CONTENT, OK}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpReadsInstances, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.{LocalDate, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConstructionIndustrySchemeConnector @Inject() (config: ServicesConfig, http: HttpClientV2)(implicit
  ec: ExecutionContext
) extends HttpReadsInstances
    with Logging {

  private val cisBaseUrl: String = config.baseUrl("construction-industry-scheme") + "/cis"

  def getCisTaxpayer()(implicit hc: HeaderCarrier): Future[CisTaxpayer] =
    http
      .get(url"$cisBaseUrl/taxpayer")
      .execute[CisTaxpayer]

  def startClientList(using HeaderCarrier): Future[GetClientListStatusResponse] =
    http
      .post(url"$cisBaseUrl/agent/client-list/retrieval/start")
      .execute[GetClientListStatusResponse]

  def getClientListStatus(using HeaderCarrier): Future[GetClientListStatusResponse] =
    http
      .post(url"$cisBaseUrl/agent/client-list/retrieval/status")
      .execute[GetClientListStatusResponse]

  def getAllClients(implicit hc: HeaderCarrier): Future[List[CisTaxpayerSearchResult]] =
    http
      .get(url"$cisBaseUrl/agent/client-list")
      .execute[JsObject]
      .map { x =>
        val clientListJson = Json.fromJson[List[CisTaxpayerSearchResult]](x("clients"))

        clientListJson.get
      }

  def getAgentClientTaxpayer(taxOfficeNumber: String, taxOfficeReference: String)(implicit
    hc: HeaderCarrier
  ): Future[CisTaxpayer] =
    http
      .get(url"$cisBaseUrl/agent/client-taxpayer/$taxOfficeNumber/$taxOfficeReference")
      .execute[CisTaxpayer]

  def prepopulateContractorKnownFacts(
    instanceId: String,
    taxOfficeNumber: String,
    taxOfficeReference: String
  )(implicit hc: HeaderCarrier): Future[Unit] =
    http
      .post(url"$cisBaseUrl/contractor-known-facts/prepopulate/$taxOfficeNumber/$taxOfficeReference/$instanceId")
      .execute[HttpResponse]
      .flatMap { resp =>
        if (resp.status / 100 == 2) {
          Future.unit
        } else {
          Future.failed(
            UpstreamErrorResponse(resp.body, resp.status, resp.status)
          )
        }
      }

  def prepopulateContractorAndSubcontractors(
    taxOfficeNumber: String,
    taxOfficeReference: String,
    instanceId: String
  )(implicit hc: HeaderCarrier): Future[Unit] =
    http
      .post(url"$cisBaseUrl/scheme/prepopulate/$taxOfficeNumber/$taxOfficeReference/$instanceId")
      .execute[HttpResponse]
      .flatMap { resp =>
        resp.status match {
          case 204   =>
            Future.unit
          case other =>
            logger.warn(s"[prepopulateContractorAndSubcontractors] Unexpected status=$other body=${resp.body}")
            Future.failed(UpstreamErrorResponse(resp.body, other, other))
        }
      }

  def getScheme(instanceId: String)(implicit hc: HeaderCarrier): Future[Option[Scheme]] =
    http
      .get(url"$cisBaseUrl/scheme/$instanceId")
      .execute[HttpResponse]
      .map { resp =>
        resp.status match {
          case 200 =>
            val json: JsValue = resp.json
            Some(json.as[Scheme])

          case 404 =>
            None

          case other =>
            logger.warn(s"[getScheme] Unexpected status=$other body=${resp.body}")
            throw UpstreamErrorResponse(resp.body, other, other)
        }
      }

  def getUnsubmittedMonthlyReturns(
    instanceId: String
  )(implicit hc: HeaderCarrier): Future[UnsubmittedMonthlyReturnsResponse] =
    http
      .get(url"$cisBaseUrl/monthly-returns/unsubmitted/$instanceId")
      .execute[UnsubmittedMonthlyReturnsResponse]

  def getSubmittedMonthlyReturns(
    instanceId: String
  )(implicit hc: HeaderCarrier): Future[SubmittedReturnsData] =
    http
      .get(url"$cisBaseUrl/monthly-returns/submitted/$instanceId")
      .execute[SubmittedReturnsData]

  def getMonthlyReturnComplete(
    instanceId: String,
    taxYear: Int,
    taxMonth: Int,
    amendment: String
  )(implicit hc: HeaderCarrier): Future[MonthlyReturnCompleteResponse] =
    http
      .post(url"$cisBaseUrl/monthly-returns-complete")
      .withBody(
        Json.obj(
          "instanceId" -> instanceId,
          "taxYear"    -> taxYear,
          "taxMonth"   -> taxMonth,
          "amendment"  -> amendment
        )
      )
      .execute[MonthlyReturnCompleteResponse]

  def saveAgentClient(userId: String, agentClientData: AgentClientData)(implicit
    hc: HeaderCarrier
  ): Future[Unit] =
    http
      .post(url"$cisBaseUrl/user-cache/agent-client/$userId")
      .withBody(Json.toJson(agentClientData))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK => ()
          case _  => throw new HttpException(response.body, response.status)
        }
      }

  def deleteUnsubmittedMonthlyReturn(request: DeleteUnsubmittedMonthlyReturnRequest)(implicit
    hc: HeaderCarrier
  ): Future[Unit] =
    http
      .post(url"$cisBaseUrl/monthly-returns/unsubmitted/delete")
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case NO_CONTENT => Future.unit
          case status     => Future.failed(UpstreamErrorResponse(response.body, status, status))
        }
      }

  def getSubmittedMonthlyReturnsData(
    request: GetSubmittedMonthlyReturnsDataRequest
  )(implicit hc: HeaderCarrier): Future[GetSubmittedMonthlyReturnsDataResponse] =
    http
      .post(url"$cisBaseUrl/monthly-returns/submitted-data")
      .withBody(Json.toJson(request))
      .execute[GetSubmittedMonthlyReturnsDataResponse]

  def createJourneyHandoff(journeyType: String, data: JsObject)(implicit
    hc: HeaderCarrier
  ): Future[String] =
    http
      .post(url"$cisBaseUrl/journey-handoffs/$journeyType")
      .withBody(data)
      .execute[JourneyHandoffResponse]
      .map(_.id)

  // TODO: Replace stub with real API call when available
  def getVerificationHistory(
    instanceId: String
  )(implicit hc: HeaderCarrier): Future[VerificationHistoryData] =
    Future.successful(
      VerificationHistoryData(
        verificationRequests = Seq(
          VerificationRequestData("V0004528770", LocalDate.of(2027, 2, 6), 2026),
          VerificationRequestData("V0004528769", LocalDate.of(2026, 12, 6), 2026),
          VerificationRequestData("V0004528768", LocalDate.of(2026, 10, 6), 2026),
          VerificationRequestData("V0004528767", LocalDate.of(2026, 8, 6), 2026),
          VerificationRequestData("V0004528766", LocalDate.of(2026, 6, 6), 2026),
          VerificationRequestData("V0004528765", LocalDate.of(2026, 4, 6), 2026),
          VerificationRequestData("V0004528764", LocalDate.of(2026, 2, 6), 2025),
          VerificationRequestData("V0004528763", LocalDate.of(2025, 12, 6), 2025),
          VerificationRequestData("V0004528762", LocalDate.of(2025, 10, 6), 2025),
          VerificationRequestData("V0004528761", LocalDate.of(2025, 8, 6), 2025),
          VerificationRequestData("V0004528760", LocalDate.of(2025, 6, 6), 2025),
          VerificationRequestData("V0004528759", LocalDate.of(2025, 4, 6), 2025),
          VerificationRequestData("V0004528758", LocalDate.of(2025, 2, 6), 2024),
          VerificationRequestData("V0004528757", LocalDate.of(2024, 12, 6), 2024),
          VerificationRequestData("V0004528756", LocalDate.of(2024, 10, 6), 2024),
          VerificationRequestData("V0004528755", LocalDate.of(2024, 8, 6), 2024),
          VerificationRequestData("V0004528754", LocalDate.of(2024, 6, 6), 2024),
          VerificationRequestData("V0004528753", LocalDate.of(2024, 4, 6), 2024),
          VerificationRequestData("V0004528752", LocalDate.of(2024, 2, 6), 2023),
          VerificationRequestData("V0004528751", LocalDate.of(2023, 12, 6), 2023),
          VerificationRequestData("V0004528750", LocalDate.of(2023, 10, 6), 2023),
          VerificationRequestData("V0004528749", LocalDate.of(2023, 8, 6), 2023),
          VerificationRequestData("V0004528748", LocalDate.of(2023, 6, 6), 2023),
          VerificationRequestData("V0004528747", LocalDate.of(2023, 4, 6), 2023)
        )
      )
    )

  // TODO: Replace stub with real API call when available
  def getVerificationRequestDetail(
    instanceId: String,
    verificationNumber: String
  )(implicit hc: HeaderCarrier): Future[VerificationRequestDetailData] =
    Future.successful(
      VerificationRequestDetailData(
        verificationNumber = verificationNumber,
        dateTimeSubmitted = LocalDateTime.of(2027, 2, 6, 14, 30),
        subcontractorsToVerify = Seq(
          SubcontractorVerificationData("Amity Marine Contractors", "V0004528765"),
          SubcontractorVerificationData("Brody, Martin", "V0004528765"),
          SubcontractorVerificationData("Brody, Michael", "V0004528765"),
          SubcontractorVerificationData("Brody, Sean", "V0004528765"),
          SubcontractorVerificationData("Hooper and Associates", "V0004528765")
        ),
        subcontractorsToReverify = Seq(
          SubcontractorVerificationData("Orca Industrial", "V0004528765/L"),
          SubcontractorVerificationData("Quint Transportation", "V0004528765")
        )
      )
    )

  def getSubcontractorDeleteStatus(
    cisId: String,
    subbieResourceRef: Long
  )(implicit hc: HeaderCarrier): Future[GetSubcontractorForDeleteResponse] =
    http
      .get(url"$cisBaseUrl/subcontractor/$cisId/$subbieResourceRef/delete-status")
      .execute[GetSubcontractorForDeleteResponse]

}
