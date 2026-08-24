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

package navigation

import models.Mode
import models.agent.ClientListCheckReturnTarget
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ClientListCheckNavigator @Inject() {

  def agentDashboard(instanceId: String): Call =
    controllers.routes.SecurityCheckController.onClientListCheck(
      returnTo = ClientListCheckReturnTarget.AgentDashboard.key,
      instanceId = Some(instanceId),
      mode = None
    )

  def fileMonthlyReturns: Call =
    controllers.routes.SecurityCheckController.onClientListCheck(
      returnTo = ClientListCheckReturnTarget.FileMonthlyReturns.key,
      instanceId = None,
      mode = None
    )

  def manageClientDetails: Call =
    controllers.routes.SecurityCheckController.onClientListCheck(
      returnTo = ClientListCheckReturnTarget.ManageClientDetails.key,
      instanceId = None,
      mode = None
    )

  def changeClientReference(mode: Mode): Call =
    controllers.routes.SecurityCheckController.onClientListCheck(
      returnTo = ClientListCheckReturnTarget.ChangeClientReference.key,
      instanceId = None,
      mode = Some(Mode.asString(mode))
    )

  def removeClient(mode: Mode): Call =
    controllers.routes.SecurityCheckController.onClientListCheck(
      returnTo = ClientListCheckReturnTarget.RemoveClient.key,
      instanceId = None,
      mode = Some(Mode.asString(mode))
    )
}
