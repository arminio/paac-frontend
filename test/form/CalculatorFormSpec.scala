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
          formWithErrors.errors.find(_.key == "definedBenefit_2008") should not be None
        },
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

    "throw validation error if defined benefit is out of bounds" in {
      CalculatorForm.form.bind(Map("definedBenefit_2014" -> "100000000.01")).fold (
        formWithErrors => {
          formWithErrors.errors should not be empty
          formWithErrors.errors.head.key shouldBe "definedBenefit_2014"
          formWithErrors.errors.head.messages.head shouldBe "error.real.precision"
        },
        formWithoutErrors => formWithoutErrors should not be Some("")
      )
    }

    "form correctly unbinds" in {
      CalculatorForm.form.bind(Map("definedBenefit_2014" -> "")).mapping.unbind(CalculatorFormFields(amount2014 = Some(scala.math.BigDecimal(0.0))))("definedBenefit_2014") shouldBe "0.00"
    }
  }

  "CalculatorFormFields" should {
    "convert to list of contributions with pence amounts" in {
      // set up
      val input = CalculatorFormFields(Some(50.50), Some(90.50), Some(100.50), Some(200.50), Some(300.50), Some(400.50), Some(500.50),
                                            Some(600.50), Some(700.50), Some(800.50), Some(900.50), Some(1000.50), Some(1100.50))

      // test
      val contributions = input.toContributions()

      // check
      contributions shouldBe List(Contribution(2006, 5050),
                                  Contribution(2007, 9050),
                                  Contribution(2008, 10050),
                                  Contribution(2009, 20050),
                                  Contribution(2010, 30050),
                                  Contribution(2011, 40050),
                                  Contribution(2012, 50050),
                                  Contribution(2013, 60050),
                                  Contribution(2014, 70050),
        Contribution(TaxPeriod(2015,3,6), TaxPeriod(2015,6,8), Some(InputAmounts(80050,90050))),
        Contribution(TaxPeriod(2015,6,9),TaxPeriod(2016,3,5), Some(InputAmounts(100050,110050))))
    }
  }
}