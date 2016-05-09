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

package models

import form.CalculatorForm

class CalculatorFormFieldSpec extends ModelSpec {
  "CalculatorFormFields" should {
    "convert values to pence amounts" in {
      // set up
      val input = CalculatorFormFields(Amounts(Some(50.50), Some(90.50), Some(100.50), Some(200.50), Some(300.50), Some(400.50), Some(500.50), Some(600.50)), 
                                       Amounts(Some(700.50), Some(800.50), Some(900.50), Some(1000.50), Some(1100.50), Some(1200.50), Some(1300.50), Some(1400.50)),
                                       Year2015Amounts(Some(1500.50), Some(1600.50), Some(1700.50), Some(1800.50), None, None), None)
      val THIS_YEAR = (new java.util.GregorianCalendar()).get(java.util.Calendar.YEAR)

      // test
      val maybeTuple = input.toDefinedBenefit(THIS_YEAR)

      // check
      maybeTuple should not be None
      maybeTuple.get._1 shouldBe 5050L
      maybeTuple.get._2 shouldBe "definedBenefit_"+THIS_YEAR
    }
  }
}
