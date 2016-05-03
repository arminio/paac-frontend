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
import play.api.mvc._

case class DefinedBenefits(currentYearMinus0:Option[BigDecimal]=None,
                           currentYearMinus1:Option[BigDecimal]=None,
                           currentYearMinus2:Option[BigDecimal]=None,
                           currentYearMinus3:Option[BigDecimal]=None,
                           currentYearMinus4:Option[BigDecimal]=None,
                           currentYearMinus5:Option[BigDecimal]=None,
                           currentYearMinus6:Option[BigDecimal]=None,
                           currentYearMinus7:Option[BigDecimal]=None,
                           currentYearMinus8:Option[BigDecimal]=None) {
  def isEmpty(): Boolean = currentYearMinus0 == None && 
                           currentYearMinus1 == None && 
                           currentYearMinus2 == None && 
                           currentYearMinus3 == None && 
                           currentYearMinus4 == None && 
                           currentYearMinus5 == None && 
                           currentYearMinus6 == None && 
                           currentYearMinus7 == None && 
                           currentYearMinus8 == None
}

case class DefinedContributions(currentYearMinus0:Option[BigDecimal]=None,
                                currentYearMinus1:Option[BigDecimal]=None,
                                currentYearMinus2:Option[BigDecimal]=None,
                                currentYearMinus3:Option[BigDecimal]=None,
                                currentYearMinus4:Option[BigDecimal]=None,
                                currentYearMinus5:Option[BigDecimal]=None,
                                currentYearMinus6:Option[BigDecimal]=None,
                                currentYearMinus7:Option[BigDecimal]=None,
                                currentYearMinus8:Option[BigDecimal]=None) {
  def isEmpty(): Boolean = currentYearMinus0 == None && 
                           currentYearMinus1 == None && 
                           currentYearMinus2 == None && 
                           currentYearMinus3 == None && 
                           currentYearMinus4 == None && 
                           currentYearMinus5 == None && 
                           currentYearMinus6 == None && 
                           currentYearMinus7 == None && 
                           currentYearMinus8 == None
}

case class Year2015Amounts(amount2015P1:Option[BigDecimal]=None,
                           dcAmount2015P1:Option[BigDecimal]=None,
                           amount2015P2:Option[BigDecimal]=None,
                           dcAmount2015P2:Option[BigDecimal]=None) {
  def isEmpty(): Boolean = amount2015P1 == None && 
                         dcAmount2015P1 == None && 
                         amount2015P2 == None && 
                         dcAmount2015P2 == None
  def hasDefinedContributions(): Boolean = dcAmount2015P1 != None && dcAmount2015P2 != None
  def hasDefinedBenefits(): Boolean = amount2015P1 != None && amount2015P2 != None
}

case class CalculatorFormFields(db: DefinedBenefits, dc: DefinedContributions, year2015: Year2015Amounts) {
  val THIS_YEAR = (new java.util.GregorianCalendar()).get(java.util.Calendar.YEAR)
  val START_YEAR = THIS_YEAR-8

  def toPageValues():List[Contribution] = {
    def get(name:String): Option[Long] = {
      name match {
        case "amount2015P1" => year2015.amount2015P1.map((v:BigDecimal)=>(v * 100).longValue)
        case "dcAmount2015P1" => year2015.dcAmount2015P1.map((v:BigDecimal)=>(v * 100).longValue)
        case "amount2015P2" => year2015.amount2015P2.map((v:BigDecimal)=>(v * 100).longValue)
        case "dcAmount2015P2" => year2015.dcAmount2015P2.map((v:BigDecimal)=>(v * 100).longValue)
        case _ => {
          if (name.contains("dbCurrentYearMinus")) {
            val fieldValueMap: Map[String,Any]= this.db.getClass.getDeclaredFields.map(_.getName).zip(this.db.productIterator.toList).toMap
            fieldValueMap.get("c" + name.drop(3)).map(_.asInstanceOf[Option[BigDecimal]]).flatMap(_.map((v:BigDecimal)=>(v * 100).longValue))
          } else if (name.contains("dcCurrentYearMinus")) {
            val fieldValueMap: Map[String,Any]= this.dc.getClass.getDeclaredFields.map(_.getName).zip(this.dc.productIterator.toList).toMap
            fieldValueMap.get("c" + name.drop(3)).map(_.asInstanceOf[Option[BigDecimal]]).flatMap(_.map((v:BigDecimal)=>(v * 100).longValue))
          } else {
            None
          }
        }
      }
    }

    def toInputAmounts(name1: String, name2: String): Option[InputAmounts] = {
      val a = InputAmounts(get(name1), get(name2))
      if (a.isEmpty) None else Some(a)
    }

    List.range(START_YEAR, THIS_YEAR+1).flatMap {
      (year:Int) =>
      if (year == 2015) {
        List(Contribution(TaxPeriod.PERIOD_1_2015_START, TaxPeriod.PERIOD_1_2015_END,toInputAmounts("amount2015P1","dcAmount2015P1")),
             Contribution(TaxPeriod.PERIOD_2_2015_START, TaxPeriod.PERIOD_2_2015_END,toInputAmounts("amount2015P2","dcAmount2015P2")))
      } else {
        val delta = THIS_YEAR - year
        List(Contribution(year, toInputAmounts("dbCurrentYearMinus"+delta, "dcCurrentYearMinus"+delta)))
      }
    }
  }

  def toContributions():List[Contribution] = {
    toPageValues().filter(_.isEmpty() == false)
  }

  def hasDefinedContributions(): Boolean = !dc.isEmpty || year2015.hasDefinedContributions
  def hasDefinedBenefits(): Boolean = !db.isEmpty || year2015.hasDefinedBenefits

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
  val THIS_YEAR = (new java.util.GregorianCalendar()).get(java.util.Calendar.YEAR)
  val CY0 = THIS_YEAR
  val CY1 = THIS_YEAR-1
  val CY2 = THIS_YEAR-2
  val CY3 = THIS_YEAR-3
  val CY4 = THIS_YEAR-4
  val CY5 = THIS_YEAR-5
  val CY6 = THIS_YEAR-6
  val CY7 = THIS_YEAR-7
  val CY8 = THIS_YEAR-8
  val validator = optional(bigDecimal(10,2)).verifying("error.bounds",value=>if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true)

  val form: Form[CalculatorFormType] = Form(
    mapping("definedBenefits" -> mapping(s"amount_${CY0}"->validator,
                                         s"amount_${CY1}"->validator,
                                         s"amount_${CY2}"->validator,
                                         s"amount_${CY3}"->validator,
                                         s"amount_${CY4}"->validator,
                                         s"amount_${CY5}"->validator,
                                         s"amount_${CY6}"->validator,
                                         s"amount_${CY7}"->validator,
                                         s"amount_${CY8}"->validator)(DefinedBenefits.apply)(DefinedBenefits.unapply),
            "definedContributions" -> mapping(s"amount_${CY0}"->validator,
                                              s"amount_${CY1}"->validator,
                                              s"amount_${CY2}"->validator,
                                              s"amount_${CY3}"->validator,
                                              s"amount_${CY4}"->validator,
                                              s"amount_${CY5}"->validator,
                                              s"amount_${CY6}"->validator,
                                              s"amount_${CY7}"->validator,
                                              s"amount_${CY8}"->validator)(DefinedContributions.apply)(DefinedContributions.unapply),
            "year2015" -> mapping("definedBenefit_2015_p1"->validator,
                                  "definedContribution_2015_p1"->validator,
                                  "definedBenefit_2015_p2"->validator,
                                  "definedContribution_2015_p2"->validator)(Year2015Amounts.apply)(Year2015Amounts.unapply)
    )(CalculatorFormFields.apply)(CalculatorFormFields.unapply)
  )

  def bind(data: Map[String, String]): Form[CalculatorFormType] = {
    val year2015 = List((s"year2015.definedBenefit_2015_p1", data.getOrElse("amount2015P1", data.getOrElse("definedBenefit_2015_p1",""))),
                        (s"year2015.definedContribution_2015_p1", data.getOrElse("dcAmount2015P1", data.getOrElse("definedContribution_2015_p1",""))),
                        (s"year2015.definedBenefit_2015_p2", data.getOrElse("amount2015P2", data.getOrElse("definedBenefit_2015_p2",""))),
                        (s"year2015.definedContribution_2015_p2", data.getOrElse("dcAmount2015P2", data.getOrElse("definedContribution_2015_p2",""))))
    val values = List.range(THIS_YEAR-8, THIS_YEAR+1).flatMap {
      (year)=>
      if (year != 2015){
        List((s"definedBenefits.amount_${year}", data.getOrElse("definedBenefit_"+year, "")),
            (s"definedContributions.amount_${year}", data.getOrElse("definedContribution_"+year, "")))
      } else {
        year2015
      }
    }
    CalculatorForm.form.bind(Map(values: _*))
  }
}