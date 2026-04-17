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

import config.FrontendAppConfig
import connectors.ConstructionIndustrySchemeConnector
import models.agent.AgentClientData
import models.history.{SubmittedMonthlyReturnData, SubmittedReturnsData, SubmittedSchemeData, SubmittedSubmissionData}
import models.requests.DeleteUnsubmittedMonthlyReturnRequest
import models.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pages.*
import repositories.{SessionRepository, UnsubmittedMonthlyReturnRepository}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.agent.AgentLandingViewModel

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, LocalDateTime, ZoneId}
import scala.jdk.CollectionConverters.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

class ManageServiceSpec extends AnyWordSpec with ScalaFutures with Matchers {

  implicit val hc: HeaderCarrier            = HeaderCarrier()
  implicit val ec: ExecutionContext         = global
  implicit val appConfig: FrontendAppConfig = mock(classOf[FrontendAppConfig])

  private def newService(): (
    ManageService,
    ConstructionIndustrySchemeConnector,
    SessionRepository,
    UnsubmittedMonthlyReturnRepository,
    Clock
  ) = {
    val connector                    = mock(classOf[ConstructionIndustrySchemeConnector])
    val sessionRepo                  = mock(classOf[SessionRepository])
    val unsubmittedMonthlyReturnRepo = mock(classOf[UnsubmittedMonthlyReturnRepository])
    val instant                      = Instant.now.truncatedTo(ChronoUnit.MILLIS)
    val stubClock: Clock             = Clock.fixed(instant, ZoneId.systemDefault)
    val service                      = new ManageService(connector, sessionRepo, unsubmittedMonthlyReturnRepo, stubClock)
    (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, stubClock)
  }

  private def createTaxpayer(
    id: String = "CIS-123",
    ton: String = "111",
    tor: String = "test111",
    name1: Option[String] = Some("TEST LTD"),
    schemeName: Option[String] = Some("ABC Construction LTD"),
    utr: Option[String] = Some("1234567890")
  ): CisTaxpayer =
    CisTaxpayer(
      uniqueId = id,
      taxOfficeNumber = ton,
      taxOfficeRef = tor,
      aoDistrict = None,
      aoPayType = None,
      aoCheckCode = None,
      aoReference = None,
      validBusinessAddr = None,
      correlation = None,
      ggAgentId = None,
      employerName1 = name1,
      employerName2 = None,
      agentOwnRef = None,
      schemeName = schemeName,
      utr = utr,
      enrolledSig = None
    )

  "resolveAndStoreCisId" should {

    "return existing cisId from UserAnswers without calling BE" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()

      val existing    = "CIS-001"
      val emptyUa     = UserAnswers("test-user")
      val uaWithCisId = emptyUa.set(CisIdPage, existing).get

      val (cisId, savedUa) = service.resolveAndStoreCisId(uaWithCisId).futureValue
      cisId mustBe existing
      savedUa mustBe uaWithCisId

      verifyNoInteractions(connector)
      verifyNoInteractions(sessionRepo)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }

    "fetch taxpayer when missing, store cisId in session, and return updated UA" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()

      val emptyUa  = UserAnswers("test-user")
      val taxpayer = createTaxpayer()

      when(connector.getCisTaxpayer()(any[HeaderCarrier]))
        .thenReturn(Future.successful(taxpayer))
      when(sessionRepo.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val (cisId, savedUa) = service.resolveAndStoreCisId(emptyUa).futureValue

      cisId mustBe "CIS-123"
      savedUa.get(CisIdPage) mustBe Some("CIS-123")
      savedUa.get(ContractorNamePage) mustBe Some("ABC Construction LTD")
      savedUa.get(EmployerReferencePage) mustBe Some("111/test111")
      savedUa.get(UniqueTaxReferencePage) mustBe Some("1234567890")

      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(sessionRepo).set(uaCaptor.capture())

      val persistedUa = uaCaptor.getValue
      persistedUa.get(CisIdPage) mustBe Some("CIS-123")
      persistedUa.get(ContractorNamePage) mustBe Some("ABC Construction LTD")
      persistedUa.get(EmployerReferencePage) mustBe Some("111/test111")
      persistedUa.get(UniqueTaxReferencePage) mustBe Some("1234567890")

      verify(connector).getCisTaxpayer()(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }

    "fail when BE returns empty uniqueId" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()
      val emptyUa                                                            = UserAnswers("test-user")

      val emptyTaxpayer = createTaxpayer(id = " ", name1 = None)

      when(connector.getCisTaxpayer()(any[HeaderCarrier]))
        .thenReturn(Future.successful(emptyTaxpayer))

      val ex = intercept[RuntimeException] {
        service.resolveAndStoreCisId(emptyUa).futureValue
      }
      ex.getMessage must include("Empty cisId (uniqueId) returned from /cis/taxpayer")

      verify(connector).getCisTaxpayer()(any[HeaderCarrier])
      verifyNoInteractions(sessionRepo)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }

    "fail when adding cisId to UserAnswers returns an error" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()

      val taxpayer = createTaxpayer()
      when(connector.getCisTaxpayer()(any[HeaderCarrier]))
        .thenReturn(Future.successful(taxpayer))

      val ua = mock(classOf[UserAnswers])
      when(ua.get(CisIdPage)).thenReturn(None)
      when(ua.set(CisIdPage, "CIS-123"))
        .thenReturn(Failure(new RuntimeException("UA set failed")))

      val ex = intercept[RuntimeException] {
        service.resolveAndStoreCisId(ua).futureValue
      }
      ex.getMessage must include("UA set failed")

      verifyNoInteractions(sessionRepo)
      verify(connector).getCisTaxpayer()(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }
  }

  private def createClient(
    id: String = "CLIENT-123",
    ton: String = "111",
    tor: String = "test111",
    utr: Option[String] = Some("1234567890")
  ): CisTaxpayerSearchResult =
    CisTaxpayerSearchResult(
      uniqueId = id,
      taxOfficeNumber = ton,
      taxOfficeRef = tor,
      agentOwnRef = Some("abc123"),
      schemeName = None,
      utr = utr
    )

  "resolveAndStoreAgentClients" should {

    "return existing clients from UserAnswers without calling BE" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()

      val existingClients = List(createClient("CLIENT-001"), createClient("CLIENT-002"))
      val emptyUa         = UserAnswers("test-user")
      val uaWithClients   = emptyUa.set(AgentClientsPage, existingClients).get

      val (clients, savedUa) = service.resolveAndStoreAgentClients(uaWithClients)(using hc).futureValue
      clients mustBe existingClients
      savedUa mustBe uaWithClients

      verifyNoInteractions(connector)
      verifyNoInteractions(sessionRepo)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }

    "fetch clients when missing, store in session, and return updated UA" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()

      val emptyUa       = UserAnswers("test-user")
      val clientsFromBe = List(createClient("CLIENT-001"), createClient("CLIENT-002"))

      when(connector.getAllClients(any[HeaderCarrier]))
        .thenReturn(Future.successful(clientsFromBe))
      when(sessionRepo.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val (clients, savedUa) = service.resolveAndStoreAgentClients(emptyUa)(using hc).futureValue

      clients mustBe clientsFromBe
      savedUa.get(AgentClientsPage) mustBe Some(clientsFromBe)

      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(sessionRepo).set(uaCaptor.capture())
      uaCaptor.getValue.get(AgentClientsPage) mustBe Some(clientsFromBe)

      verify(connector).getAllClients(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }

    "return empty list when BE returns no clients" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()

      val emptyUa      = UserAnswers("test-user")
      val emptyClients = List.empty[CisTaxpayerSearchResult]

      when(connector.getAllClients(any[HeaderCarrier]))
        .thenReturn(Future.successful(emptyClients))
      when(sessionRepo.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val (clients, savedUa) = service.resolveAndStoreAgentClients(emptyUa)(using hc).futureValue

      clients mustBe empty
      savedUa.get(AgentClientsPage) mustBe Some(emptyClients)

      verify(connector).getAllClients(any[HeaderCarrier])
      verify(sessionRepo).set(any[UserAnswers])
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }

    "fail when BE call fails" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()

      val emptyUa = UserAnswers("test-user")

      when(connector.getAllClients(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("Backend error")))

      val ex = intercept[RuntimeException] {
        service.resolveAndStoreAgentClients(emptyUa)(using hc).futureValue
      }
      ex.getMessage must include("Backend error")

      verify(connector).getAllClients(any[HeaderCarrier])
      verifyNoInteractions(sessionRepo)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }

    "fail when session repository fails to save" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()

      val emptyUa       = UserAnswers("test-user")
      val clientsFromBe = List(createClient())

      when(connector.getAllClients(any[HeaderCarrier]))
        .thenReturn(Future.successful(clientsFromBe))
      when(sessionRepo.set(any[UserAnswers]))
        .thenReturn(Future.failed(new RuntimeException("Session save failed")))

      val ex = intercept[RuntimeException] {
        service.resolveAndStoreAgentClients(emptyUa)(using hc).futureValue
      }
      ex.getMessage must include("Session save failed")

      verify(connector).getAllClients(any[HeaderCarrier])
      verify(sessionRepo).set(any[UserAnswers])
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }
  }

  "getAgentLandingData" should {

    val userId = "some-user-id"

    "fail when AgentClientsPage is missing from UserAnswers" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()
      val ua                                                                 = UserAnswers("test-user")

      val ex = intercept[RuntimeException] {
        service.getAgentLandingData("CLIENT-001", ua, userId).futureValue
      }

      ex.getMessage must include("AgentClientsPage missing in UserAnswers")
      verifyNoInteractions(connector)
      verifyNoInteractions(sessionRepo)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }

    "fail when the client with given uniqueId is not present in AgentClientsPage" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()

      val clients      = List(createClient("CLIENT-001"), createClient("CLIENT-002"))
      val uaWithClient = UserAnswers("test-user").set(AgentClientsPage, clients).get

      val ex = intercept[RuntimeException] {
        service.getAgentLandingData("OTHER-ID", uaWithClient, userId).futureValue
      }

      ex.getMessage must include("Client with uniqueId=OTHER-ID not found in AgentClientsPage")
      verifyNoInteractions(connector)
      verifyNoInteractions(sessionRepo)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }

    "fetch taxpayer, update client UTR in session, and return AgentLandingViewModel" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()
      val uniqueId                                                           = "CLIENT-123"
      val baseClient                                                         = createClient(id = uniqueId, utr = None)
        .copy(schemeName = Some("ABC Construction Ltd"))
      val otherClient                                                        = createClient(id = "CLIENT-999")

      val clients  = List(baseClient, otherClient)
      val ua       = UserAnswers("test-user").set(AgentClientsPage, clients).get
      val taxpayer =
        createTaxpayer(id = uniqueId, name1 = Some("ABC Construction Ltd"))
          .copy(utr = Some("5555555555"))

      when(connector.getAgentClientTaxpayer(any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(taxpayer))

      when(sessionRepo.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      when(connector.saveAgentClient(any[String], any[AgentClientData])(using any()))
        .thenReturn(Future.unit)

      val result: AgentLandingViewModel =
        service.getAgentLandingData(uniqueId, ua, userId).futureValue

      result.clientName mustBe "ABC Construction Ltd"
      result.employerRef mustBe "111/test111"
      result.utr mustBe Some("5555555555")

      val uaCaptor: ArgumentCaptor[UserAnswers] =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(sessionRepo).set(uaCaptor.capture())

      val storedClientsOpt =
        uaCaptor.getValue.get(AgentClientsPage)

      storedClientsOpt.isDefined mustBe true
      val storedClients = storedClientsOpt.get

      val updatedClientOpt = storedClients.find(_.uniqueId == uniqueId)
      updatedClientOpt.flatMap(_.utr) mustBe Some("5555555555")

      val agentClientData = AgentClientData("CLIENT-123", "111", "test111", Some("ABC Construction Ltd"))
      verify(connector).getAgentClientTaxpayer("111", "test111")(hc)
      verify(connector).saveAgentClient("some-user-id", agentClientData)(hc)
      verifyNoMoreInteractions(connector)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }
  }

  "getUnsubmittedMonthlyReturns" should {

    "delegate to connector and return response (happy path)" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()
      val instanceId                                                         = "900063"

      val resp = UnsubmittedMonthlyReturnsResponse(
        unsubmittedCisReturns = Seq(
          UnsubmittedMonthlyReturnsRow(
            monthlyReturnId = 3000L,
            taxYear = 2025,
            taxMonth = 1,
            returnType = "Nil",
            status = "PENDING",
            lastUpdate = None,
            amendment = Some("Y"),
            deletable = true
          )
        )
      )

      when(connector.getUnsubmittedMonthlyReturns(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(resp))

      service.getUnsubmittedMonthlyReturns(instanceId).futureValue mustBe resp

      verify(connector).getUnsubmittedMonthlyReturns(eqTo(instanceId))(any[HeaderCarrier])
      verifyNoInteractions(sessionRepo)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }

    "propagate failure from connector" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()
      val instanceId                                                         = "900063"
      val boom                                                               = new RuntimeException("Backend error")

      when(connector.getUnsubmittedMonthlyReturns(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(boom))

      val ex = service.getUnsubmittedMonthlyReturns(instanceId).failed.futureValue
      ex mustBe boom

      verify(connector).getUnsubmittedMonthlyReturns(eqTo(instanceId))(any[HeaderCarrier])
      verifyNoInteractions(sessionRepo)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }
  }

  "getSubmittedMonthlyReturns" should {

    "delegate to connector and return response (happy path)" in {
      val (service, connector, sessionRepo) = newService()
      val instanceId                        = "900063"

      val resp = SubmittedReturnsData(
        scheme = SubmittedSchemeData(
          name = "ABC Construction Ltd",
          taxOfficeNumber = "123",
          taxOfficeReference = "AB456"
        ),
        monthlyReturns = Seq(
          SubmittedMonthlyReturnData(
            monthlyReturnId = 1L,
            taxYear = 2025,
            taxMonth = 1,
            nilReturnIndicator = "Y",
            status = "SUBMITTED",
            supersededBy = None,
            amendmentStatus = None,
            monthlyReturnItems = None
          )
        ),
        submissions = Seq(
          SubmittedSubmissionData(
            submissionId = 100L,
            submissionType = Some("MONTHLY_RETURN"),
            activeObjectId = Some(1L),
            status = "ACCEPTED",
            hmrcMarkGenerated = None,
            hmrcMarkGgis = None,
            emailRecipient = Some("test@test.com"),
            acceptedTime = None
          )
        )
      )

      when(connector.getSubmittedMonthlyReturns(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(resp))

      service.getSubmittedMonthlyReturns(instanceId).futureValue mustBe resp

      verify(connector).getSubmittedMonthlyReturns(eqTo(instanceId))(any[HeaderCarrier])
      verifyNoInteractions(sessionRepo)
    }

    "propagate failure from connector" in {
      val (service, connector, sessionRepo) = newService()
      val instanceId                        = "900063"
      val boom                              = new RuntimeException("Backend error")

      when(connector.getSubmittedMonthlyReturns(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(boom))

      val ex = service.getSubmittedMonthlyReturns(instanceId).failed.futureValue
      ex mustBe boom

      verify(connector).getSubmittedMonthlyReturns(eqTo(instanceId))(any[HeaderCarrier])
      verifyNoInteractions(sessionRepo)
    }
  }

  "buildReturnsLandingContext" should {

    "return Some(context) for agent when name + client exist and connector returns returns" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, clock) = newService()

      val fixedInstant = Instant.now(clock)

      val fixedDateTime =
        LocalDateTime.ofInstant(fixedInstant, ZoneId.systemDefault())

      when(appConfig.fileStandardReturnUrl(any[String])).thenReturn("/standard")
      when(appConfig.fileNilReturnUrl(any[String])).thenReturn("/nil")

      val instanceId = "CLIENT-123"
      val client     = createClient(id = instanceId).copy(schemeName = Some("Client Ltd"))

      val ua = UserAnswers("test-user").set(AgentClientsPage, List(client)).get

      val mockResponse = UnsubmittedMonthlyReturnsResponse(
        unsubmittedCisReturns = Seq(
          UnsubmittedMonthlyReturnsRow(3000L, 2025, 1, "Nil", "PENDING", None, Some("Y"), true),
          UnsubmittedMonthlyReturnsRow(3001L, 2025, 2, "Nil", "STARTED", Some(fixedDateTime), Some("Y"), true)
        )
      )

      when(connector.getUnsubmittedMonthlyReturns(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(mockResponse))
      when(unsubmittedMonthlyReturnRepo.upsert(any()))
        .thenReturn(Future.successful(()))

      val captor = ArgumentCaptor.forClass(classOf[UnsubmittedMonthlyReturn])

      val context = service.buildReturnsLandingContext(instanceId, ua, isAgent = true).futureValue

      context.isDefined mustBe true
      context.get.contractorName mustBe "Client Ltd"
      context.get.standardReturnLink mustBe "/standard"
      context.get.nilReturnLink mustBe "/nil"

      verify(connector).getUnsubmittedMonthlyReturns(eqTo(instanceId))(any[HeaderCarrier])
      verifyNoInteractions(sessionRepo)
      verify(unsubmittedMonthlyReturnRepo, times(2)).upsert(captor.capture())

      val savedReturns = captor.getAllValues.asScala

      savedReturns.foreach { saved =>
        saved.lastUpdated mustBe fixedInstant
      }
    }

    "return None for agent when client missing" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()

      val ua = UserAnswers("test-user").set(AgentClientsPage, List(createClient("OTHER"))).get

      val context = service.buildReturnsLandingContext("CLIENT-123", ua, isAgent = true).futureValue
      context mustBe None

      verifyNoInteractions(connector)
      verifyNoInteractions(sessionRepo)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }

    "return Some(context) for org when ContractorNamePage exists" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()

      when(appConfig.fileStandardReturnUrl).thenReturn("/standard-org")
      when(appConfig.fileNilReturnUrl).thenReturn("/nil-org")

      val ua = UserAnswers("test-user").set(ContractorNamePage, "Org Ltd").get

      val resp = UnsubmittedMonthlyReturnsResponse(Nil)
      when(connector.getUnsubmittedMonthlyReturns(eqTo("CIS-123"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(resp))

      val context = service.buildReturnsLandingContext("CIS-123", ua, isAgent = false).futureValue

      context.isDefined mustBe true
      context.get.contractorName mustBe "Org Ltd"
      context.get.standardReturnLink mustBe "/standard-org"
      context.get.nilReturnLink mustBe "/nil-org"

      verify(connector).getUnsubmittedMonthlyReturns(eqTo("CIS-123"))(any[HeaderCarrier])
      verifyNoInteractions(sessionRepo)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }

    "return None for org when ContractorNamePage missing" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()

      val ua = UserAnswers("test-user")

      val context = service.buildReturnsLandingContext("CIS-123", ua, isAgent = false).futureValue
      context mustBe None

      verifyNoInteractions(connector)
      verifyNoInteractions(sessionRepo)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }
  }

  "deleteUnsubmittedMonthlyReturn" should {

    "delegate to connector and return response (happy path)" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()
      val instanceId                                                         = "900063"
      val now: Instant                                                       = Instant.parse("2026-04-09T12:34:56.789Z")

      val dataModel = UnsubmittedMonthlyReturn(
        instanceId = instanceId,
        monthlyReturnId = 3000L,
        taxYear = 2026,
        taxMonth = 4,
        returnType = "Nil",
        status = "In Progress",
        amendment = Some("Y"),
        deletable = true,
        lastUpdated = now
      )

      val expectedRequest = DeleteUnsubmittedMonthlyReturnRequest(
        instanceId = instanceId,
        taxYear = 2026,
        taxMonth = 4,
        amendment = "Y"
      )

      when(connector.deleteUnsubmittedMonthlyReturn(eqTo(expectedRequest))(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))

      service.deleteUnsubmittedMonthlyReturn(dataModel).futureValue mustBe ()

      verify(connector).deleteUnsubmittedMonthlyReturn(eqTo(expectedRequest))(any[HeaderCarrier])
      verifyNoInteractions(sessionRepo)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }

    "propagate failure from connector" in {
      val (service, connector, sessionRepo, unsubmittedMonthlyReturnRepo, _) = newService()
      val instanceId                                                         = "900063"
      val boom                                                               = new RuntimeException("Backend error")
      val now: Instant                                                       = Instant.parse("2026-04-09T12:34:56.789Z")

      val dataModel = UnsubmittedMonthlyReturn(
        instanceId = instanceId,
        monthlyReturnId = 3000L,
        taxYear = 2026,
        taxMonth = 4,
        returnType = "Nil",
        status = "In Progress",
        amendment = Some("Y"),
        deletable = true,
        lastUpdated = now
      )

      val expectedRequest = DeleteUnsubmittedMonthlyReturnRequest(
        instanceId = instanceId,
        taxYear = 2026,
        taxMonth = 4,
        amendment = "Y"
      )

      when(connector.deleteUnsubmittedMonthlyReturn(eqTo(expectedRequest))(any[HeaderCarrier]))
        .thenReturn(Future.failed(boom))

      val ex = service.deleteUnsubmittedMonthlyReturn(dataModel).failed.futureValue
      ex mustBe boom

      verify(connector).deleteUnsubmittedMonthlyReturn(eqTo(expectedRequest))(any[HeaderCarrier])
      verifyNoInteractions(sessionRepo)
      verifyNoInteractions(unsubmittedMonthlyReturnRepo)
    }
  }
}
