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
import models._

class CalculatorFormSpec extends UnitSpec{
  val thisYear = (config.PaacConfiguration.year())

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
      CalculatorForm.bind(Map(
        s"definedBenefit_$thisYear" -> "-1"
      )).fold(
        formWithErrors => {
          formWithErrors.errors.find(_.key == s"definedBenefits.amount_$thisYear") should not be None
        },
        success =>
          success should not be Some(-1)
      )
    }

    "throw a ValidationError when providing a character" in {
      CalculatorForm.bind(Map(
        s"definedBenefit_$thisYear" -> "Idontknow"
      )).fold(
        formWithErrors => {
          formWithErrors.errors.head.message should not be ("character") },
        success =>
          success should not be Some("Idontknow")
      )
    }

    "throw a ValidationError when providing special characters" in {
      CalculatorForm.bind(Map(
        s"definedBenefit_$thisYear" -> "%l&^@sl3"
      )).fold(
        formWithErrors =>
          {formWithErrors.errors.head should not be ("Success")},
        success =>
          success should not be Some("%l&^@sl3")
      )
    }

    "throw validation error if defined benefit is out of bounds" in {
      CalculatorForm.bind(Map(s"definedBenefit_$thisYear" -> "100000000.01")).fold (
        formWithErrors => {
          formWithErrors.errors should not be empty
          formWithErrors.errors.head.key shouldBe s"definedBenefits.amount_$thisYear"
          formWithErrors.errors.head.messages.head shouldBe "error.real.precision"
        },
        formWithoutErrors => formWithoutErrors should not be Some("")
      )
    }

    "validating form correctly unbinds" in {
      // set up
      val model = CalculatorFormFields(Amounts(Some(scala.math.BigDecimal(0.0))),
                                       Amounts(Some(scala.math.BigDecimal(123.45))),
                                       Year2015Amounts(),
                                       None)
      val form = CalculatorForm.bind(Map(s"definedBenefits.amount_$thisYear" -> ""))

      // test
      val map = form.mapping.unbind(model)

      // check
      map(s"definedBenefits.amount_$thisYear") shouldBe "0.00"
      map(s"definedContributions.amount_$thisYear") shouldBe "123.45"
    }

    "non-validating form correctly unbinds" in {
      // set up
      val model = CalculatorFormFields(Amounts(Some(scala.math.BigDecimal(0.0))),
                                       Amounts(Some(scala.math.BigDecimal(123.45))),
                                       Year2015Amounts(),
                                       None)
      val form = CalculatorForm.bind(Map(s"definedBenefits.amount_$thisYear" -> ""), true)

      // test
      val map = form.mapping.unbind(model)

      // check
      map(s"definedBenefits.amount_$thisYear") shouldBe "0.00"
      map(s"definedContributions.amount_$thisYear") shouldBe "123.45"
    }
  }
}
