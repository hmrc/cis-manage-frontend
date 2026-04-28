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

package utils

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class IrMarkReferenceGeneratorSpec extends AnyFreeSpec with Matchers {

  private val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

  "IrMarkReferenceGenerator.fromBase64" - {

    "must convert a known IR mark (base64) to the expected base32 reference without padding" in {
      val base64   = "Pyy1LRJh053AE+nuyp0GJR7oESw="
      val expected = "H4WLKLISMHJZ3QAT5HXMVHIGEUPOQEJM"

      IrMarkReferenceGenerator.fromBase64(base64) mustBe expected
    }

    "must be stable for random bytes and contain only base32 alphabet" in {
      val rnd   = new scala.util.Random(42)
      val bytes = Array.fill[Byte](64)((rnd.nextInt(256) - 128).toByte)
      val b64   = java.util.Base64.getEncoder.encodeToString(bytes)

      val out = IrMarkReferenceGenerator.fromBase64(b64)

      out must not be empty
      out.forall(ch => base32Chars.contains(ch)) mustBe true
    }

    "must return empty string for empty input" in {
      IrMarkReferenceGenerator.fromBase64("") mustBe ""
    }

    "must encode 1 remaining byte (2-char output)" in {
      // 1 zero byte encodes to base64 "AA==" and base32 "AA"
      IrMarkReferenceGenerator.fromBase64("AA==") mustBe "AA"
    }

    "must encode 2 remaining bytes (4-char output)" in {
      // 2 zero bytes encode to base64 "AAA=" and base32 "AAAA"
      IrMarkReferenceGenerator.fromBase64("AAA=") mustBe "AAAA"
    }

    "must encode 3 remaining bytes (5-char output)" in {
      // 3 zero bytes encode to base64 "AAAA" and base32 "AAAAA"
      IrMarkReferenceGenerator.fromBase64("AAAA") mustBe "AAAAA"
    }

    "must encode 4 remaining bytes (7-char output)" in {
      // 4 zero bytes encode to base64 "AAAAAA==" and base32 "AAAAAAA"
      IrMarkReferenceGenerator.fromBase64("AAAAAA==") mustBe "AAAAAAA"
    }

    "must throw for invalid base64 input" in {
      a[IllegalArgumentException] should be thrownBy IrMarkReferenceGenerator.fromBase64("not-valid!!!")
    }
  }
}
