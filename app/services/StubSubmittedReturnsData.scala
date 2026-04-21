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

import models.UserAnswers
import models.{SubmittedMonthlyReturnData, SubmittedReturnsData, SubmittedSchemeData, SubmittedSubmissionData}
import pages.SubmittedReturnsDataPage

import java.time.Instant

object StubSubmittedReturnsData {

  private val stubData = SubmittedReturnsData(
    scheme = SubmittedSchemeData(
      name = "ABC Construction Ltd",
      taxOfficeNumber = "123",
      taxOfficeReference = "AB456"
    ),
    monthlyReturn = Seq(
      SubmittedMonthlyReturnData(
        monthlyReturnId = 1001L,
        taxYear = 2023,
        taxMonth = 4,
        nilReturnIndicator = "N",
        status = "SUBMITTED",
        supersededBy = None,
        amendmentStatus = None,
        monthlyReturnItems = Some("Y")
      ),
      SubmittedMonthlyReturnData(
        monthlyReturnId = 1002L,
        taxYear = 2023,
        taxMonth = 7,
        nilReturnIndicator = "Y",
        status = "SUBMITTED_NO_RECEIPT",
        supersededBy = None,
        amendmentStatus = None,
        monthlyReturnItems = Some("N")
      ),
      SubmittedMonthlyReturnData(
        monthlyReturnId = 1003L,
        taxYear = 2019,
        taxMonth = 12,
        nilReturnIndicator = "N",
        status = "SUBMITTED",
        supersededBy = Some(2001L),
        amendmentStatus = Some("STARTED"),
        monthlyReturnItems = Some("Y")
      ),
      SubmittedMonthlyReturnData(
        monthlyReturnId = 1004L,
        taxYear = 2023,
        taxMonth = 2,
        nilReturnIndicator = "Y",
        status = "SUBMITTED",
        supersededBy = None,
        amendmentStatus = None,
        monthlyReturnItems = Some("N")
      )
    ),
    submissions = Seq(
      SubmittedSubmissionData(
        submissionId = 5001L,
        submissionType = Some("MONTHLY_RETURN"),
        activeObjectId = 1001L,
        status = "SUBMITTED",
        hmrcMarkGenerated = Some("AAA"),
        hmrcMarkGgis = Some("AAA"),
        emailRecipient = Some("test1@test.com"),
        acceptedTime = Some(Instant.parse("2023-02-01T10:00:00Z"))
      ),
      SubmittedSubmissionData(
        submissionId = 5002L,
        submissionType = Some("MONTHLY_RETURN"),
        activeObjectId = 1002L,
        status = "SUBMITTED_NO_RECEIPT",
        hmrcMarkGenerated = None,
        hmrcMarkGgis = None,
        emailRecipient = None,
        acceptedTime = Some(Instant.parse("2023-06-01T10:00:00Z"))
      ),
      SubmittedSubmissionData(
        submissionId = 5003L,
        submissionType = Some("MONTHLY_RETURN"),
        activeObjectId = 1003L,
        status = "SUBMITTED",
        hmrcMarkGenerated = Some("BBB"),
        hmrcMarkGgis = Some("BBB"),
        emailRecipient = Some("test2@test.com"),
        acceptedTime = Some(Instant.parse("2019-11-15T10:00:00Z"))
      ),
      SubmittedSubmissionData(
        submissionId = 5004L,
        submissionType = Some("Nil return"),
        activeObjectId = 1004L,
        status = "SUBMITTED",
        hmrcMarkGenerated = Some("CCC"),
        hmrcMarkGgis = Some("CCC"),
        emailRecipient = Some("test3@test.com"),
        acceptedTime = Some(Instant.parse("2023-01-10T10:00:00Z"))
      )
    )
  )

  def addTo(userAnswers: UserAnswers): UserAnswers =
    userAnswers.set(SubmittedReturnsDataPage, stubData).getOrElse(userAnswers)

}
