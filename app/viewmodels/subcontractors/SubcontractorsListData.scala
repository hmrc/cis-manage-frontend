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

package viewmodels.subcontractors

object SubcontractorsListData {

  private val contractorFrontendBaseUrl =
    "http://localhost:6998/construction-industry-scheme/subcontractor"

  private val instanceId =
    "test-instance-id"

  private def detailsUrl(
    subbieResourceRef: Long
  ): String =
    s"$contractorFrontendBaseUrl/amend/company/$instanceId/$subbieResourceRef"

  private def row(
    id: String,
    name: String,
    utr: String,
    verified: Boolean,
    verificationNumber: String,
    taxTreatment: TaxTreatment,
    dateAdded: String,
    subbieResourceRef: Long
  ): SubcontractorsListRow =
    SubcontractorsListRow(
      id = id,
      name = name,
      utr = utr,
      verified = verified,
      verificationNumber = verificationNumber,
      taxTreatment = taxTreatment,
      dateAdded = dateAdded,
      subbieResourceRef = subbieResourceRef,
      detailsUrl = detailsUrl(subbieResourceRef)
    )

  val rows: Seq[SubcontractorsListRow] =
    Seq(
      row(
        id = "Grantalan",
        name = "Grant, Alan",
        utr = "0991272528",
        verified = false,
        verificationNumber = "V0001256246",
        taxTreatment = TaxTreatment.StandardRate,
        dateAdded = "11 May 2020",
        subbieResourceRef = 1L
      ),
      row(
        id = "Hammondhouse",
        name = "Hammond House",
        utr = "2904743750",
        verified = true,
        verificationNumber = "V0001217702",
        taxTreatment = TaxTreatment.Gross,
        dateAdded = "1 Oct 2025",
        subbieResourceRef = 2L
      ),
      row(
        id = "Ingenresearch",
        name = "InGen Research",
        utr = "9347488729",
        verified = false,
        verificationNumber = "V0005617876",
        taxTreatment = TaxTreatment.StandardRate,
        dateAdded = "1 Mar 2020",
        subbieResourceRef = 3L
      ),
      row(
        id = "Malcolmandsattler",
        name = "Malcolm And Sattler",
        utr = "0074742762",
        verified = true,
        verificationNumber = "V0004635231",
        taxTreatment = TaxTreatment.HigherRate,
        dateAdded = "1 Oct 2025",
        subbieResourceRef = 4L
      ),
      row(
        id = "brightwellPartners",
        name = "Brightwell Partners",
        utr = "1234567890",
        verified = false,
        verificationNumber = "V0007771001",
        taxTreatment = TaxTreatment.StandardRate,
        dateAdded = "23 Apr 2026",
        subbieResourceRef = 5L
      ),
      row(
        id = "brightwellPartners",
        name = "Brightwell Partners",
        utr = "1234567890",
        verified = false,
        verificationNumber = "V0007771001",
        taxTreatment = TaxTreatment.StandardRate,
        dateAdded = "23 Apr 2026",
        subbieResourceRef = 29L
      ),
      row(
        id = "carterfieldsLtd",
        name = "Carterfields Ltd",
        utr = "2345678901",
        verified = true,
        verificationNumber = "V0007771002",
        taxTreatment = TaxTreatment.Gross,
        dateAdded = "23 Apr 2026",
        subbieResourceRef = 6L
      ),
      row(
        id = "northbridgeBuild",
        name = "Northbridge Build",
        utr = "3456789012",
        verified = false,
        verificationNumber = "V0007771003",
        taxTreatment = TaxTreatment.StandardRate,
        dateAdded = "23 Apr 2026",
        subbieResourceRef = 7L
      ),
      row(
        id = "oakthornServices",
        name = "Oakthorn Services",
        utr = "4567890123",
        verified = true,
        verificationNumber = "V0007771004",
        taxTreatment = TaxTreatment.HigherRate,
        dateAdded = "23 Apr 2026",
        subbieResourceRef = 8L
      ),
      row(
        id = "alphaBuild",
        name = "Alpha Build Ltd",
        utr = "1111111111",
        verified = false,
        verificationNumber = "V0009000001",
        taxTreatment = TaxTreatment.StandardRate,
        dateAdded = "01 Jan 2026",
        subbieResourceRef = 9L
      ),
      row(
        id = "betaConstruct",
        name = "Beta Construction",
        utr = "1111111112",
        verified = true,
        verificationNumber = "V0009000002",
        taxTreatment = TaxTreatment.Gross,
        dateAdded = "02 Jan 2026",
        subbieResourceRef = 10L
      ),
      row(
        id = "noNameProvided1",
        name = "No name provided",
        utr = "1111111127",
        verified = false,
        verificationNumber = "V0009000017",
        taxTreatment = TaxTreatment.StandardRate,
        dateAdded = "17 Jan 2026",
        subbieResourceRef = 28L
      ),
      row(
        id = "noNameProvided2",
        name = "No name provided",
        utr = "1111111128",
        verified = true,
        verificationNumber = "V0009000018",
        taxTreatment = TaxTreatment.Gross,
        dateAdded = "18 Jan 2026",
        subbieResourceRef = 29L
      ),
      row(
        id = "charlieWorks",
        name = "Charlie Works Ltd",
        utr = "1111111113",
        verified = false,
        verificationNumber = "V0009000003",
        taxTreatment = TaxTreatment.HigherRate,
        dateAdded = "03 Jan 2026",
        subbieResourceRef = 11L
      ),
      row(
        id = "deltaGroup",
        name = "Delta Group",
        utr = "1111111114",
        verified = true,
        verificationNumber = "V0009000004",
        taxTreatment = TaxTreatment.StandardRate,
        dateAdded = "04 Jan 2026",
        subbieResourceRef = 12L
      ),
      row(
        id = "echoServices",
        name = "Echo Services",
        utr = "1111111115",
        verified = false,
        verificationNumber = "V0009000005",
        taxTreatment = TaxTreatment.Gross,
        dateAdded = "05 Jan 2026",
        subbieResourceRef = 13L
      ),
      row(
        id = "foxtrotLtd",
        name = "Foxtrot Ltd",
        utr = "1111111116",
        verified = true,
        verificationNumber = "V0009000006",
        taxTreatment = TaxTreatment.StandardRate,
        dateAdded = "06 Jan 2026",
        subbieResourceRef = 14L
      ),
      row(
        id = "gammaBuilders",
        name = "Gamma Builders",
        utr = "1111111117",
        verified = false,
        verificationNumber = "V0009000007",
        taxTreatment = TaxTreatment.HigherRate,
        dateAdded = "07 Jan 2026",
        subbieResourceRef = 15L
      ),
      row(
        id = "harbourWorks",
        name = "Harbour Works",
        utr = "1111111118",
        verified = true,
        verificationNumber = "V0009000008",
        taxTreatment = TaxTreatment.Gross,
        dateAdded = "08 Jan 2026",
        subbieResourceRef = 16L
      ),
      row(
        id = "ivoryConstruction",
        name = "Ivory Construction",
        utr = "1111111119",
        verified = false,
        verificationNumber = "V0009000009",
        taxTreatment = TaxTreatment.StandardRate,
        dateAdded = "09 Jan 2026",
        subbieResourceRef = 17L
      ),
      row(
        id = "jupiterProjects",
        name = "Jupiter Projects",
        utr = "1111111120",
        verified = true,
        verificationNumber = "V0009000010",
        taxTreatment = TaxTreatment.HigherRate,
        dateAdded = "10 Jan 2026",
        subbieResourceRef = 18L
      ),
      row(
        id = "kingstonBuild",
        name = "Kingston Build",
        utr = "1111111121",
        verified = false,
        verificationNumber = "V0009000011",
        taxTreatment = TaxTreatment.Gross,
        dateAdded = "11 Jan 2026",
        subbieResourceRef = 19L
      ),
      row(
        id = "lunarDevelopments",
        name = "Lunar Developments",
        utr = "1111111122",
        verified = true,
        verificationNumber = "V0009000012",
        taxTreatment = TaxTreatment.StandardRate,
        dateAdded = "12 Jan 2026",
        subbieResourceRef = 20L
      ),
      row(
        id = "metroConstruct",
        name = "Metro Construct Ltd",
        utr = "1111111123",
        verified = false,
        verificationNumber = "V0009000013",
        taxTreatment = TaxTreatment.HigherRate,
        dateAdded = "13 Jan 2026",
        subbieResourceRef = 21L
      ),
      row(
        id = "novaBuilders",
        name = "Nova Builders",
        utr = "1111111124",
        verified = true,
        verificationNumber = "V0009000014",
        taxTreatment = TaxTreatment.Gross,
        dateAdded = "14 Jan 2026",
        subbieResourceRef = 22L
      ),
      row(
        id = "gammaBuilders",
        name = "Gamma Builders",
        utr = "1111111117",
        verified = false,
        verificationNumber = "V0009000007",
        taxTreatment = TaxTreatment.Unknown,
        dateAdded = "07 Jan 2026",
        subbieResourceRef = 23L
      ),
      row(
        id = "orbitWorks",
        name = "Orbit Works",
        utr = "1111111125",
        verified = false,
        verificationNumber = "V0009000015",
        taxTreatment = TaxTreatment.Unknown,
        dateAdded = "15 Jan 2026",
        subbieResourceRef = 24L
      ),
      row(
        id = "northbridgeBuild",
        name = "Northbridge Build",
        utr = "3456789012",
        verified = false,
        verificationNumber = "V0007771003",
        taxTreatment = TaxTreatment.Unknown,
        dateAdded = "23 Apr 2026",
        subbieResourceRef = 25L
      ),
      row(
        id = "orbitWorks",
        name = "Orbit Works",
        utr = "1111111125",
        verified = false,
        verificationNumber = "V0009000015",
        taxTreatment = TaxTreatment.StandardRate,
        dateAdded = "15 Jan 2026",
        subbieResourceRef = 26L
      ),
      row(
        id = "pioneerLtd",
        name = "Pioneer Ltd",
        utr = "1111111126",
        verified = true,
        verificationNumber = "V0009000016",
        taxTreatment = TaxTreatment.HigherRate,
        dateAdded = "16 Jan 2026",
        subbieResourceRef = 27L
      )
    ).sortBy(_.name.toLowerCase)
}
