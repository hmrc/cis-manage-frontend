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

package viewmodels.agent

import base.SpecBase
import models.CisTaxpayerSearchResult
import org.scalatest.matchers.should.Matchers.*
import play.api.i18n.Messages
import viewmodels.agent.ClientStatus.{Active, InActive}

class ClientListViewModelSpec extends SpecBase {

  implicit val messages: Messages = play.api.i18n.MessagesImpl(
    play.api.i18n.Lang.defaultLang,
    app.injector.instanceOf[play.api.i18n.MessagesApi]
  )

  private val sampleClients: Seq[ClientListViewModel] = Seq(
    ClientListViewModel("ABC Construction Ltd", "123/AB45678", "ABC-001", Active),
    ClientListViewModel("ABC Property Services", "789/EF23456", "ABC-002", Active),
    ClientListViewModel("Capital Construction Group", "345/IJ67890", "CAP-001", Active)
  )

  private def cisClient(
    schemeName: Option[String] = Some("Some Scheme"),
    ton: String = "123",
    tor: String = "AB45678",
    agentOwnRef: Option[String] = Some("AOR-001")
  ): CisTaxpayerSearchResult =
    CisTaxpayerSearchResult(
      uniqueId = "UID-1",
      taxOfficeNumber = ton,
      taxOfficeRef = tor,
      agentOwnRef = agentOwnRef,
      schemeName = schemeName
    )

  "ClientListViewModel.removeLink" - {
    "return a remove link when status is Active" in {
      val model  = ClientListViewModel(
        clientName = "Test",
        employerReference = "123/AA12345",
        clientReference = "TEST-001",
        clientStatus = Active
      )
      val result = model.removeLink
      result.isDefined shouldBe true
      result.get.text  shouldBe messages("agent.clientListSearch.td.actions.remove")
      result.get.href  shouldBe "#"
    }

    "return None when status is NOT Active" in {
      val model = ClientListViewModel(
        clientName = "Test",
        employerReference = "123/AA12345",
        clientReference = "TEST-002",
        clientStatus = InActive
      )
      model.removeLink shouldBe None
    }
  }

  "ClientListViewModel.clientLink" - {
    "always return a link" in {
      val model  = ClientListViewModel(
        clientName = "Test",
        employerReference = "123/AA12345",
        clientReference = "TEST-003",
        clientStatus = Active
      )
      val result = model.clientLink
      result.isDefined shouldBe true
      result.get.text  shouldBe ""
      result.get.href  shouldBe ""
    }
  }

  "ClientListViewModel.filterByField" - {
    "return all clients when query is empty" in {
      val result = ClientListViewModel.filterByField("CR", "", sampleClients)
      result shouldBe sampleClients
    }
    "filter by client reference when field = CR" in {
      val result = ClientListViewModel.filterByField("CR", "abc-002", sampleClients)
      result.map(_.clientReference) shouldBe Seq("ABC-002")
    }
    "filter by employer reference when field = ER" in {
      val result = ClientListViewModel.filterByField("ER", "345/ij", sampleClients)
      result.map(_.employerReference) shouldBe Seq("345/IJ67890")
    }
    "filter by client name when field is anything else" in {
      val result = ClientListViewModel.filterByField("NAME", "construction", sampleClients)
      result.map(_.clientName) should contain theSameElementsAs Seq(
        "ABC Construction Ltd",
        "Capital Construction Group"
      )
    }
    "trim and lowercase the query before searching" in {
      val result = ClientListViewModel.filterByField("CR", "   AbC-001  ", sampleClients)
      result.map(_.clientReference) shouldBe Seq("ABC-001")
    }
  }

  "ClientListViewModel.fromCisClients" - {

    "map cis clients into view models using schemeName / ton/tor / agentOwnRef" in {
      val cisClients = List(
        cisClient(
          schemeName = Some("ABC Construction Ltd"),
          ton = "111",
          tor = "AA12345",
          agentOwnRef = Some("AOR-999")
        ),
        cisClient(
          schemeName = Some("Capital Construction Group"),
          ton = "222",
          tor = "BB67890",
          agentOwnRef = Some("AOR-888")
        )
      )

      val result = ClientListViewModel.fromCisClients(cisClients)

      result shouldBe Seq(
        ClientListViewModel(
          clientName = "ABC Construction Ltd",
          employerReference = "111/AA12345",
          clientReference = "AOR-999",
          clientStatus = Active
        ),
        ClientListViewModel(
          clientName = "Capital Construction Group",
          employerReference = "222/BB67890",
          clientReference = "AOR-888",
          clientStatus = Active
        )
      )
    }

    "use empty strings when schemeName or agentOwnRef are missing" in {
      val cisClients = List(
        cisClient(
          schemeName = None,
          agentOwnRef = None
        )
      )

      val result = ClientListViewModel.fromCisClients(cisClients)

      result.head.clientName      shouldBe ""
      result.head.clientReference shouldBe ""
    }
  }

  "SearchBy.values" - {
    "contain CN, CR and ER in order" in {
      SearchBy.values shouldBe Seq(SearchBy.CN, SearchBy.CR, SearchBy.ER)
    }
  }
  "SearchBy enumerable" - {
    "convert strings to the correct SearchBy values" in {
      SearchBy.enumerable.withName("CN") shouldBe Some(SearchBy.CN)
      SearchBy.enumerable.withName("CR") shouldBe Some(SearchBy.CR)
      SearchBy.enumerable.withName("ER") shouldBe Some(SearchBy.ER)
    }
  }
  "SearchByList.searchByOptions" - {
    "contain the correct value-label pairs" in {
      SearchByList.searchByOptions should contain theSameElementsAs Seq(
        SearchByList("CN", "Client name"),
        SearchByList("CR", "Client reference"),
        SearchByList("ER", "Employer reference")
      )
    }
    "preserve ordering CN → ER → CR" in {
      SearchByList.searchByOptions.map(_.value) shouldBe Seq("CN", "ER", "CR")
    }
  }

}
