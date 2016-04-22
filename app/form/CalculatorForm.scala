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
                                amount2014:Option[BigDecimal]=None,
                                amount2015P1:Option[BigDecimal]=None,
                                dcAmount2015P1:Option[BigDecimal]=None,
                                amount2015P2:Option[BigDecimal]=None) {

  def toContributions():List[Contribution] = {
    def toPence(maybeAmount: Option[BigDecimal]) : Long = maybeAmount.map((v:BigDecimal)=>(v*100).longValue).getOrElse(0)

    val currentYear = (new java.util.GregorianCalendar()).get(java.util.Calendar.YEAR)
    val fieldValueMap: Map[String,Any]= this.getClass.getDeclaredFields.map(_.getName).zip(this.productIterator.toList).toMap
    val maybeContributions: List[Option[Contribution]] = List.range(2006, currentYear).flatMap {
      (year:Int) =>
        if (year != 2015) {
          fieldValueMap.get("amount" + year).map((v: Any) => Some(Contribution(year, toPence(v.asInstanceOf[Option[BigDecimal]]))))
        } else {
          None
        }
    }
    (maybeContributions ++ List(Some(Contribution(TaxPeriod(2015,3,6), TaxPeriod(2015,6,8), Some(InputAmounts(toPence(amount2015P1),toPence(dcAmount2015P1))))),
      Some(Contribution(TaxPeriod(2015,6,9), TaxPeriod(2016,3,5), Some(InputAmounts(toPence(amount2015P2))))))).filter(_!=None).map(_.get)

  }

  def to1516Period1DefinedBenefit: Option[(Long, String)] = {
    toContributions.find(_.taxPeriodEnd.day == 8).flatMap {
      (c)=>
        (for {
          amounts <- c.amounts
          definedBenefit <- amounts.definedBenefit
        } yield definedBenefit).map((_,"definedBenefit_"+c.taxPeriodStart.year+"_p1"))
    }
  }

  def to1516Period1DefinedContribution: Option[(Long, String)] = {
    toContributions.find(_.taxPeriodEnd.day == 8).flatMap {
      (c)=>
        (for {
          amounts <- c.amounts
          definedContribution <- amounts.moneyPurchase
        } yield definedContribution).map((_,"definedContribution_"+c.taxPeriodStart.year+"_p1"))
    }
  }

  def to1516Period2DefinedBenefit: Option[(Long, String)] = {
    toContributions.find(_.taxPeriodStart.day == 9).flatMap {
      (c)=>
        (for {
          amounts <- c.amounts
          definedBenefit <- amounts.definedBenefit
        } yield definedBenefit).map((_,"definedBenefit_"+c.taxPeriodStart.year+"_p2"))
    }
  }

  // Useful for all years except 2015/16 Tax Year
  def toDefinedBenefit(year: Int) : Option[(Long, String)] = {
    toContributions.find(_.taxPeriodStart.year == year).flatMap {
      (c)=>
        (for {
          amounts <- c.amounts
          definedBenefit <- amounts.definedBenefit
        } yield definedBenefit).map((_,"definedBenefit_"+c.taxPeriodStart.year))
    }
  }

}

object CalculatorFormFields

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
      "definedBenefit_2014" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true),
      "definedBenefit_2015_p1" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true),
      "definedContribution_2015_p1" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true),
      "definedBenefit_2015_p2" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true)
    )(CalculatorFormFields.apply)(CalculatorFormFields.unapply)
  )
}