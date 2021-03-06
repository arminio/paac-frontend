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

import service.KeystoreService._
import models._

trait Contributions {
  def apply(data: Map[String,String]): List[Contribution] = toContributionsWithTrigger(data)

  protected def toContributionsWithoutIncome(data: Map[String,String]): List[Contribution] = {
    data.filterKeys((key)=>keys.exists(key.startsWith(_))).map {
      (entry) =>
      entry match {
        case (P1_DB_KEY, v) => Contribution(true, Some(InputAmounts(toLong(v), None)))
        case (P1_DC_KEY, v) => Contribution(true, Some(InputAmounts(None, toLong(v))))
        case (P2_DB_KEY, v) => Contribution(false, Some(InputAmounts(toLong(v), None)))
        case (P2_DC_KEY, v) => Contribution(false, Some(InputAmounts(None, toLong(v))))
        case (k, v) if k.startsWith(DB_PREFIX) => Contribution((k.replace(DB_PREFIX,"")).toInt, Some(InputAmounts(toLong(v))))
        case (k, v) if k.startsWith(DC_PREFIX) => Contribution((k.replace(DC_PREFIX,"")).toInt, Some(InputAmounts(moneyPurchase=toLong(v))))
        case _ => Contribution(0,0)
      }
    }.toList.groupBy(_.taxPeriodStart).values.map((l)=>if(l.size>=2) l(0)+l(1) else l(0)).toList
  }

  protected def toContributionsWithIncome(data: Map[String,String]): List[Contribution] = {
    toContributionsWithoutIncome(data).map {
      (contribution) =>
      val year = contribution.taxPeriodStart.taxYear
      val maybeIncome = data.get(s"${AI_PREFIX}${year}").flatMap(toLong(_))
      contribution.copy(amounts=contribution.amounts.map(_.copy(income=maybeIncome)))
    }
  }

  protected def toContributionsWithTrigger(data: Map[String,String]): List[Contribution] = {
    val contributions = toContributionsWithIncome(data).sortWith(Contribution.sortByYearAndPeriod)
    data.get(TRIGGER_DATE_KEY).map {
      (date) =>
      val triggerDate: PensionPeriod = date
      val index = contributions.indexWhere(isPreTrigger(triggerDate))
      if (index > -1) {
        val (before, after) = contributions.splitAt(index)
        val newAfter = after.map((c)=>c.copy(amounts=c.amounts.map((a)=>a.copy(triggered=Some(!isPreTrigger(triggerDate)(c))))))
        val maybeTriggerAmount = triggerDate match {
          case PensionPeriod(_,_,_) if triggerDate.isPeriod1 => data.get(P1_TRIGGER_DC_KEY).flatMap(toLong(_))
          case PensionPeriod(_,_,_) if triggerDate.isPeriod2 => data.get(P2_TRIGGER_DC_KEY).flatMap(toLong(_))
          case PensionPeriod(_,_,_) => data.get(TRIGGER_DC_KEY).flatMap(toLong(_))
        }
        maybeTriggerAmount.map {
          (triggerAmount) =>
          val preTriggerContribution = newAfter(newAfter.indexWhere(isPreTrigger(triggerDate)))
          val totalMoneyPurchase = preTriggerContribution.moneyPurchase
          val preTriggerMoneyPurchase = totalMoneyPurchase - triggerAmount
          val newPreTriggerContribution = Contribution(preTriggerContribution.taxPeriodStart, triggerDate, Some(InputAmounts(preTriggerContribution.amounts.flatMap(_.definedBenefit), Some(preTriggerMoneyPurchase), preTriggerContribution.amounts.flatMap(_.income), Some(false))))
          val triggerContribution = Contribution(triggerDate, preTriggerContribution.taxPeriodEnd, Some(InputAmounts(definedBenefit=Some(0L),moneyPurchase=maybeTriggerAmount,triggered=Some(true))))

          (before ++ newAfter.drop(1) ++ List(newPreTriggerContribution,triggerContribution)).sortWith(Contribution.sortByYearAndPeriod)
        }.getOrElse(contributions)
      } else {
        contributions
      }
    }.getOrElse(contributions)
  }

  protected val isPreTrigger: PensionPeriod => Contribution => Boolean = (triggerDate) => (c)=> triggerDate >= c.taxPeriodStart && triggerDate <= c.taxPeriodEnd
  protected val keys = List(P1_DB_KEY, P1_DC_KEY, P2_DB_KEY, P2_DC_KEY, DB_PREFIX, DC_PREFIX)
  protected def toLong(longString: String): Option[Long] = if (longString.trim.isEmpty || !longString.matches("\\d+")) None else Some(longString.toLong)
}

object Contributions extends Contributions
