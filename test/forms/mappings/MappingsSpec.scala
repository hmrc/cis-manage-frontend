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

package forms.mappings

import forms.Validation.{clientNameInputMaxLength, clientReferenceInputMaxLength, employerReferenceInputMaxLength}
import generators.StringGenerators
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.*
import play.api.data.Forms.*
import play.api.data.{Form, FormError}
import models.Enumerable
import models.agent.ClientListFormData

object MappingsSpec {

  sealed trait Foo
  case object Bar extends Foo
  case object Baz extends Foo

  object Foo {

    val values: Set[Foo] = Set(Bar, Baz)

    implicit val fooEnumerable: Enumerable[Foo] =
      Enumerable(values.toSeq.map(v => v.toString -> v): _*)
  }
}

class MappingsSpec extends AnyFreeSpec with Matchers with OptionValues with Mappings with StringGenerators {

  import MappingsSpec._

  "text" - {

    val testForm: Form[String] =
      Form(
        "value" -> text()
      )

    "must bind a valid string" in {
      val result = testForm.bind(Map("value" -> "foobar"))
      result.get mustEqual "foobar"
    }

    "must not bind an empty string" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind a string of whitespace only" in {
      val result = testForm.bind(Map("value" -> " \t"))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must return a custom error message" in {
      val form   = Form("value" -> text("custom.error"))
      val result = form.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "custom.error"))
    }

    "must unbind a valid value" in {
      val result = testForm.fill("foobar")
      result.apply("value").value.value mustEqual "foobar"
    }
  }

  "boolean" - {

    val testForm: Form[Boolean] =
      Form(
        "value" -> boolean()
      )

    "must bind true" in {
      val result = testForm.bind(Map("value" -> "true"))
      result.get mustEqual true
    }

    "must bind false" in {
      val result = testForm.bind(Map("value" -> "false"))
      result.get mustEqual false
    }

    "must not bind a non-boolean" in {
      val result = testForm.bind(Map("value" -> "not a boolean"))
      result.errors must contain(FormError("value", "error.boolean"))
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must unbind" in {
      val result = testForm.fill(true)
      result.apply("value").value.value mustEqual "true"
    }
  }

  "int" - {

    val testForm: Form[Int] =
      Form(
        "value" -> int()
      )

    "must bind a valid integer" in {
      val result = testForm.bind(Map("value" -> "1"))
      result.get mustEqual 1
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must unbind a valid value" in {
      val result = testForm.fill(123)
      result.apply("value").value.value mustEqual "123"
    }
  }

  "enumerable" - {

    val testForm = Form(
      "value" -> enumerable[Foo]()
    )

    "must bind a valid option" in {
      val result = testForm.bind(Map("value" -> "Bar"))
      result.get mustEqual Bar
    }

    "must not bind an invalid option" in {
      val result = testForm.bind(Map("value" -> "Not Bar"))
      result.errors must contain(FormError("value", "error.invalid"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }
  }

  "currency" - {

    val testForm: Form[BigDecimal] =
      Form(
        "value" -> currency()
      )

    "must bind a valid integer" in {
      val result = testForm.bind(Map("value" -> "1"))
      result.get mustEqual 1
    }

    "must bind a valid decimal with 1 decimal place" in {
      val result = testForm.bind(Map("value" -> "1.2"))
      result.get mustEqual 1.2
    }

    "must bind a valid decimal with 2 decimal places" in {
      val result = testForm.bind(Map("value" -> "1.23"))
      result.get mustEqual 1.23
    }

    "must bind a valid number with spaces, commas and `£` characters" in {
      val result = testForm.bind(Map("value" -> "£ 1,234 . 01"))
      result.get mustEqual 1234.01
    }

    "must not bind values with a `£` after any numbers" in {
      val result = testForm.bind(Map("value" -> "123 £456"))
      result.errors must contain only FormError("value", "error.nonNumeric")
    }

    "must not bind values with non-numeric characters except commas, spaces and `£`s" in {
      val result = testForm.bind(Map("value" -> "abc"))
      result.errors must contain only FormError("value", "error.nonNumeric")
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind a number with more than 2 decimal places" in {
      val result = testForm.bind(Map("value" -> "1.234"))
      result.errors must contain only FormError("value", "error.invalidNumeric")
    }

    "must not bind negative numbers" in {
      val result = testForm.bind(Map("value" -> "-1"))
      result.errors must contain only FormError("value", "error.nonNumeric")
    }

    "must unbind a valid value" in {
      val result = testForm.fill(1)
      result.apply("value").value.value mustEqual "1"
    }
  }

  "clientListSearchMapping" - {

    def requiredKey(key: String): String = s"agent.clientListSearch.$key.error.required"

    val testForm: Form[ClientListFormData] =
      Form(
        "value" -> clientListSearchMapping(requiredKey)
      )

    "must bind a valid searchBy and searchFilter" in {
      val result = testForm.bind(Map("searchBy" -> "CN", "searchFilter" -> "ABC"))
      result.get mustEqual ClientListFormData("CN", "ABC")
    }

    "must not bind an empty searchBy" in {
      val result = testForm.bind(Map("searchBy" -> ""))
      result.errors must contain(FormError("searchBy", "agent.clientListSearch.searchBy.error.required"))
    }

    "must not bind an invalid searchBy" in {
      val result = testForm.bind(Map("searchBy" -> "XX"))
      result.errors must contain(FormError("searchBy", "agent.clientListSearch.searchBy.error.required"))
    }

    "searchFilter for client name" - {
      "must bind a valid client name searchFilter" in {
        val result = testForm.bind(Map("searchBy" -> "CN", "searchFilter" -> "ABC"))
        result.get mustEqual ClientListFormData("CN", "ABC")
      }
      "must not bind an invalid client name searchFilter" in {
        val result = testForm.bind(Map("searchBy" -> "CN", "searchFilter" -> "$"))
        result.errors must contain(FormError("searchFilter", "agent.clientListSearch.searchFilter.cn.error.format"))
      }
      "must not bind an empty client name searchFilter" in {
        val result = testForm.bind(Map("searchBy" -> "CN", "searchFilter" -> ""))
        result.errors must contain(FormError("searchFilter", "agent.clientListSearch.searchFilter.cn.error.required"))
      }
      s"must not bind a client name searchFilter that exceeds $clientNameInputMaxLength" in {
        val stringInput = randomAlphaNumericStringGenerator(clientNameInputMaxLength + 1)
        val result      = testForm.bind(Map("searchBy" -> "CN", "searchFilter" -> stringInput))
        result.errors must contain(FormError("searchFilter", "agent.clientListSearch.searchFilter.cn.error.length"))
      }
    }

    "searchFilter for client reference" - {
      "must bind a valid client reference searchFilter" in {
        val result = testForm.bind(Map("searchBy" -> "CR", "searchFilter" -> "ABC"))
        result.get mustEqual ClientListFormData("CR", "ABC")
      }
      "must not bind an invalid client reference searchFilter" in {
        val result = testForm.bind(Map("searchBy" -> "CR", "searchFilter" -> "$"))
        result.errors must contain(FormError("searchFilter", "agent.clientListSearch.searchFilter.cr.error.format"))
      }
      "must not bind an empty client reference searchFilter" in {
        val result = testForm.bind(Map("searchBy" -> "CR", "searchFilter" -> ""))
        result.errors must contain(FormError("searchFilter", "agent.clientListSearch.searchFilter.cr.error.required"))
      }
      s"must not bind a client reference searchFilter that exceeds $clientReferenceInputMaxLength" in {
        val stringInput = randomAlphaNumericStringGenerator(clientReferenceInputMaxLength + 1)
        val result      = testForm.bind(Map("searchBy" -> "CR", "searchFilter" -> stringInput))
        result.errors must contain(FormError("searchFilter", "agent.clientListSearch.searchFilter.cr.error.length"))
      }
    }

    "searchFilter for employer reference" - {
      "must bind a valid employer reference searchFilter" in {
        val result = testForm.bind(Map("searchBy" -> "ER", "searchFilter" -> "ABC"))
        result.get mustEqual ClientListFormData("ER", "ABC")
      }
      "must not bind an invalid employer reference searchFilter" in {
        val result = testForm.bind(Map("searchBy" -> "ER", "searchFilter" -> "$"))
        result.errors must contain(FormError("searchFilter", "agent.clientListSearch.searchFilter.er.error.format"))
      }
      "must not bind an empty employer reference searchFilter" in {
        val result = testForm.bind(Map("searchBy" -> "ER", "searchFilter" -> ""))
        result.errors must contain(FormError("searchFilter", "agent.clientListSearch.searchFilter.er.error.required"))
      }
      s"must not bind a employer reference searchFilter that exceeds $employerReferenceInputMaxLength" in {
        val stringInput = randomAlphaNumericStringGenerator(employerReferenceInputMaxLength + 1)
        val result      = testForm.bind(Map("searchBy" -> "ER", "searchFilter" -> stringInput))
        result.errors must contain(FormError("searchFilter", "agent.clientListSearch.searchFilter.er.error.length"))
      }
    }

  }

}
