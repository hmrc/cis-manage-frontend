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

package services

import models.verify.VerificationRequestDetailData
import viewmodels.*

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

@Singleton
class VerificationRequestService @Inject() () {

  private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def buildViewModel(
    data: VerificationRequestDetailData,
    instanceId: String
  ): VerificationRequestPageViewModel =
    VerificationRequestPageViewModel(
      submittedTime = data.dateTimeSubmitted.format(timeFormatter),
      submittedDate = data.dateTimeSubmitted.format(dateFormatter),
      verificationNumber = data.verificationNumber,
      totalSubcontractors = data.subcontractorsToVerify.size + data.subcontractorsToReverify.size,
      subcontractorsToVerify =
        data.subcontractorsToVerify.map(s => SubcontractorRowViewModel(s.name, s.verificationNumber)),
      subcontractorsToReverify =
        data.subcontractorsToReverify.map(s => SubcontractorRowViewModel(s.name, s.verificationNumber)),
      manageSubcontractorsUrl = controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId).url
    )
}
