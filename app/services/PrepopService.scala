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

package services

import connectors.ConstructionIndustrySchemeConnector
import models.Scheme
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PrepopService @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector
)(implicit ec: ExecutionContext) {

  def prepopulateContractorKnownFacts(
    instanceId: String,
    taxOfficeNumber: String,
    taxOfficeReference: String
  )(implicit hc: HeaderCarrier): Future[Unit] =
    cisConnector.prepopulateContractorKnownFacts(
      instanceId = instanceId,
      taxOfficeNumber = taxOfficeNumber,
      taxOfficeReference = taxOfficeReference
    )

  def prepopulate(
    taxOfficeNumber: String,
    taxOfficeReference: String,
    instanceId: String
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    cisConnector
      .prepopulateContractorAndSubcontractors(
        taxOfficeNumber = taxOfficeNumber,
        taxOfficeReference = taxOfficeReference,
        instanceId = instanceId
      )
      .map(_ => true)
      .recover { case _ =>
        false
      }

  def getScheme(instanceId: String)(implicit hc: HeaderCarrier): Future[Option[Scheme]] =
    cisConnector.getScheme(instanceId)

  private def isNonEmpty(opt: Option[String]): Boolean =
    opt.exists(_.trim.nonEmpty)

  def determineLandingDestination(
    targetCall: Call,
    instanceId: String,
    scheme: Scheme,
    addContractorDetailsCall: Call,
    checkSubcontractorRecordsCall: Call
  ): Call = {
    val prePopOk = scheme.prePopSuccessful.contains("Y")

    val hasName = isNonEmpty(scheme.name)
    val hasUtr  = isNonEmpty(scheme.utr)

    val subCount = scheme.subcontractorCounter.getOrElse(0)

    if (prePopOk) {
      targetCall
    } else if (hasName && hasUtr) {
      targetCall
    } else if (hasName ^ hasUtr) {
      addContractorDetailsCall
    } else {
      if (subCount > 0) {
        addContractorDetailsCall
      } else {
        checkSubcontractorRecordsCall
      }
    }
  }
}
