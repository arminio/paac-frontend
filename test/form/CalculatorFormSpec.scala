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

import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec


object CalculatorFormSpec extends UnitSpec{

  "CalculatorForm" should {

    "throw validation error when form not filled" in {
      CalculatorForm.form.bind(Map(
        "pensionInputAmount" -> ""
      )).fold(
        errors =>
          errors.errors.head.message shouldBe Messages(""),
        success =>
          success should not be Some("")
      )
    }

    "throw validation error when providing a value less than 0" in {
      CalculatorForm.form.bind(Map(
        "pensionInputAmount" -> "-0.01"
      )).fold(
        errors =>
          errors.errors.head.message shouldBe Messages("cc.childcare.cost.error.not.a.number"),
        success =>
          success should not be Some(-0.01)
      )
    }

    "throw validation error when form not filled" in {
      CalculatorForm.form.bind(Map(
        "pensionInputAmount" -> ""
      )).fold(
        errors =>
          errors.errors.head.message shouldBe Messages("cc.childcare.cost.error.required"),
        success =>
          success should not be Some("")
      )
    }
  }
}
