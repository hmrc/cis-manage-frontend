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

package base

import config.FrontendAppConfig
import controllers.actions.*
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.{Binding, bind}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.PlayBodyParsers
import play.api.test.Helpers.stubControllerComponents
import repositories.SessionRepository

import scala.concurrent.Future

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with GuiceOneAppPerSuite
    with IntegrationPatience {

  override def fakeApplication(): Application = applicationBuilder().build()

  implicit lazy val applicationConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val userAnswersId: String    = "id"
  val parsers: PlayBodyParsers = stubControllerComponents().parsers
  val cisIdData: JsObject      = Json.obj("cisId" -> "1")

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def userAnswersWithCisId: UserAnswers = UserAnswers(userAnswersId, cisIdData)

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def mockSessionRepository(userAnswers: Option[UserAnswers]): SessionRepository = {
    val repository = mock[SessionRepository]
    when(repository.get(any[String])).thenReturn(Future.successful(userAnswers))
    repository
  }

  protected def applicationBuilder(
    userAnswers: Option[UserAnswers] = None,
    additionalBindings: Seq[Binding[_]] = Nil,
    isAgent: Boolean = false,
    agentCode: Option[String] = Some("agentCode"),
    itmpName: Option[String] = None
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure("play.http.router" -> "app.Routes")
      .overrides(
        Seq(
          bind[DataRequiredAction].to[DataRequiredActionImpl],
          bind[IdentifierAction].to(new FakeIdentifierAction(isAgent)(parsers)),
          bind[IdentifierAction]
            .qualifiedWith("AgentIdentifier")
            .to(new FakeIdentifierAction(true, agentCode, itmpName)(parsers)),
          bind[IdentifierAction].qualifiedWith("ContractorIdentifier").to(new FakeIdentifierAction(false)(parsers)),
          bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
          bind[FormpRdsReconcileAction].toInstance(new FakeFormpRdsReconcileAction)
        ) ++ additionalBindings
      )
}
