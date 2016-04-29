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
                                amount2015P2:Option[BigDecimal]=None,
                                dcAmount2015P2:Option[BigDecimal]=None) {
  val THIS_YEAR = (new java.util.GregorianCalendar()).get(java.util.Calendar.YEAR)
  val START_YEAR = 2006

  def toPageValues():List[Contribution] = {
    val fieldValueMap: Map[String,Any]= this.getClass.getDeclaredFields.map(_.getName).zip(this.productIterator.toList).toMap
    def get(name:String): Option[Long] = fieldValueMap.get(name).map(_.asInstanceOf[Option[BigDecimal]]).flatMap(_.map((v:BigDecimal)=>(v * 100).longValue))
    def toInputAmounts(name1: String, name2: String): Option[InputAmounts] = {
      val a = InputAmounts(get(name1), get(name2))
      if (a.isEmpty) None else Some(a)
    }

    List.range(START_YEAR, THIS_YEAR).flatMap {
      (year:Int) =>
      if (year == 2015) {
        List(Contribution(TaxPeriod.PERIOD_1_2015_START, TaxPeriod.PERIOD_1_2015_END,toInputAmounts("amount2015P1","dcAmount2015P1")),
             Contribution(TaxPeriod.PERIOD_2_2015_START, TaxPeriod.PERIOD_2_2015_END,toInputAmounts("amount2015P2","dcAmount2015P2")))
      } else {
        List(Contribution(year, toInputAmounts("amount"+year, "dcAmount"+year)))
      }
    }
  }

  def toContributions():List[Contribution] = {
    toPageValues().filter(_.isEmpty() == false)
  }

  def hasDefinedContributions(): Boolean = dcAmount2015P1 != None || dcAmount2015P2 != None
  def hasDefinedBenefits(): Boolean = List(amount2006, amount2007, amount2008, amount2009, amount2010, amount2011, amount2012, amount2013, amount2014).exists(_ != None)

  def toDefinedBenefit(first: Contribution => Boolean)(key: String) : Option[(Long, String)] = {
    toContributions.find(first).flatMap {
      (c)=>
        (for {
          amounts <- c.amounts
          definedBenefit <- amounts.definedBenefit
        } yield definedBenefit).map((_,key))
    }
  }

  def toDefinedContribution(first: Contribution => Boolean)(key: String) : Option[(Long, String)] = {
    toContributions.find(first).flatMap {
      (c)=>
        (for {
          amounts <- c.amounts
          definedBenefit <- amounts.moneyPurchase
        } yield definedBenefit).map((_,key))
    }
  }

  def period1(): Contribution => Boolean = { (c) => c.taxPeriodEnd.day == 8 && c.taxPeriodEnd.year == 2015 } 
  def period2(): Contribution => Boolean = { (c) => c.taxPeriodStart.day == 9 && c.taxPeriodStart.year == 2015 } 

  def to1516Period1DefinedBenefit: Option[(Long, String)] = toDefinedBenefit(period1())("definedBenefit_2015_p1")
  def to1516Period1DefinedContribution: Option[(Long, String)] = toDefinedContribution(period1())("definedContribution_2015_p1")
  def to1516Period2DefinedBenefit: Option[(Long, String)] = toDefinedBenefit(period2())("definedBenefit_2015_p2")
  def to1516Period2DefinedContribution: Option[(Long, String)] = toDefinedContribution(period2())("definedContribution_2015_p2")
  // Useful for all years except 2015/16 Tax Year
  def toDefinedBenefit(year: Int) : Option[(Long, String)] = toDefinedBenefit((_.taxPeriodStart.year == year))("definedBenefit_"+year)
  def toDefinedContribution(year: Int) : Option[(Long, String)] = toDefinedContribution((_.taxPeriodStart.year == year))("definedContribution_"+year)
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
      "definedBenefit_2015_p2" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true),
      "definedContribution_2015_p2" -> optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true)
    )(CalculatorFormFields.apply)(CalculatorFormFields.unapply)
  )
}