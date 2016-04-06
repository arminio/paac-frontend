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

class PensionInputFormSpec extends UnitSpec {

  "PensionInputFormFields" can {
    "have default value of 0 for 2014" in {
      // setup
      val formFields = PensionInputFormFields()

      // check
      formFields.amount2014 shouldBe 0
    }

    "toContributions return pence amount of value" in {
      // setup
      val formFields = PensionInputFormFields(789835319.79)

      // test
      val contributions = formFields.toContributions()

      // check
      contributions.head.taxPeriodStart.year shouldBe 2014
      contributions.head.amounts.get.definedBenefit.get shouldBe 78983531979L
    }

    "toDefinedBenefit for 2014 will return some amount and field name" in {
      // setup
      val formFields = PensionInputFormFields(3328238.83)

      // test
      val maybeTuple = formFields.toDefinedBenefit(2014)

      // check
      maybeTuple.isDefined shouldBe true
      maybeTuple.get._1 shouldBe 332823883L
      maybeTuple.get._2 shouldBe "definedBenefit_2014"
    }

    "toDefinedBenefit for 2013 will return none" in {
      // setup
      val formFields = PensionInputFormFields()

      // test
      val maybeTuple = formFields.toDefinedBenefit(2013)

      // check
      maybeTuple.isDefined shouldBe false
    }
  }

  "PensionInputForm" can {
    "throw validation error if defined benefit is less than 0" in {
      PensionInputForm.form.bind(Map("definedBenefit_2014" -> "-1")).fold (
        formWithErrors => {
          formWithErrors.errors should not be empty
          formWithErrors.errors.head.key shouldBe "definedBenefit_2014"
          formWithErrors.errors.head.messages.head shouldBe "error.min"
          formWithErrors.errors.head.args.head shouldBe 0
        },
        formWithoutErrors => formWithoutErrors should not be Some("")
      )
    }

    "throw validation error if defined benefit is more than 9999999.99" in {
      PensionInputForm.form.bind(Map("definedBenefit_2014" -> "10000000.00")).fold (
        formWithErrors => {
          formWithErrors.errors should not be empty
          formWithErrors.errors.head.key shouldBe "definedBenefit_2014"
          formWithErrors.errors.head.messages.head shouldBe "error.max"
          formWithErrors.errors.head.args.head shouldBe 9999999.99
        },
        formWithoutErrors => formWithoutErrors should not be Some("")
      )
    }

    "throw validation error if defined benefit is very large" in {
      PensionInputForm.form.bind(Map("definedBenefit_2014" -> "10000000000.00")).fold (
        formWithErrors => {
          formWithErrors.errors should not be empty
          formWithErrors.errors.head.key shouldBe "definedBenefit_2014"
          formWithErrors.errors.head.messages.head shouldBe "error.real.precision"
          formWithErrors.errors.head.args.head shouldBe 10
        },
        formWithoutErrors => formWithoutErrors should not be Some("")
      )
    }

    "throw validation error if defined benefit is blank" in {
      PensionInputForm.form.bind(Map("definedBenefit_2014" -> "")).fold (
        formWithErrors => {
          formWithErrors.errors should not be empty
          formWithErrors.errors.head.key shouldBe "definedBenefit_2014"
          formWithErrors.errors.head.messages.head shouldBe "error.real.precision"
          formWithErrors.errors.head.args.head shouldBe 10
        },
        formWithoutErrors => formWithoutErrors should not be Some("")
      )
    }

    "return form correctly bound" in {
      PensionInputForm.form.bind(Map("definedBenefit_2014" -> "893.50")).fold (
        formWithErrors => {
          formWithErrors.errors shouldBe empty
        },
        formWithoutErrors => {
          formWithoutErrors.amount2014 shouldBe scala.math.BigDecimal(893.50)
        }
      )
    }

    "form correctly unbinds" in {
      PensionInputForm.form.bind(Map("definedBenefit_2014" -> "")).mapping.unbind(PensionInputFormFields())("definedBenefit_2014") shouldBe "0.00"
    }
  }
}
