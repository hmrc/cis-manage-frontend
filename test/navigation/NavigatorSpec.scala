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

package navigation

import base.SpecBase
import controllers.routes
import pages.*
import models.*
import pages.clientdetails.{ChangeClientReferencePage, RemoveClientYesNoPage}

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "RemoveClientYesNoPage" - {

        "must go to ClientRemovedController when answer is true" in {

          val ua = emptyUserAnswers
            .set(RemoveClientYesNoPage, true)
            .success
            .value

          navigator.nextPage(RemoveClientYesNoPage, NormalMode, ua) mustBe
            controllers.clientdetails.routes.ClientRemovedController.onPageLoad()
        }

        "must go to AgentLandingController when answer is false" in {

          val ua = userAnswersWithCisId
            .set(RemoveClientYesNoPage, false)
            .success
            .value

          navigator.nextPage(RemoveClientYesNoPage, NormalMode, ua) mustBe
            controllers.agent.routes.AgentLandingController.onPageLoad("1")
        }

        "must go to ClientListSearchController when answer is false" in {

          val ua = emptyUserAnswers
            .set(RemoveClientYesNoPage, false)
            .success
            .value

          navigator.nextPage(RemoveClientYesNoPage, NormalMode, ua) mustBe
            controllers.agent.routes.ClientListSearchController.onPageLoad()
        }

        "must go to JourneyRecoveryController when user answer is missing" in {

          navigator.nextPage(RemoveClientYesNoPage, NormalMode, emptyUserAnswers) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "ChangeClientReferencePage" - {
        "must go to ClientRefUpdateConfirmationController" in {

          val ua = emptyUserAnswers
            .set(ChangeClientReferencePage, "client ref")
            .success
            .value

          navigator.nextPage(ChangeClientReferencePage, NormalMode, ua) mustBe
            controllers.clientdetails.routes.ClientRefUpdateConfirmationController.onPageLoad()
        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController
          .onPageLoad()
      }
    }
  }
}
