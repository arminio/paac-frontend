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
import service.KeystoreService._

class CalculatorFormSpec extends test.BaseSpec {
  var thisYear = 2016

  trait Year2016NoPence extends models.ThisYear {
    override def THIS_YEAR = thisYear
    override def POUNDS_AND_PENCE = false
  }

  trait Year2016WithPence extends models.ThisYear {
    override def THIS_YEAR = thisYear
    override def POUNDS_AND_PENCE = true
  }

  override def beforeAll() {
    super.beforeAll
    thisYear = (config.PaacConfiguration.year())
  }

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

    "throw validation error when providing a defined benefit value less than 0" in {
      CalculatorForm.form.bind(Map(
        s"definedBenefits.amount_$thisYear" -> "-1"
      )).fold(
        formWithErrors => {
          formWithErrors.errors.find(_.key == s"definedBenefits.amount_$thisYear") should not be None
        },
        success =>
          success should not be Some(-1)
      )
    }

    "throw validation error when providing a defined contribution value less than 0" in {
      CalculatorForm.form.bind(Map(
        s"definedContributions.amount_$thisYear" -> "-1"
      )).fold(
        formWithErrors =>
          formWithErrors.errors.find(_.key == s"definedContributions.amount_$thisYear") should not be None,
        success =>
          success should not be Some(-1)
      )
    }

    "throw a ValidationError when providing a character" in {
      CalculatorForm.form.bind(Map(
        s"definedBenefits.amount_$thisYear" -> "Idontknow"
      )).fold(
        formWithErrors => {
          formWithErrors.errors.head.message should not be ("character") },
        success =>
          success should not be Some("Idontknow")
      )
    }

    "throw a ValidationError when providing special characters" in {
      CalculatorForm.form.bind(Map(
        s"definedBenefits.amount_$thisYear" -> "%l&^@sl3"
      )).fold(
        formWithErrors =>
          formWithErrors.errors.head should not be ("Success"),
        success =>
          success should not be Some("%l&^@sl3")
      )
    }

    "throw validation error if defined benefit is out of bounds" in {
      CalculatorForm.form.bind(Map(s"definedBenefits.amount_$thisYear" -> "100000000.01")).fold (
        formWithErrors => {
          formWithErrors.errors should not be empty
          formWithErrors.errors.head.key shouldBe s"definedBenefits.amount_$thisYear"
          formWithErrors.errors.head.messages.head shouldBe "error.number"
        },
        formWithoutErrors => formWithoutErrors should not be Some("")
      )
    }

    "validating form correctly unbinds" in {
      // set up
      val model = CalculatorFormFields(Amounts(Some(scala.math.BigDecimal(0))),
                                       Amounts(Some(scala.math.BigDecimal(123))),
                                       Amounts(),
                                       Year2015Amounts(Some(scala.math.BigDecimal(987))),
                                       None,
                                       Some("2015-11-01"))

      // test
      val map = CalculatorForm.form.mapping.unbind(model)

      // check
      map(s"definedBenefits.amount_$thisYear") shouldBe "0"
      map(s"definedContributions.amount_$thisYear") shouldBe "123"
      map("year2015.definedBenefit_2015_p1") shouldBe "987"
    }

    "non-validating form correctly unbinds" in {
      // set up
      val model = CalculatorFormFields(Amounts(Some(scala.math.BigDecimal(0))),
                                       Amounts(Some(scala.math.BigDecimal(123))),
                                       Amounts(Some(scala.math.BigDecimal(555))),
                                       Year2015Amounts(Some(scala.math.BigDecimal(987))),
                                       Some(99),
                                       Some("2015-11-01"))

      // test
      val map = CalculatorForm.nonValidatingForm.mapping.unbind(model)

      // check
      map(s"definedBenefits.amount_$thisYear") shouldBe "0"
      map(s"definedContributions.amount_$thisYear") shouldBe "123"
      map(s"adjustedIncome.amount_$thisYear") shouldBe "555"
      map("year2015.definedBenefit_2015_p1") shouldBe "987"
      map("triggerAmount") shouldBe "99"
    }

    "support a form with amounts in pounds and pence" in {
      // set up
      var form = (new CalculatorForm() with Year2016WithPence).form
      val model = CalculatorFormFields(Amounts(Some(scala.math.BigDecimal(0))),
                                       Amounts(Some(scala.math.BigDecimal(123))),
                                       Amounts(Some(scala.math.BigDecimal(555))),
                                       Year2015Amounts(Some(scala.math.BigDecimal(987))),
                                       Some(99),
                                       Some("2015-11-01"))

      // test
      val map = form.mapping.unbind(model)

      // check
      map(s"definedBenefits.amount_$thisYear") shouldBe "0.00"
      map(s"definedContributions.amount_$thisYear") shouldBe "123.00"
      map(s"adjustedIncome.amount_$thisYear") shouldBe "555.00"
      map("year2015.definedBenefit_2015_p1") shouldBe "987.00"
      map("triggerAmount") shouldBe "99"
    }

    "support a non-validating form with amounts in pounds and pence" in {
      // set up
      var form = (new CalculatorForm() with Year2016WithPence).nonValidatingForm
      val model = CalculatorFormFields(Amounts(Some(scala.math.BigDecimal(0))),
                                       Amounts(Some(scala.math.BigDecimal(123))),
                                       Amounts(Some(scala.math.BigDecimal(555))),
                                       Year2015Amounts(Some(scala.math.BigDecimal(987))),
                                       Some(99),
                                       Some("2015-11-01"))

      // test
      val map = form.mapping.unbind(model)

      // check
      map(s"definedBenefits.amount_$thisYear") shouldBe "0.00"
      map(s"definedContributions.amount_$thisYear") shouldBe "123.00"
      map(s"adjustedIncome.amount_$thisYear") shouldBe "555.00"
      map("year2015.definedBenefit_2015_p1") shouldBe "987.00"
      map("triggerAmount") shouldBe "99"
    }

    "throw validation error if defined benefit (in pounds and pence) is out of bounds" in {
      var form = (new CalculatorForm() with Year2016WithPence).nonValidatingForm
      form.bind(Map(s"definedBenefits.amount_$thisYear" -> "500000000.01")).fold (
        formWithErrors => {
          formWithErrors.errors should not be empty
          formWithErrors.errors.head.key shouldBe s"definedBenefits.amount_$thisYear"
          formWithErrors.errors.head.messages.head shouldBe "error.real.precision"
        },
        formWithoutErrors => formWithoutErrors should not be Some("")
      )
    }
    "not throw validation error if defined benefit (in pounds and pence) is in of bounds" in {
      var form = (new CalculatorForm() with Year2016WithPence).nonValidatingForm
      form.bind(Map(s"definedBenefits.amount_$thisYear" -> "50000.01")).fold (
        formWithErrors => {
          formWithErrors shouldBe None
        },
        formWithoutErrors => formWithoutErrors.definedBenefits.currentYearMinus0 shouldBe Some(50000.01)
      )
    }

    "toAmount" should {
      "return given value if it was empty" in {
        // set up
        var form = new CalculatorForm() with Year2016WithPence {
          def test(poundsAndPence: String, isPoundsAndPence: Boolean): String = {
            super.toAmount(poundsAndPence, isPoundsAndPence)
          }
        }

        // test
        val result = form.test("", false)

        // check
        result shouldBe ""
      }
      "return formatted pounds and pence value if given amount was not empty" in {
        // set up
        var form = new CalculatorForm() with Year2016WithPence {
          def test(poundsAndPence: String, isPoundsAndPence: Boolean): String = {
            super.toAmount(poundsAndPence, isPoundsAndPence)
          }
        }

        // test
        val result = form.test("123456", true)

        // check
        result shouldBe "1234.56"
      }
      "return formatted pounds value if given amount was not empty" in {
        // set up
        var form = new CalculatorForm() with Year2016WithPence {
          def test(poundsAndPence: String, isPoundsAndPence: Boolean): String = {
            super.toAmount(poundsAndPence, isPoundsAndPence)
          }
        }

        // test
        val result = form.test("123456", false)

        // check
        result shouldBe "1235"
      }
      "return errors for out of bounds pound and pence value" ignore {
        // set up
        var form = new CalculatorForm() with Year2016WithPence

        // test
        val result = form.bind(Map[String,String](s"definedBenefits.amount_$thisYear" -> "50000000000.01")).fold (
            formWithErrors => {
              formWithErrors.errors should not be empty
              formWithErrors.errors.head.key shouldBe s"definedBenefits.amount_$thisYear"
              formWithErrors.errors.head.messages.head shouldBe "error.number"
            },
            formWithoutErrors => formWithoutErrors shouldBe None
          )
      }
    }

    "trigger amount should be bound" in {
      // set up
      val data = Map[String,String](TRIGGER_DC_KEY->"1234")
      val form = CalculatorForm.bind(data)

      // test
      val result = form.data.get("triggerAmount")

      // check
      result.isDefined shouldBe true
      result.get shouldBe "12"
    }
  }
}
