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
      verified = "No",
      verificationNumber = "V0001256246",
      taxTreatment = "Standard rate",
      dateAdded = "11 May 2020"
    ),
    SubcontractorsListRow(
      id = "Hammondhouse",
      name = "Hammond House",
      utr = "2904743750",
      verified = "Yes",
      verificationNumber = "V0001217702",
      taxTreatment = "Gross",
      dateAdded = "1 Oct 2025"
    ),
    SubcontractorsListRow(
      id = "Ingenresearch",
      name = "InGen Research",
      utr = "9347488729",
      verified = "No",
      verificationNumber = "V0005617876",
      taxTreatment = "Standard rate",
      dateAdded = "1 Mar 2020"
    ),
    SubcontractorsListRow(
      id = "Malcolmandsattler",
      name = "Malcolm And Sattler",
      utr = "0074742762",
      verified = "Yes",
      verificationNumber = "V0004635231",
      taxTreatment = "Higher rate",
      dateAdded = "1 Oct 2025"
    ),
    SubcontractorsListRow(
      id = "brightwellPartners",
      name = "Brightwell Partners",
      utr = "1234567890",
      verified = "No",
      verificationNumber = "V0007771001",
      taxTreatment = "Standard rate",
      dateAdded = "23 Apr 2026"
    ),
    SubcontractorsListRow(
      id = "carterfieldsLtd",
      name = "Carterfields Ltd",
      utr = "2345678901",
      verified = "Yes",
      verificationNumber = "V0007771002",
      taxTreatment = "Gross",
      dateAdded = "23 Apr 2026"
    ),
    SubcontractorsListRow(
      id = "northbridgeBuild",
      name = "Northbridge Build",
      utr = "3456789012",
      verified = "No",
      verificationNumber = "V0007771003",
      taxTreatment = "Standard rate",
      dateAdded = "23 Apr 2026"
    ),
    SubcontractorsListRow(
      id = "oakthornServices",
      name = "Oakthorn Services",
      utr = "4567890123",
      verified = "Yes",
      verificationNumber = "V0007771004",
      taxTreatment = "Higher rate",
      dateAdded = "23 Apr 2026"
    ),
    SubcontractorsListRow(
      "alphaBuild",
      "Alpha Build Ltd",
      "1111111111",
      "No",
      "V0009000001",
      "Standard rate",
      "01 Jan 2026"
    ),
    SubcontractorsListRow(
      "betaConstruct",
      "Beta Construction",
      "1111111112",
      "Yes",
      "V0009000002",
      "Gross",
      "02 Jan 2026"
    ),
    SubcontractorsListRow(
      "charlieWorks",
      "Charlie Works Ltd",
      "1111111113",
      "No",
      "V0009000003",
      "Higher rate",
      "03 Jan 2026"
    ),
    SubcontractorsListRow(
      "deltaGroup",
      "Delta Group",
      "1111111114",
      "Yes",
      "V0009000004",
      "Standard rate",
      "04 Jan 2026"
    ),
    SubcontractorsListRow(
      "echoServices",
      "Echo Services",
      "1111111115",
      "No",
      "V0009000005",
      "Gross",
      "05 Jan 2026"
    ),
    SubcontractorsListRow(
      "foxtrotLtd",
      "Foxtrot Ltd",
      "1111111116",
      "Yes",
      "V0009000006",
      "Standard rate",
      "06 Jan 2026"
    ),
    SubcontractorsListRow(
      "gammaBuilders",
      "Gamma Builders",
      "1111111117",
      "No",
      "V0009000007",
      "Higher rate",
      "07 Jan 2026"
    ),
    SubcontractorsListRow(
      "harbourWorks",
      "Harbour Works",
      "1111111118",
      "Yes",
      "V0009000008",
      "Gross",
      "08 Jan 2026"
    ),
    SubcontractorsListRow(
      "ivoryConstruction",
      "Ivory Construction",
      "1111111119",
      "No",
      "V0009000009",
      "Standard rate",
      "09 Jan 2026"
    ),
    SubcontractorsListRow(
      "jupiterProjects",
      "Jupiter Projects",
      "1111111120",
      "Yes",
      "V0009000010",
      "Higher rate",
      "10 Jan 2026"
    ),
    SubcontractorsListRow(
      "kingstonBuild",
      "Kingston Build",
      "1111111121",
      "No",
      "V0009000011",
      "Gross",
      "11 Jan 2026"
    ),
    SubcontractorsListRow(
      "lunarDevelopments",
      "Lunar Developments",
      "1111111122",
      "Yes",
      "V0009000012",
      "Standard rate",
      "12 Jan 2026"
    ),
    SubcontractorsListRow(
      "metroConstruct",
      "Metro Construct Ltd",
      "1111111123",
      "No",
      "V0009000013",
      "Higher rate",
      "13 Jan 2026"
    ),
    SubcontractorsListRow(
      "novaBuilders",
      "Nova Builders",
      "1111111124",
      "Yes",
      "V0009000014",
      "Gross",
      "14 Jan 2026"
    ),
    SubcontractorsListRow(
      "orbitWorks",
      "Orbit Works",
      "1111111125",
      "No",
      "V0009000015",
      "Standard rate",
      "15 Jan 2026"
    ),
    SubcontractorsListRow(
      "pioneerLtd",
      "Pioneer Ltd",
      "1111111126",
      "Yes",
      "V0009000016",
      "Higher rate",
      "16 Jan 2026"
    )
  ).sortBy(_.name.toLowerCase)
}
