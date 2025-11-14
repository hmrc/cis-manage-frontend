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
import viewmodels.agent.ClientStatus.*


class ClientStatusSpec extends SpecBase {

  implicit val messages: Messages = play.api.i18n.MessagesImpl(
    play.api.i18n.Lang.defaultLang,
    app.injector.instanceOf[play.api.i18n.MessagesApi]
  )

  "ClientStatus enum" - {
    "contain Active and InActive in values" in {
      ClientStatus.values should contain theSameElementsInOrderAs Seq(
        ClientStatus.Active,
        ClientStatus.InActive
      )
    }
    "expose correct entry names" in {
      ClientStatus.Active.entryName shouldBe "ACTIVE"
      ClientStatus.InActive.entryName shouldBe "INACTIVE"
    }
    "resolve enum values using withName" in {
      ClientStatus.withName("ACTIVE") shouldBe ClientStatus.Active
      ClientStatus.withName("INACTIVE") shouldBe ClientStatus.InActive
    }
    "resolve enum values using withNameInsensitive" in {
      ClientStatus.withNameInsensitive("active") shouldBe ClientStatus.Active
      ClientStatus.withNameInsensitive("inactive") shouldBe ClientStatus.InActive
    }
    "throw when name is invalid" in {
      an[NoSuchElementException] should be thrownBy {
        ClientStatus.withName("UNKNOWN")
      }
    }
  }

}
