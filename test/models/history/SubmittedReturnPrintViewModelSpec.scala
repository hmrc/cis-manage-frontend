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

package models.history

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.stubMessagesApi

class SubmittedReturnPrintViewModelSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues {

  private val messageApi: MessagesApi = stubMessagesApi()
  implicit val messages: Messages     = messageApi.preferred(Seq.empty)

  "SubmittedReturnPrintViewModel" - {

    "must return all fields" in {
      val monthYear              = "April 2026"
      val submittedTime          = "8:46am"
      val submittedDate          = "16 March 2025"
      val receiptReferenceNumber = "6QEDAHDREBY455GDNCPMDCNDFBDBJSJSJDNDDHDJDZ5"
      val submissionType         = "Monthly return"
      val contractorName         = "PAL 355 Scheme"
      val payeReference          = "123/AB456"
      val totalPaymentsMade      = "£1900"
      val totalCostOfMaterials   = "£616"
      val totalTaxDeducted       = "£380"

      val subcontractors = Seq(
        SubcontractorPayment(
          name = "BuildRight Construction",
          verificationNumber = "V1234567890",
          paymentsMade = "£165",
          costOfMaterials = "£95",
          taxDeducted = "£95"
        ),
        SubcontractorPayment(
          name = "Northern Trades Ltd",
          verificationNumber = "V0987654321",
          paymentsMade = "£75",
          costOfMaterials = "£55",
          taxDeducted = "£55"
        ),
        SubcontractorPayment(
          name = "TyneWear Ltd",
          verificationNumber = "",
          paymentsMade = "£165",
          costOfMaterials = "£125",
          taxDeducted = "£55"
        )
      )

      val model = SubmittedReturnPrintViewModel(
        monthYear = monthYear,
        submittedTime = submittedTime,
        submittedDate = submittedDate,
        receiptReferenceNumber = receiptReferenceNumber,
        submissionType = submissionType,
        contractorName = contractorName,
        payeReference = payeReference,
        totalPaymentsMade = totalPaymentsMade,
        totalCostOfMaterials = totalCostOfMaterials,
        totalTaxDeducted = totalTaxDeducted,
        subcontractors = subcontractors
      )

      model.monthYear mustBe monthYear
      model.submittedTime mustBe submittedTime
      model.submittedDate mustBe submittedDate
      model.receiptReferenceNumber mustBe receiptReferenceNumber
      model.submissionType mustBe submissionType
      model.contractorName mustBe contractorName
      model.payeReference mustBe payeReference
      model.totalPaymentsMade mustBe totalPaymentsMade
      model.totalCostOfMaterials mustBe totalCostOfMaterials
      model.totalTaxDeducted mustBe totalTaxDeducted
      model.subcontractors mustBe subcontractors
    }
  }
}
