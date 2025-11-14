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
import org.scalatest.matchers.should.Matchers.*
import play.api.i18n.Messages
import viewmodels.agent.ClientStatus.{Active, InActive}
import viewmodels.agent.{ClientListViewModel, SearchBy, SearchByList}

class ClientListViewModelSpec extends SpecBase {

  implicit val messages: Messages = play.api.i18n.MessagesImpl(
    play.api.i18n.Lang.defaultLang,
    app.injector.instanceOf[play.api.i18n.MessagesApi]
  )

  "ClientListViewModel.removeLink" - {
    "return a remove link when status is Active" in {
      val model = ClientListViewModel(
        clientName = "Test",
        employerReference = "123/AA12345",
        clientReference = "TEST-001",
        clientStatus = Active
      )
      val result = model.removeLink
      result.isDefined shouldBe true
      result.get.text shouldBe "Remove"
      result.get.href shouldBe "#"
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
      val model = ClientListViewModel(
        clientName = "Test",
        employerReference = "123/AA12345",
        clientReference = "TEST-003",
        clientStatus = Active
      )
      val result = model.clientLink
      result.isDefined shouldBe true
      result.get.text shouldBe ""
      result.get.href shouldBe ""
    }
  }

  "ClientListViewModel.filterByField" - {
    "return all clients when query is empty" in {
      val result = ClientListViewModel.filterByField("CR", "")
      result shouldBe ClientListViewModel.allAgentClients
    }
    "filter by client reference when field = CR" in {
      val result = ClientListViewModel.filterByField("CR", "abc-002")
      result.map(_.clientReference) shouldBe Seq("ABC-002")
    }
    "filter by employer reference when field = ER" in {
      val result = ClientListViewModel.filterByField("ER", "345/ij")
      result.map(_.employerReference) shouldBe Seq("345/IJ67890")
    }
    "filter by client name when field is anything else" in {
      val result = ClientListViewModel.filterByField("NAME", "construction")
      result.map(_.clientName) should contain theSameElementsAs Seq(
        "ABC Construction Ltd",
        "Capital Construction Group"
      )
    }
    "trim and lowercase the query before searching" in {
      val result = ClientListViewModel.filterByField("CR", "   AbC-001  ")
      result.map(_.clientReference) shouldBe Seq("ABC-001")
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
    "preserve ordering CN → CR → ER" in {
      SearchByList.searchByOptions.map(_.value) shouldBe Seq("CN", "CR", "ER")
    }
  }

}
