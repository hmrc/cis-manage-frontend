package pages.subcontractors

import base.SpecBase
import play.api.libs.json.JsPath

class DeletedSubcontractorPageSpec extends SpecBase {

  "DeletedSubcontractorPage" - {

    "must have the correct path" in {
      DeletedSubcontractorPage.path mustBe JsPath \ "deletedSubcontractorName"
    }

    "must have the correct toString value" in {
      DeletedSubcontractorPage.toString mustBe "deletedSubcontractorName"
    }

    "must be able to store and retrieve deleted subcontractor name from UserAnswers" in {
      val subcontractorName = "ABC Contractors"

      val userAnswers =
        emptyUserAnswers
          .set(DeletedSubcontractorPage, subcontractorName)
          .success
          .value

      userAnswers.get(DeletedSubcontractorPage) mustBe Some(subcontractorName)
    }

    "must be able to remove deleted subcontractor name from UserAnswers" in {
      val subcontractorName = "ABC Contractors"

      val userAnswers =
        emptyUserAnswers
          .set(DeletedSubcontractorPage, subcontractorName)
          .success
          .value

      val updatedAnswers =
        userAnswers
          .remove(DeletedSubcontractorPage)
          .success
          .value

      updatedAnswers.get(DeletedSubcontractorPage) mustBe None
    }
  }
}
