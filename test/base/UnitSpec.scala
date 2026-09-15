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

package base

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.DefaultAwaitTimeout

import scala.concurrent.ExecutionContext

class UnitSpec
    extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with MockitoSugar
    with ScalaFutures
    with DefaultAwaitTimeout {
  import models.UserAnswers
  import org.mockito.ArgumentMatchers.any
  import org.mockito.Mockito.when
  import play.api.libs.json.{JsObject, Json}
  import play.twirl.api.Html
  import uk.gov.hmrc.http.HeaderCarrier
  import views.html.PageNotFoundView

  protected given ExecutionContext = ExecutionContext.global
  protected given HeaderCarrier    = HeaderCarrier()

  protected val mockControllerComponents = new MockCisControllerComponents()

  protected val journeyRecoveryUrl: String = controllers.routes.JourneyRecoveryController.onPageLoad().url
  protected val unauthorisedUrl: String    = controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad().url

  protected val userAnswersId: String         = "id"
  protected val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  protected val cisId                             = "90063"
  protected val cisIdData: JsObject               = Json.obj("cisId" -> cisId)
  protected val userAnswersWithCisId: UserAnswers = UserAnswers(userAnswersId, cisIdData)

  protected val stubNotFoundView: PageNotFoundView = mock[PageNotFoundView]
  protected val stubNotFoundContent                = "NOT FOUND"
  when(stubNotFoundView.apply()(any, any)) thenReturn Html(stubNotFoundContent)
}
