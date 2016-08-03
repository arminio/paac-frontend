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

trait ThisYear {
  def THIS_YEAR = config.PaacConfiguration.year()
  def POUNDS_AND_PENCE = config.PaacConfiguration.supportPence()
}

//noinspection ScalaStyle
trait CalculatorFields extends ThisYear {
  settings: ThisYear =>

  def definedBenefits: Amounts
  def definedContributions: Amounts
  def adjustedIncome: Amounts
  def year2015: Year2015Amounts
  def triggerAmount: Option[Int]
  def triggerDate: Option[String]

  val START_YEAR = settings.THIS_YEAR-8

  def toPageValues():List[Contribution] = {
    // if db == NONE then will be group 2 calculation otherwise db = 0 and will be group 3
    def toTriggerContributions(maybeDB: Option[Long],
                               maybeTriggerDC: Option[Long],
                               c: Contribution,
                               contribution: Contribution): List[Contribution] =
      List(Contribution(c.taxPeriodStart,
                        contribution.taxPeriodEnd,
                        Some(InputAmounts(maybeDB.map((_)=>0L),
                        maybeTriggerDC.orElse(Some(0)),
                        None,
                        Some(true)))),
          contribution.copy(taxPeriodEnd=c.taxPeriodStart,amounts=contribution.amounts.map(_.copy(triggered=Some(false)))))

    def toPeriodContribution(contribution: Contribution,
                             p1: Contribution => List[Contribution],
                             p2: Contribution => List[Contribution]): List[Contribution] =
    triggerDatePeriod map ((c)=> if (c.isPeriod1) p1(c) else if (c.isPeriod2) p2(c) else List(contribution)) getOrElse List(contribution)

    List.range(settings.START_YEAR, settings.THIS_YEAR + 1).flatMap {
      (year:Int) =>
      year match {
        case y if (y == 2015) => {
          val p1DB = year2015.amount2015P1.map(Amounts.toPence)
          val p1DC = year2015.dcAmount2015P1.map(Amounts.toPence)
          val p1Contribution = Contribution(true, Some(InputAmounts(p1DB, p1DC)))
          val p1Contributions = toPeriodContribution(p1Contribution, (c)=>toTriggerContributions(p1DB, year2015.postTriggerDcAmount2015P1.map(Amounts.toPence), c, p1Contribution), (c)=>List(p1Contribution))
          val p2DB = year2015.amount2015P2.map(Amounts.toPence)
          val p2DC = year2015.dcAmount2015P2.map(Amounts.toPence)
          val p2Contribution = Contribution(false, Some(InputAmounts(p2DB, p2DC)))
          val p2Contributions = toPeriodContribution(p2Contribution, (c)=>List(p2Contribution.copy(amounts=p2Contribution.amounts.map(_.copy(triggered=Some(true))))), (c)=>toTriggerContributions(p2DB, year2015.postTriggerDcAmount2015P2.map(Amounts.toPence), c, p2Contribution))
          p2Contributions ++ p1Contributions
        }
        case _ => {
          val delta = settings.THIS_YEAR - year
          val pp = PensionPeriod.toPensionPeriod(triggerDate.getOrElse("0000-01-1"))
          val isTriggered = triggerDate.isDefined && pp.taxYear == year
          if (isTriggered) {
            val contribution = Contribution(year, toInputAmounts(DB_PREFIX + delta, DC_PREFIX + delta, AI_PREFIX + delta))
            // $COVERAGE-OFF$
            // This is probably never used and should be investigated if this can be removed.
            val moneyPurchase = contribution.amounts.flatMap(_.moneyPurchase)
            // $COVERAGE-ON$
            toTriggerContributions(moneyPurchase, triggerAmount.map(_.toLong*100L), triggerDatePeriod.get, contribution)
          } else
            List(Contribution(year, toInputAmounts(DB_PREFIX + delta, DC_PREFIX + delta, AI_PREFIX + delta)))
        }
      }
    }
  }
  // Utility methods for views and controllers
  def toContributions():List[Contribution] = toPageValues().filter(_.isEmpty() == false)
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
  def triggerDatePeriod(): Option[Contribution] = triggerDate.map {
      (jodaDateStr) =>
      val parts = jodaDateStr.split("-")
      val taxPeriod = PensionPeriod(parts(0).toInt, parts(1).toInt, parts(2).toInt)
      Contribution(taxPeriod, taxPeriod, None)
    }

  protected def toDefinedBenefit(first: Contribution => Boolean)(key: String) : Option[(Long, String)] = toTuple(first, (a)=>a.definedBenefit)(key)
  protected def toAdjustedIncome(first: Contribution => Boolean)(key: String) : Option[(Long, String)] = toTuple(first, (a)=>a.income)(key)
  protected def toDefinedContribution(first: Contribution => Boolean)(key: String) : Option[(Long, String)] = toTuple(first, (a)=>a.moneyPurchase)(key)
  protected def period1(): Contribution => Boolean = { (c) => c.isPeriod1 }
  protected def period2(): Contribution => Boolean = { (c) => c.isPeriod2 }
  protected def toTuple(first: Contribution => Boolean, value: InputAmounts => Option[Long])(key: String) : Option[(Long, String)] = toContributions.find(first).flatMap {
      (c)=>
        (for {
          amounts <- c.amounts
          v <- value(amounts)
        } yield v).map((_,key))
    }
  protected def toInputAmounts(name1: String, name2: String, name3: String = ""): Option[InputAmounts] = {
    val a = InputAmounts(get(name1), get(name2), get(name3), None)
    if (a.isEmpty) None else Some(a)
  }
  protected val DB_PREFIX = "dbCurrentYearMinus"
  protected val DC_PREFIX = "dcCurrentYearMinus"
  protected val AI_PREFIX = "aiCurrentYearMinus"

  protected def get(name:String): Option[Long] = (if (name.contains(DB_PREFIX)) {
          Some(definedBenefits)
        } else if (name.contains(DC_PREFIX)) {
          Some(definedContributions)
        } else if (name.contains(AI_PREFIX)) {
          Some(adjustedIncome)
        } else {
          None
        }).flatMap(Amounts.retrieveValue(_, name.replaceAll("^(dbC|dcC|aiC)", "c")))
}

case class CalculatorFormFields(definedBenefits: Amounts,
                                definedContributions: Amounts,
                                adjustedIncome: Amounts,
                                year2015: Year2015Amounts,
                                triggerAmount: Option[Int],
                                triggerDate: Option[String]) extends CalculatorFields

object CalculatorFormFields