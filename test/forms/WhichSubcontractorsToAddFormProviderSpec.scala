package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.WhichSubcontractorsToAdd
import play.api.data.FormError

class WhichSubcontractorsToAddFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new WhichSubcontractorsToAddFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "whichSubcontractorsToAdd.error.required"

    behave like checkboxField[WhichSubcontractorsToAdd](
      form,
      fieldName,
      validValues  = WhichSubcontractorsToAdd.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
