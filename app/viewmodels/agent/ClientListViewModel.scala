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

import controllers.routes
import ClientStatus.Active
import play.api.i18n.Messages
import viewmodels.Link

case class ClientListViewModel (clientName: String,
                            employerReference: String,
                            clientReference: String,
                            clientStatus: ClientStatus
                           ) {

  def removeLink(implicit messages: Messages): Option[Link] = {
    clientStatus match {
      case Active => Some(Link(messages("agent.clientListSearch.td.action.remove"), routes.JourneyRecoveryController.onPageLoad().url))
      case _ => None
    }
  }
  def clientLink(implicit messages: Messages): Option[Link] = {
    Some(Link("", routes.JourneyRecoveryController.onPageLoad().url))
  }
}

object ClientListViewModel {
  
  def filterByField(field: String, query: String): Seq[ClientListViewModel] = {
    val trimmed = query.trim.toLowerCase
    if (trimmed.isEmpty) {allAgentClients}
    else {field match {
      case "CR" => allAgentClients.filter(u => u.clientReference.toLowerCase.contains(trimmed))
      case "ER" => allAgentClients.filter(u => u.employerReference.toLowerCase.contains(trimmed))
      case _ => allAgentClients.filter(u => u.clientName.toLowerCase.contains(trimmed))
    }}
  }

  val allAgentClients: Seq[ClientListViewModel] = Seq(
    ClientListViewModel("ABC Construction Ltd", "123/AB45678", "ABC-001", Active),
    ClientListViewModel("ABC Property Services", "789/EF23456", "ABC-002", Active),
    ClientListViewModel("Capital Construction Group", "345/IJ67890", "CAP-001", Active)
  )
  
}

case class SearchByList(value: String, label: String)

object SearchByList {

  val searchByOptions: Seq[SearchByList] = Seq(
    SearchByList("CN", "Client name"),
    SearchByList("CR", "Client reference"),
    SearchByList("ER", "Employer reference")
  )

}
