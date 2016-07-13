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

import service.KeystoreService

import scala.math._
import PensionPeriod._

case class Amounts(currentYearMinus0:Option[BigDecimal]=None,
                   currentYearMinus1:Option[BigDecimal]=None,
                   currentYearMinus2:Option[BigDecimal]=None,
                   currentYearMinus3:Option[BigDecimal]=None,
                   currentYearMinus4:Option[BigDecimal]=None,
                   currentYearMinus5:Option[BigDecimal]=None,
                   currentYearMinus6:Option[BigDecimal]=None,
                   currentYearMinus7:Option[BigDecimal]=None,
                   currentYearMinus8:Option[BigDecimal]=None) {
  def isEmpty(): Boolean = !currentYearMinus0.isDefined &&
                           !currentYearMinus1.isDefined &&
                           !currentYearMinus2.isDefined &&
                           !currentYearMinus3.isDefined &&
                           !currentYearMinus4.isDefined &&
                           !currentYearMinus5.isDefined &&
                           !currentYearMinus6.isDefined &&
                           !currentYearMinus7.isDefined &&
                           !currentYearMinus8.isDefined
}

case class Year2015Amounts(amount2015P1:Option[BigDecimal]=None,
                           dcAmount2015P1:Option[BigDecimal]=None,
                           amount2015P2:Option[BigDecimal]=None,
                           dcAmount2015P2:Option[BigDecimal]=None,
                           postTriggerDcAmount2015P1:Option[BigDecimal]=None,
                           postTriggerDcAmount2015P2:Option[BigDecimal]=None) {
  def isEmpty(): Boolean = !amount2015P1.isDefined &&
                           !dcAmount2015P1.isDefined &&
                           !amount2015P2.isDefined &&
                           !dcAmount2015P2.isDefined &&
                           !postTriggerDcAmount2015P1.isDefined &&
                           !postTriggerDcAmount2015P2.isDefined
  def hasDefinedContributions(): Boolean = dcAmount2015P1.isDefined || dcAmount2015P2.isDefined || postTriggerDcAmount2015P1.isDefined || postTriggerDcAmount2015P2.isDefined
  def hasDefinedBenefits(): Boolean = amount2015P1.isDefined || amount2015P2.isDefined
}

trait ThisYear {
  def THIS_YEAR = config.PaacConfiguration.year()
}

//noinspection ScalaStyle
case class CalculatorFormFields(definedBenefits: Amounts,
                                definedContributions: Amounts,
                                adjustedIncome: Amounts,
                                year2015: Year2015Amounts,
                                triggerAmount: Option[Int],
                                triggerDate: Option[String]) extends ThisYear {
  settings: ThisYear =>

  val START_YEAR = settings.THIS_YEAR-8
  val P1_DB_AMOUNT = "amount2015P1"
  val P1_DC_AMOUNT = "dcAmount2015P1"
  val P2_DB_AMOUNT = "amount2015P2"
  val P2_DC_AMOUNT = "dcAmount2015P2"
  val P1_TRIGGER_AMOUNT = "ptDcAmount2015P1"
  val P2_TRIGGER_AMOUNT = "ptDcAmount2015P2"
  val DB_PREFIX = "dbCurrentYearMinus"
  val DC_PREFIX = "dcCurrentYearMinus"
  val AI_PREFIX = "aiCurrentYearMinus"

  def toPageValues():List[Contribution] = {
    def retrieveValue(amounts: Amounts, name: String): Option[Long] = {
      val fieldValueMap: Map[String,Any]= amounts.getClass.getDeclaredFields.map(_.getName).zip(amounts.productIterator.toList).toMap
      val fieldName = "c" + name.drop(3)
      fieldValueMap.get(fieldName).map(_.asInstanceOf[Option[BigDecimal]]).flatMap(_.map(Amounts.toPence))
    }

    def get(name:String): Option[Long] = {
      name match {
        case P1_DB_AMOUNT => year2015.amount2015P1.map(Amounts.toPence)
        case P1_DC_AMOUNT => year2015.dcAmount2015P1.map(Amounts.toPence)
        case P2_DB_AMOUNT => year2015.amount2015P2.map(Amounts.toPence)
        case P2_DC_AMOUNT => year2015.dcAmount2015P2.map(Amounts.toPence)
        case P1_TRIGGER_AMOUNT => year2015.postTriggerDcAmount2015P1.map(Amounts.toPence)
        case P2_TRIGGER_AMOUNT => year2015.postTriggerDcAmount2015P2.map(Amounts.toPence)
        case _ => {
          val amounts: Option[Amounts] = if (name.contains(DB_PREFIX)) {
            Some(this.definedBenefits)
          } else if (name.contains(DC_PREFIX)) {
            Some(this.definedContributions)
          } else if (name.contains(AI_PREFIX)) {
            Some(this.adjustedIncome)
          } else {
            None
          }
          amounts.flatMap(retrieveValue(_, name))
        }
      }
    }

    def toInputAmounts(name1: String, name2: String, name3: String = "", isTriggered: Boolean = false): Option[InputAmounts] = {
      val a = InputAmounts(get(name1), get(name2), get(name3), if (isTriggered) Some(true) else None)
      if (a.isEmpty) None else Some(a)
    }

    def p1Contribution(): List[Contribution] = {
      val contribution = Contribution(PERIOD_1_2015_START, PERIOD_1_2015_END, toInputAmounts(P1_DB_AMOUNT,P1_DC_AMOUNT))
      if (triggerDate.isDefined) {
        val c = triggerDatePeriod().get
        if (c.isPeriod1) {
          val db = get(P1_DB_AMOUNT).map((_)=>0L) // if db == NONE then will be group 2 calculation otherwise will be group 3
          List(Contribution(c.taxPeriodStart, PERIOD_1_2015_END, Some(InputAmounts(db, get(P1_TRIGGER_AMOUNT), None, Some(true)))),contribution.copy(taxPeriodEnd=c.taxPeriodStart))
        } else if (c.isPeriod2) {
          List(contribution)
        } else {
          List(contribution)
        }
      } else {
        List(contribution)
      }
    }

    def p2Contribution(): List[Contribution] = {
      val contribution = Contribution(PERIOD_2_2015_START, PERIOD_2_2015_END, toInputAmounts(P2_DB_AMOUNT,P2_DC_AMOUNT))
      if (triggerDate.isDefined) {
        val c = triggerDatePeriod().get
        if (c.isPeriod1) {
          List(contribution.copy(amounts=contribution.amounts.map(_.copy(triggered=Some(true)))))
        } else if (c.isPeriod2) {
          val db = get(P2_DB_AMOUNT).map((_)=>0L) // if db == NONE then will be group 2 calculation otherwise will be group 3
          List(Contribution(c.taxPeriodStart, PERIOD_2_2015_END, Some(InputAmounts(db, get(P2_TRIGGER_AMOUNT), None, Some(true)))),contribution.copy(taxPeriodEnd=c.taxPeriodStart))
        } else {
          List(contribution)
        }
      } else {
        List(contribution)
      }
    }

    List.range(settings.START_YEAR, settings.THIS_YEAR + 1).flatMap {
      (year:Int) =>
      if (year == 2015)
        p2Contribution ++ p1Contribution
      else {
        val delta = settings.THIS_YEAR - year
        if (triggerAmount.isDefined && triggerDate.isDefined) {
          val pp = PensionPeriod.toPensionPeriod(triggerDate.get)
          if (pp.taxYear >= year) {
            if (pp.taxYear == year) {
              val start = Contribution(year, 0).taxPeriodStart
              val end = Contribution(year, 0).taxPeriodEnd
              List(Contribution(start, pp, toInputAmounts(DB_PREFIX + delta, DC_PREFIX + delta, AI_PREFIX + delta)),
                   Contribution(pp, end, Some(InputAmounts(None, triggerAmount.map(_.toLong*100L), None, Some(true)))))
            } else
              List(Contribution(year, toInputAmounts(DB_PREFIX + delta, DC_PREFIX + delta, AI_PREFIX + delta, true)))
          } else
            List(Contribution(year, toInputAmounts(DB_PREFIX + delta, DC_PREFIX + delta, AI_PREFIX + delta)))
        } else
          List(Contribution(year, toInputAmounts(DB_PREFIX + delta, DC_PREFIX + delta, AI_PREFIX + delta)))
      }
    }
  }

  def toContributions():List[Contribution] = {
    toPageValues().filter(_.isEmpty() == false)
  }

  def hasDefinedContributions(): Boolean = !definedContributions.isEmpty || year2015.hasDefinedContributions
  def hasDefinedBenefits(): Boolean = !definedBenefits.isEmpty || year2015.hasDefinedBenefits
  def hasTriggerDate(): Boolean = triggerDate.isDefined && !triggerDate.get.isEmpty

  def toDefinedBenefit(first: Contribution => Boolean)(key: String) : Option[(Long, String)] = {
    toContributions.find(first).flatMap {
      (c)=>
        (for {
          amounts <- c.amounts
          definedBenefit <- amounts.definedBenefit
        } yield definedBenefit).map((_,key))
    }
  }

  def toAdjustedIncome(first: Contribution => Boolean)(key: String) : Option[(Long, String)] = {
    toContributions.find(first).flatMap {
      (c)=>
        (for {
          amounts <- c.amounts
          adjustedIncome <- amounts.income
        } yield adjustedIncome).map((_,key))
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

  def period1(): Contribution => Boolean = { (c) => c.isPeriod1 }
  def period2(): Contribution => Boolean = { (c) => c.isPeriod2 }

  def to1516Period1DefinedBenefit: Option[(Long, String)] = toDefinedBenefit(period1())(KeystoreService.P1_DB_KEY)
  def to1516Period1DefinedContribution: Option[(Long, String)] = toDefinedContribution(period1())(KeystoreService.P1_DC_KEY)
  def to1516Period2DefinedBenefit: Option[(Long, String)] = toDefinedBenefit(period2())(KeystoreService.P2_DB_KEY)
  def to1516Period2DefinedContribution: Option[(Long, String)] = toDefinedContribution(period2())(KeystoreService.P2_DC_KEY)

  def toP1TriggerDefinedContribution: Option[(Long, String)] = year2015.postTriggerDcAmount2015P1.map((v:BigDecimal)=>((v*100).toLong,
    KeystoreService.P1_TRIGGER_DC_KEY))
  def toP2TriggerDefinedContribution: Option[(Long, String)] = year2015.postTriggerDcAmount2015P2.map((v:BigDecimal)=>((v*100).toLong,
    KeystoreService.P2_TRIGGER_DC_KEY))

  // Useful for all years except 2015/16 Tax Year
  def toDefinedBenefit(year: Int) : Option[(Long, String)] = toDefinedBenefit((_.taxPeriodStart.year == year))(KeystoreService.DB_PREFIX + year)
  def toDefinedContribution(year: Int) : Option[(Long, String)] = toDefinedContribution((_.taxPeriodStart.year == year))(KeystoreService.DC_PREFIX + year)

  def toAdjustedIncome(year: Int) : Option[(Long, String)] = toAdjustedIncome((_.taxPeriodStart.year == year))(KeystoreService.AI_PREFIX + year)


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
  /* Form for now takes whole pounds as numbers instead of bigdecimal to represent pounds and pence. Simply chop off the pence.*/
  def applyFromInt(cy0: Option[Int], 
                   cy1: Option[Int], 
                   cy2: Option[Int], 
                   cy3: Option[Int], 
                   cy4: Option[Int], 
                   cy5: Option[Int], 
                   cy6: Option[Int], 
                   cy7: Option[Int], 
                   cy8: Option[Int]): Amounts = {
    Amounts(cy0.map(BigDecimal(_)), cy1.map(BigDecimal(_)), cy2.map(BigDecimal(_)), cy3.map(BigDecimal(_)), cy4.map(BigDecimal(_)), cy5.map(BigDecimal(_)), cy6.map(BigDecimal(_)), cy7.map(BigDecimal(_)), cy8.map(BigDecimal(_)))
  }
  def unapplyToInt(amounts: Amounts): Option[(Option[Int], Option[Int], Option[Int], Option[Int], Option[Int], Option[Int], Option[Int], Option[Int], Option[Int])] = {
    Amounts.unapply(amounts).map {
      (tuple)=>
      (tuple._1.map(_.intValue),
       tuple._2.map(_.intValue),
       tuple._3.map(_.intValue),
       tuple._4.map(_.intValue),
       tuple._5.map(_.intValue),
       tuple._6.map(_.intValue),
       tuple._7.map(_.intValue),
       tuple._8.map(_.intValue),
       tuple._9.map(_.intValue))
    }
  }
}

object Year2015Amounts {
  /* Form for now takes whole pounds as numbers instead of bigdecimal to represent pounds and pence. Simply chop off the pence.*/
  def applyFromInt(n1: Option[Int], 
                   n2: Option[Int], 
                   n3: Option[Int], 
                   n4: Option[Int], 
                   n5: Option[Int], 
                   n6: Option[Int]): Year2015Amounts = {
    Year2015Amounts(n1.map(BigDecimal(_)), n2.map(BigDecimal(_)), n3.map(BigDecimal(_)), n4.map(BigDecimal(_)), n5.map(BigDecimal(_)), n6.map(BigDecimal(_)))
  }
  def unapplyToInt(amounts: Year2015Amounts): Option[(Option[Int], Option[Int], Option[Int], Option[Int], Option[Int], Option[Int])] = {
    Year2015Amounts.unapply(amounts).map {
      (tuple)=>
      (tuple._1.map(_.intValue),
       tuple._2.map(_.intValue),
       tuple._3.map(_.intValue),
       tuple._4.map(_.intValue),
       tuple._5.map(_.intValue),
       tuple._6.map(_.intValue))
    }
  }
}