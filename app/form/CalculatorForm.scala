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
import scala.math._
import play.api.data.validation._

/* Really horrible to have repeated fields however this will change shortly due to mini-tax years and tax periods. */
case class CalculatorFormFields(amount2008:BigDecimal=0,
                                amount2009:BigDecimal=0,
                                amount2010:BigDecimal=0,
                                amount2011:BigDecimal=0,
                                amount2012:BigDecimal=0,
                                amount2013:BigDecimal=0,
                                amount2014:BigDecimal=0) {
  def toContributions():List[Contribution] = {
    List(Contribution(2008,(amount2008*100).longValue),
         Contribution(2009,(amount2009*100).longValue),
         Contribution(2010,(amount2010*100).longValue),
         Contribution(2011,(amount2011*100).longValue),
         Contribution(2012,(amount2012*100).longValue),
         Contribution(2013,(amount2013*100).longValue),
         Contribution(2014,(amount2014*100).longValue)
         )
  }
}
object CalculatorFormFields

object CalculatorForm {
  type CalculatorFormType = CalculatorFormFields

  val form: Form[CalculatorFormType] = Form(
    mapping(
      "definedBenefit_2008" -> bigDecimal(10,2).verifying(Constraints.min[BigDecimal](0)),
      "definedBenefit_2009" -> bigDecimal(10,2).verifying(Constraints.min[BigDecimal](0)),
      "definedBenefit_2010" -> bigDecimal(10,2).verifying(Constraints.min[BigDecimal](0)),
      "definedBenefit_2011" -> bigDecimal(10,2).verifying(Constraints.min[BigDecimal](0)),
      "definedBenefit_2012" -> bigDecimal(10,2).verifying(Constraints.min[BigDecimal](0)),
      "definedBenefit_2013" -> bigDecimal(10,2).verifying(Constraints.min[BigDecimal](0)),
      "definedBenefit_2014" -> bigDecimal(10,2).verifying(Constraints.min[BigDecimal](0))
    )(CalculatorFormFields.apply)(CalculatorFormFields.unapply)
  )
}