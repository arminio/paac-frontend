/*
 * Copyright 2016 HM Revenue & Customs
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

package form

import uk.gov.hmrc.play.test.UnitSpec

class PackageSpec extends UnitSpec {

    "Form package utilities" should {
      "poundsAndPenceField" must {
        "when isValidating is false returns big decimal field" in {
          // setup
          val isValidating = false

          // test
          val result = form.utilities.poundsAndPenceField(isValidating)

          // check
          result.bind(Map(""->"123.45")) match {
            case Left(error) => {
              info("unexpected")
              error shouldBe 0
            }
            case Right(bigdecimal) => bigdecimal shouldBe BigDecimal(123.45)
          }
        }

        "when isValidating is true returns a validating big decimal field" in {
          // setup
          val isValidating = true

          // test
          val result = form.utilities.poundsAndPenceField(isValidating)

          // check
          result.bind(Map(""->"50000013.45")) match {
            case Left(error) => {
              error(0).messages(0) shouldBe "errorbounds"
            }
            case Right(bigdecimal) => bigdecimal shouldBe 0
          }
        }
      }

    }
}
