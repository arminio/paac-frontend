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


class CalculatorFormSpec extends UnitSpec{

  "CalculatorForm" should {

    "throw validation error when form not filled" in {
      CalculatorForm.form.bind(Map(
        "pensionInputAmount" -> ""
      )).fold(
        formWithErrors =>
          formWithErrors.errors should not be empty,
        success =>
          success should not be Some("")
      )
    }

    "throw validation error when providing a value less than 0" in {
      CalculatorForm.form.bind(Map(
        "definedBenefit_2008" -> "-1"
      )).fold(
        formWithErrors => {
          formWithErrors.errors.head.message should not be ("Error")},
        success =>
          success should not be Some(-1)
      )
    }

    "throw a ValidationError when providing a character" in {
      CalculatorForm.form.bind(Map(
        "definedBenefit_2010" -> "Idontknow"
      )).fold(
        formWithErrors => {
          formWithErrors.errors.head.message should not be ("character") },
        success =>
          success should not be Some("Idontknow")
      )
    }

    "throw a ValidationError when providing special characters" in {
      CalculatorForm.form.bind(Map(
        "PensionInputAmount" -> "%l&^@sl3"
      )).fold(
        formWithErrors =>
          {formWithErrors.errors.head should not be ("Success")},
        success =>
          success should not be Some("%l&^@sl3")
      )
    }
  }
}
