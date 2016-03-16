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

import models.{TaxPeriod, Contribution,InputAmounts}
import play.api.data.Form
import play.api.data.Forms._

/* Really horrible to have repeated fields however this will change shortly due to mini-tax years and tax periods. */
case class CalculatorFormFields(amount2008:Long=0,
                                amount2009:Long=0,
                                amount2010:Long=0,
                                amount2011:Long=0,
                                amount2012:Long=0,
                                amount2013:Long=0) {
  def toContributions():List[Contribution] = {
    List(Contribution(2008,amount2008),
         Contribution(2009,amount2009),
         Contribution(2010,amount2010),
         Contribution(2011,amount2011),
         Contribution(2012,amount2012),
         Contribution(2013,amount2013)
         )
  }
}
object CalculatorFormFields

object CalculatorForm {
  type CalculatorFormType = CalculatorFormFields

  val form: Form[CalculatorFormType] = Form(
    mapping(
      "definedBenefit_2008" -> longNumber,
      "definedBenefit_2009" -> longNumber,
      "definedBenefit_2010" -> longNumber,
      "definedBenefit_2011" -> longNumber,
      "definedBenefit_2012" -> longNumber,
      "definedBenefit_2013" -> longNumber
    )(CalculatorFormFields.apply)(CalculatorFormFields.unapply)
  )
}