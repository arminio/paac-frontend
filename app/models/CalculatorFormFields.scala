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

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint
import scala.math._
import play.api.data.validation._
import play.api.i18n.Messages
import play.api.mvc._
import service.KeystoreService

case class Amounts(currentYearMinus0:Option[BigDecimal]=None,
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
                           dcAmount2015P2:Option[BigDecimal]=None,
                           postTriggerDcAmount2015P1:Option[BigDecimal]=None,
                           postTriggerDcAmount2015P2:Option[BigDecimal]=None) {
  def isEmpty(): Boolean = amount2015P1 == None && 
                           dcAmount2015P1 == None && 
                           amount2015P2 == None && 
                           dcAmount2015P2 == None && 
                           postTriggerDcAmount2015P1 == None && 
                           postTriggerDcAmount2015P2 == None
  def hasDefinedContributions(): Boolean = dcAmount2015P1 != None || dcAmount2015P2 != None || postTriggerDcAmount2015P1 != None || postTriggerDcAmount2015P2 != None
  def hasDefinedBenefits(): Boolean = amount2015P1 != None || amount2015P2 != None
}

trait ThisYear {
  def THIS_YEAR = config.PaacConfiguration.year()
}

case class CalculatorFormFields(definedBenefits: Amounts, 
                                definedContributions: Amounts, 
                                year2015: Year2015Amounts,
                                triggerDate: Option[String]) extends ThisYear {
  settings: ThisYear =>

  val START_YEAR = settings.THIS_YEAR-8

  def toPageValues():List[Contribution] = {
    def retrieveValue(amounts: Amounts, name: String): Option[Long] = {
      val fieldValueMap: Map[String,Any]= amounts.getClass.getDeclaredFields.map(_.getName).zip(amounts.productIterator.toList).toMap
      fieldValueMap.get("c" + name.drop(3)).map(_.asInstanceOf[Option[BigDecimal]]).flatMap(_.map(Amounts.toPence))
    }

    def get(name:String): Option[Long] = {
      name match {
        case "amount2015P1" => year2015.amount2015P1.map(Amounts.toPence)
        case "dcAmount2015P1" => year2015.dcAmount2015P1.map(Amounts.toPence)
        case "amount2015P2" => year2015.amount2015P2.map(Amounts.toPence)
        case "dcAmount2015P2" => year2015.dcAmount2015P2.map(Amounts.toPence)
        case "ptDcAmount2015P1" => year2015.postTriggerDcAmount2015P1.map(Amounts.toPence)
        case "ptDcAmount2015P2" => year2015.postTriggerDcAmount2015P2.map(Amounts.toPence)
        case _ => {
          val amounts: Option[Amounts] = if (name.contains("dbCurrentYearMinus")) {
            Some(this.definedBenefits)
          } else if (name.contains("dcCurrentYearMinus")) {
            Some(this.definedContributions)
          } else {
            None
          }
          amounts.flatMap(retrieveValue(_, name))
        }
      }
    }

    def toInputAmounts(name1: String, name2: String): Option[InputAmounts] = {
      val a = InputAmounts(get(name1), get(name2), None, Some(false))
      if (a.isEmpty) None else Some(a)
    }

    List.range(settings.START_YEAR, settings.THIS_YEAR+1).flatMap {
      (year:Int) =>
      if (year == 2015) {
        if (triggerDate != None) {
          val c = triggerDatePeriod().get
          if (c.isPeriod1) {
            val preList = if (c.taxPeriodStart == PensionPeriod.PERIOD_1_2015_START) {
              List (Contribution(PensionPeriod.PERIOD_1_2015_START, PensionPeriod.PERIOD_1_2015_END,Some(InputAmounts(Some(0), get("ptDcAmount2015P1"), None, Some(true)))))
            } else {
              val date = c.taxPeriodStart.toCalendar
              date.add(java.util.Calendar.DAY_OF_MONTH, -1)
              val tp = PensionPeriod(date.get(java.util.Calendar.YEAR), date.get(java.util.Calendar.MONTH)+1, date.get(java.util.Calendar.DAY_OF_MONTH))
              List(Contribution(PensionPeriod.PERIOD_1_2015_START, tp, toInputAmounts("amount2015P1","dcAmount2015P1").map(_.copy(triggered=Some(false)))),
                   Contribution(c.taxPeriodStart, PensionPeriod.PERIOD_1_2015_END,Some(InputAmounts(Some(0), get("ptDcAmount2015P1"), None, Some(true)))))
            }
            preList ++ List(Contribution(PensionPeriod.PERIOD_2_2015_START, PensionPeriod.PERIOD_2_2015_END,toInputAmounts("amount2015P2","dcAmount2015P2").map(_.copy(triggered=Some(true)))))
          } else if (c.isPeriod2) {
            val preList = List(Contribution(PensionPeriod.PERIOD_1_2015_START, PensionPeriod.PERIOD_1_2015_END,toInputAmounts("amount2015P1","dcAmount2015P1").map(_.copy(triggered=Some(false)))))
            val postList = if (c.taxPeriodStart == PensionPeriod.PERIOD_2_2015_START) {
              List (Contribution(PensionPeriod.PERIOD_2_2015_START, PensionPeriod.PERIOD_2_2015_END,Some(InputAmounts(Some(0), get("ptDcAmount2015P2"), None, Some(true)))))
            } else {
              val date = c.taxPeriodStart.toCalendar
              date.add(java.util.Calendar.DAY_OF_MONTH, -1)
              val tp = PensionPeriod(date.get(java.util.Calendar.YEAR), date.get(java.util.Calendar.MONTH)+1, date.get(java.util.Calendar.DAY_OF_MONTH))
              List(Contribution(PensionPeriod.PERIOD_2_2015_START, tp, toInputAmounts("amount2015P2","dcAmount2015P2").map(_.copy(triggered=Some(false)))),
                   Contribution(c.taxPeriodStart, PensionPeriod.PERIOD_2_2015_END,Some(InputAmounts(Some(0), get("ptDcAmount2015P2"), None, Some(true)))))
            }
            preList ++ postList
          } else {
            List(Contribution(PensionPeriod.PERIOD_1_2015_START, PensionPeriod.PERIOD_1_2015_END,toInputAmounts("amount2015P1","dcAmount2015P1")),
                Contribution(PensionPeriod.PERIOD_2_2015_START, PensionPeriod.PERIOD_2_2015_END,toInputAmounts("amount2015P2","dcAmount2015P2")))
          }
        } else {
          List(Contribution(PensionPeriod.PERIOD_1_2015_START, PensionPeriod.PERIOD_1_2015_END,toInputAmounts("amount2015P1","dcAmount2015P1")),
               Contribution(PensionPeriod.PERIOD_2_2015_START, PensionPeriod.PERIOD_2_2015_END,toInputAmounts("amount2015P2","dcAmount2015P2")))
        }
      } else {
        val delta = settings.THIS_YEAR - year
        List(Contribution(year, toInputAmounts("dbCurrentYearMinus"+delta, "dcCurrentYearMinus"+delta)))
      }
    }
  }

  def toContributions():List[Contribution] = {
    toPageValues().filter(_.isEmpty() == false)
  }

  def hasDefinedContributions(): Boolean = !definedContributions.isEmpty || year2015.hasDefinedContributions
  def hasDefinedBenefits(): Boolean = !definedBenefits.isEmpty || year2015.hasDefinedBenefits
  def hasTriggerDate(): Boolean = triggerDate != None && triggerDate.get != ""

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
          moneyPurchase <- amounts.moneyPurchase
        } yield moneyPurchase).map((_,key))
    }
  }

  def period1(): Contribution => Boolean = { (c) => c.taxPeriodEnd.day == 8 && c.taxPeriodEnd.year == 2015 } 
  def period2(): Contribution => Boolean = { (c) => c.taxPeriodStart.day == 9 && c.taxPeriodStart.year == 2015 } 

  def to1516Period1DefinedBenefit: Option[(Long, String)] = toDefinedBenefit(period1())(KeystoreService.P1_DB_KEY)
  def to1516Period1DefinedContribution: Option[(Long, String)] = toDefinedContribution(period1())(KeystoreService.P1_DC_KEY)
  def to1516Period2DefinedBenefit: Option[(Long, String)] = toDefinedBenefit(period2())(KeystoreService.P2_DB_KEY)
  def to1516Period2DefinedContribution: Option[(Long, String)] = toDefinedContribution(period2())(KeystoreService.P2_DC_KEY)

  def toP1TriggerDefinedContribution: Option[(Long, String)] = year2015.postTriggerDcAmount2015P1.map((v:BigDecimal)=>((v*100).toLong, KeystoreService.P1_TRIGGER_DC_KEY))
  def toP2TriggerDefinedContribution: Option[(Long, String)] = year2015.postTriggerDcAmount2015P2.map((v:BigDecimal)=>((v*100).toLong, KeystoreService.P2_TRIGGER_DC_KEY))

  // Useful for all years except 2015/16 Tax Year
  def toDefinedBenefit(year: Int) : Option[(Long, String)] = toDefinedBenefit((_.taxPeriodStart.year == year))(KeystoreService.DB_PREFIX+year)
  def toDefinedContribution(year: Int) : Option[(Long, String)] = toDefinedContribution((_.taxPeriodStart.year == year))(KeystoreService.DC_PREFIX+year)

  def triggerDatePeriod(): Option[Contribution] = {
    triggerDate.map {
      (jodaDateStr) =>
      val parts = jodaDateStr.split("-")
      val taxPeriod = PensionPeriod(parts(0).toInt, parts(1).toInt, parts(2).toInt)
      Contribution(taxPeriod, taxPeriod, None)
    }
  }
}

object CalculatorFormFields

object Amounts {
  def toPence(value: BigDecimal): Long = (value * 100).longValue
}
