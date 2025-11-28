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

import ClientStatus.Active
import models.{CisTaxpayerSearchResult, Enumerable, WithName}
import play.api.i18n.Messages
import viewmodels.Link
import viewmodels.agent.SearchBy.*

case class ClientListViewModel(
  clientName: String,
  employerReference: String,
  clientReference: String,
  clientStatus: ClientStatus
) {

  def removeLink(implicit messages: Messages): Option[Link] =
    clientStatus match {
      case Active =>
        Some(
          Link(messages("agent.clientListSearch.td.actions.remove"), "#")
        )
      case _      => None
    }
  def clientLink(implicit messages: Messages): Option[Link] =
    Some(Link("", ""))
}

object ClientListViewModel {

  def filterByField(field: String, query: String, clients: Seq[ClientListViewModel]): Seq[ClientListViewModel] = {
    val trimmed = query.trim.toLowerCase
    if (trimmed.isEmpty) { clients }
    else {
      field match {
        case "CR" => clients.filter(u => u.clientReference.toLowerCase.contains(trimmed))
        case "ER" => clients.filter(u => u.employerReference.toLowerCase.contains(trimmed))
        case _    => clients.filter(u => u.clientName.toLowerCase.contains(trimmed))
      }
    }
  }

  def fromCisClients(clients: List[CisTaxpayerSearchResult]): Seq[ClientListViewModel] =
    clients.map { client =>
      ClientListViewModel(
        clientName = client.schemeName.getOrElse(""),
        employerReference = s"${client.taxOfficeNumber}/${client.taxOfficeRef}",
        clientReference = client.agentOwnRef.getOrElse(""),
        clientStatus = Active
      )
    }

}

sealed trait SearchBy

object SearchBy extends Enumerable.Implicits {
  case object CN extends WithName("CN") with SearchBy
  case object CR extends WithName("CR") with SearchBy
  case object ER extends WithName("ER") with SearchBy

  val values: Seq[SearchBy] = Seq(
    CN,
    CR,
    ER
  )

  implicit val enumerable: Enumerable[SearchBy] =
    Enumerable(values.map(v => v.toString -> v): _*)

}

case class SearchByList(value: String, label: String)

object SearchByList {

  val searchByOptions: Seq[SearchByList] = Seq(
    SearchByList(CN.toString, "Client name"),
    SearchByList(ER.toString, "Employer reference"),
    SearchByList(CR.toString, "Client reference")
  )

}
