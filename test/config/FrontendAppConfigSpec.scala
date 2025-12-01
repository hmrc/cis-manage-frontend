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

package config

import base.SpecBase
import play.api.Application
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest

class FrontendAppConfigSpec extends SpecBase {

  "FrontendAppConfig" - {
    "must contain correct values for the provided configuration" in new Setup {
      appConfig.appName mustBe "cis-manage-frontend"

      appConfig.loginUrl mustBe "http://localhost:9949/auth-login-stub/gg-sign-in"
      appConfig.loginContinueUrl mustBe "http://localhost:6996/construction-industry-scheme/management"
      appConfig.signOutUrl mustBe "http://localhost:9553/bas-gateway/sign-out-without-state"

      appConfig.govUkCISGuidanceUrl mustBe "https://www.gov.uk/what-is-the-construction-industry-scheme"
      appConfig.commercialSoftwareSuppliersUrl mustBe
        "https://www.gov.uk/government/publications/construction-industry-scheme-cis-commercial-software-suppliers"
      appConfig.technicalSupportWithHmrcOnlineServicesUrl mustBe
        "https://www.gov.uk/find-hmrc-contacts/technical-support-with-hmrc-online-services"
      appConfig.registerAsAProfessionalTaxAgentWithHmrcUrl mustBe
        "https://www.gov.uk/guidance/find-out-how-to-register-as-a-professional-tax-agent-with-hmrc"
      appConfig.taxAgentsAndAdvisorsAuthorisationFormsUrl mustBe
        "https://www.gov.uk/government/collections/tax-agents-and-advisors-authorisation-forms"

      appConfig.cisHelpWhatIsUrl mustBe "https://www.gov.uk/what-is-the-construction-industry-scheme"
      appConfig.cisHelpMonthlyUrl mustBe "https://www.gov.uk/guidance/cis-monthly-returns"
      appConfig.cisHelp340Url mustBe
        "https://www.gov.uk/what-you-must-do-as-a-cis-contractor/file-your-monthly-returns#penalties-for-late-returns"

      appConfig.returnToHomeUrl mustBe "#"

      appConfig.contractorLandingWhatIsUrl mustBe "https://www.gov.uk/what-is-the-construction-industry-scheme"
      appConfig.contractorLandingGuidanceUrl mustBe "https://www.gov.uk/guidance/cis-monthly-returns"
      appConfig.contractorLandingPenaltiesUrl mustBe "https://www.gov.uk/government/publications/cis-340"

      appConfig.hmrcOnlineServiceDeskUrl mustBe
        "https://www.gov.uk/find-hmrc-contacts/technical-support-with-hmrc-online-services"
      appConfig.payeCisForAgentsOnlineService mustBe "https://www.gov.uk/guidance/payecis-for-agents-online-service"

      appConfig.timeout mustBe 900
      appConfig.countdown mustBe 120
      appConfig.cacheTtl mustBe 900L
    }
  }

  "feedbackUrl" - {
    "must include contact-frontend host, service id and request back url" in new Setup {
      implicit val request: RequestHeader = FakeRequest("GET", "/some-path")

      appConfig.feedbackUrl mustBe
        "http://localhost:9250/contact/beta-feedback?service=cis-manage-frontend&backUrl=http://localhost:6996/some-path"
    }
  }

  "exitSurveyUrl" - {
    "must be built from feedback-frontend service base URL" in new Setup {
      appConfig.exitSurveyUrl mustBe "http://localhost:9514/feedback/cis-manage-frontend"
    }
  }

  "languageMap" - {
    "must contain English and Welsh language codes" in new Setup {
      val map = appConfig.languageMap

      map.keys must contain allOf ("en", "cy")
      map("en") mustBe Lang("en")
      map("cy") mustBe Lang("cy")
    }
  }

  "languageTranslationEnabled" - {
    "must reflect the configured feature flag" in new Setup {
      appConfig.languageTranslationEnabled mustBe false
    }
  }

  trait Setup {
    val app: Application             = applicationBuilder().build()
    val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  }
}
