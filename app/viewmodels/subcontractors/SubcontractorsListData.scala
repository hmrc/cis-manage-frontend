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

  val rows: Seq[SubcontractorsListRow] = Seq(
    SubcontractorsListRow(
      id = "Grantalan",
      name = "Grant, Alan",
      utr = "0991272528",
      verified = false,
      verificationNumber = "V0001256246",
      taxTreatment = TaxTreatment.StandardRate,
      dateAdded = "11 May 2020"
    ),
    SubcontractorsListRow(
      id = "Hammondhouse",
      name = "Hammond House",
      utr = "2904743750",
      verified = true,
      verificationNumber = "V0001217702",
      taxTreatment = TaxTreatment.Gross,
      dateAdded = "1 Oct 2025"
    ),
    SubcontractorsListRow(
      id = "Ingenresearch",
      name = "InGen Research",
      utr = "9347488729",
      verified = false,
      verificationNumber = "V0005617876",
      taxTreatment = TaxTreatment.StandardRate,
      dateAdded = "1 Mar 2020"
    ),
    SubcontractorsListRow(
      id = "Malcolmandsattler",
      name = "Malcolm And Sattler",
      utr = "0074742762",
      verified = true,
      verificationNumber = "V0004635231",
      taxTreatment = TaxTreatment.HigherRate,
      dateAdded = "1 Oct 2025"
    ),
    SubcontractorsListRow(
      id = "brightwellPartners",
      name = "Brightwell Partners",
      utr = "1234567890",
      verified = false,
      verificationNumber = "V0007771001",
      taxTreatment = TaxTreatment.StandardRate,
      dateAdded = "23 Apr 2026"
    ),
    SubcontractorsListRow(
      id = "carterfieldsLtd",
      name = "Carterfields Ltd",
      utr = "2345678901",
      verified = true,
      verificationNumber = "V0007771002",
      taxTreatment = TaxTreatment.Gross,
      dateAdded = "23 Apr 2026"
    ),
    SubcontractorsListRow(
      id = "northbridgeBuild",
      name = "Northbridge Build",
      utr = "3456789012",
      verified = false,
      verificationNumber = "V0007771003",
      taxTreatment = TaxTreatment.StandardRate,
      dateAdded = "23 Apr 2026"
    ),
    SubcontractorsListRow(
      id = "oakthornServices",
      name = "Oakthorn Services",
      utr = "4567890123",
      verified = true,
      verificationNumber = "V0007771004",
      taxTreatment = TaxTreatment.HigherRate,
      dateAdded = "23 Apr 2026"
    ),
    SubcontractorsListRow(
      "alphaBuild",
      "Alpha Build Ltd",
      "1111111111",
      false,
      "V0009000001",
      TaxTreatment.StandardRate,
      "01 Jan 2026"
    ),
    SubcontractorsListRow(
      "betaConstruct",
      "Beta Construction",
      "1111111112",
      true,
      "V0009000002",
      TaxTreatment.Gross,
      "02 Jan 2026"
    ),
    SubcontractorsListRow(
      "charlieWorks",
      "Charlie Works Ltd",
      "1111111113",
      false,
      "V0009000003",
      TaxTreatment.HigherRate,
      "03 Jan 2026"
    ),
    SubcontractorsListRow(
      "deltaGroup",
      "Delta Group",
      "1111111114",
      true,
      "V0009000004",
      TaxTreatment.StandardRate,
      "04 Jan 2026"
    ),
    SubcontractorsListRow(
      "echoServices",
      "Echo Services",
      "1111111115",
      false,
      "V0009000005",
      TaxTreatment.Gross,
      "05 Jan 2026"
    ),
    SubcontractorsListRow(
      "foxtrotLtd",
      "Foxtrot Ltd",
      "1111111116",
      true,
      "V0009000006",
      TaxTreatment.StandardRate,
      "06 Jan 2026"
    ),
    SubcontractorsListRow(
      "gammaBuilders",
      "Gamma Builders",
      "1111111117",
      false,
      "V0009000007",
      TaxTreatment.HigherRate,
      "07 Jan 2026"
    ),
    SubcontractorsListRow(
      "harbourWorks",
      "Harbour Works",
      "1111111118",
      true,
      "V0009000008",
      TaxTreatment.Gross,
      "08 Jan 2026"
    ),
    SubcontractorsListRow(
      "ivoryConstruction",
      "Ivory Construction",
      "1111111119",
      false,
      "V0009000009",
      TaxTreatment.StandardRate,
      "09 Jan 2026"
    ),
    SubcontractorsListRow(
      "jupiterProjects",
      "Jupiter Projects",
      "1111111120",
      true,
      "V0009000010",
      TaxTreatment.HigherRate,
      "10 Jan 2026"
    ),
    SubcontractorsListRow(
      "kingstonBuild",
      "Kingston Build",
      "1111111121",
      false,
      "V0009000011",
      TaxTreatment.Gross,
      "11 Jan 2026"
    ),
    SubcontractorsListRow(
      "lunarDevelopments",
      "Lunar Developments",
      "1111111122",
      true,
      "V0009000012",
      TaxTreatment.StandardRate,
      "12 Jan 2026"
    ),
    SubcontractorsListRow(
      "metroConstruct",
      "Metro Construct Ltd",
      "1111111123",
      false,
      "V0009000013",
      TaxTreatment.HigherRate,
      "13 Jan 2026"
    ),
    SubcontractorsListRow(
      "novaBuilders",
      "Nova Builders",
      "1111111124",
      true,
      "V0009000014",
      TaxTreatment.Gross,
      "14 Jan 2026"
    ),
    SubcontractorsListRow(
      "orbitWorks",
      "Orbit Works",
      "1111111125",
      false,
      "V0009000015",
      TaxTreatment.StandardRate,
      "15 Jan 2026"
    ),
    SubcontractorsListRow(
      "pioneerLtd",
      "Pioneer Ltd",
      "1111111126",
      true,
      "V0009000016",
      TaxTreatment.HigherRate,
      "16 Jan 2026"
    )
  ).sortBy(_.name.toLowerCase)
}
