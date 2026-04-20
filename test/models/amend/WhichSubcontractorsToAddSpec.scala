package models.amend

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class WhichSubcontractorsToAddSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues
    with ModelGenerators {

  "WhichSubcontractorsToAdd" - {

    "must deserialise valid values" in {

      val gen = arbitrary[WhichSubcontractorsToAdd]

      forAll(gen) { whichSubcontractorsToAdd =>
        JsString(whichSubcontractorsToAdd.toString)
          .validate[WhichSubcontractorsToAdd]
          .asOpt
          .value mustEqual whichSubcontractorsToAdd
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!WhichSubcontractorsToAdd.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[WhichSubcontractorsToAdd] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[WhichSubcontractorsToAdd]

      forAll(gen) { whichSubcontractorsToAdd =>
        Json.toJson(whichSubcontractorsToAdd) mustEqual JsString(whichSubcontractorsToAdd.toString)
      }
    }
  }
}
