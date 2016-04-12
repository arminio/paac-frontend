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
import play.api.data.validation.Constraint
import scala.math._
import play.api.data.validation._
import play.api.i18n.Messages

/* Really horrible to have repeated fields however this will change shortly due to mini-tax years and tax periods. */
case class CalculatorFormFields(amount2006:Option[BigDecimal]=None,
                                amount2007:Option[BigDecimal]=None,
                                amount2008:Option[BigDecimal]=None,
                                amount2009:Option[BigDecimal]=None,
                                amount2010:Option[BigDecimal]=None,
                                amount2011:Option[BigDecimal]=None,
                                amount2012:Option[BigDecimal]=None,
                                amount2013:Option[BigDecimal]=None,
                                amount2014:Option[BigDecimal]=None) {
  def toContributions():List[Contribution] = {
    def toPence(maybeAmount: Option[BigDecimal]) : Long = maybeAmount.map((v:BigDecimal)=>(v*100).longValue).getOrElse(0)

    val currentYear = (new java.util.GregorianCalendar()).get(java.util.Calendar.YEAR)
    val fieldValueMap: Map[String,Any]= this.getClass.getDeclaredFields.map(_.getName).zip(this.productIterator.toList).toMap
    val maybeContributions = List.range(2006, currentYear).map {
      (year:Int) =>
      fieldValueMap.get("amount"+year).map((v:Any)=>Contribution(year,toPence(v.asInstanceOf[Option[BigDecimal]])))
    }
    maybeContributions.filter(_!=None).map(_.get)
  }

  def to1516P1DefinedBenefit(year: Int) : Option[(Long, String)] = {
    toContributions.find(_.taxPeriodStart.year == year).flatMap {
      (c)=>
        (for {
          amounts <- c.amounts
          definedBenefit <- amounts.definedBenefit
        } yield definedBenefit).map((_,"definedBenefit_"+c.taxPeriodStart.year+"_p1"))
    }
  }

}

object CalculatorFormFields {
}

object CalculatorForm {
  type CalculatorFormType = CalculatorFormFields

  val form: Form[CalculatorFormType] = Form(
    mapping(
      "definedBenefit_2006" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true),
      "definedBenefit_2007" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true),
      "definedBenefit_2008" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true),
      "definedBenefit_2009" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true),
      "definedBenefit_2010" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true),
      "definedBenefit_2011" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true),
      "definedBenefit_2012" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true),
      "definedBenefit_2013" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true),
      "definedBenefit_2014" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true)
    )(CalculatorFormFields.apply)(CalculatorFormFields.unapply)
  )
}