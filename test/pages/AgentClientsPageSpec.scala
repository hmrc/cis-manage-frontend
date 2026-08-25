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

package pages

import base.SpecBase
import models.CisTaxpayerSearchResult

class AgentClientsPageSpec extends SpecBase {

  private val client1 =
    CisTaxpayerSearchResult(
      uniqueId = "client-1",
      taxOfficeNumber = "111",
      taxOfficeRef = "REF1",
      agentOwnRef = None,
      schemeName = Some("Client One"),
      utr = None
    )

  private val client2 =
    CisTaxpayerSearchResult(
      uniqueId = "client-2",
      taxOfficeNumber = "222",
      taxOfficeRef = "REF2",
      agentOwnRef = None,
      schemeName = Some("Client Two"),
      utr = None
    )

  "AgentClientsPage" - {

    "must find a client by instanceId" in {
      val userAnswers =
        emptyUserAnswers
          .set(AgentClientsPage, List(client1, client2))
          .success
          .value

      AgentClientsPage.findClient(userAnswers, "client-2") mustBe
        Some(client2)
    }

    "must return None when client is not found" in {
      val userAnswers =
        emptyUserAnswers
          .set(AgentClientsPage, List(client1))
          .success
          .value

      AgentClientsPage.findClient(userAnswers, "missing") mustBe None
    }
  }
}
