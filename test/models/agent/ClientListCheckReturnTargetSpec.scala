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

package models.agent

import base.SpecBase

class ClientListCheckReturnTargetSpec extends SpecBase {

  "ClientListCheckReturnTarget" - {

    "must expose the correct keys" in {
      ClientListCheckReturnTarget.AgentDashboard.key mustBe "agent-dashboard"
      ClientListCheckReturnTarget.FileMonthlyReturns.key mustBe "file-monthly-returns"
      ClientListCheckReturnTarget.ManageClientDetails.key mustBe "manage-client-details"
      ClientListCheckReturnTarget.ChangeClientReference.key mustBe "change-client-reference"
      ClientListCheckReturnTarget.RemoveClient.key mustBe "remove-client"
    }
  }
}
