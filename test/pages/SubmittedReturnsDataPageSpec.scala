package pages

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.JsPath

class SubmittedReturnsDataPageSpec extends AnyWordSpec with Matchers {

  "SubmittedReturnsDataPage" should {
    "have the correct path and string value" in {
      SubmittedReturnsDataPage.path shouldBe (JsPath \ "submittedReturnData")
      SubmittedReturnsDataPage.toString shouldBe "submittedReturnData"
    }
  }
}
