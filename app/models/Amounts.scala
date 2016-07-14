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

/* Case classes used by CalculatorFormFields */

/** 
  Represent a set of pence values for the 8 years prior to this year and current year as determined by paac.conf year flag.
  8 years are stored as originally the tool was to display a table of up to 8 year's results (entirely controlled by taxyearselection.scala.html)
  Values are in pence as BigDecimal (divide by 100D to retrieve pound and pence or 100L to retrieve pounds).
  We keep the models and backend calculator in pence values although pages (for the present time) have been converted to pounds.
 */
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

/** 
  Represent a set of pence values for 2015 period 1 and 2.
  Values are in pence as BigDecimal (divide by 100D to retrieve pound and pence or 100L to retrieve pounds).
  We keep the models and backend calculator in pence values although pages (for the present time) have been converted to pounds.
 */
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


object Amounts {
  def toPence(value: BigDecimal): Long = (value * 100).longValue
  def retrieveValue(amounts: Amounts, fieldName: String): Option[Long] = {
    val fieldValueMap: Map[String,Any]= amounts.getClass.getDeclaredFields.map(_.getName).zip(amounts.productIterator.toList).toMap
    fieldValueMap.get(fieldName).map(_.asInstanceOf[Option[BigDecimal]]).flatMap(_.map(Amounts.toPence))
  }
  /* 
    Form for now takes whole pounds as numbers instead of pence as bigdecimal so to represent pounds simply chop off the pence. 
    Can be deleted if revert to pence values again.
  */
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
  /* 
    Form for now takes whole pounds as numbers instead of pence as bigdecimal so to represent pounds simply chop off the pence. 
    Can be deleted if revert to pence values again.
  */
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
  /* 
    Form for now takes whole pounds as numbers instead of pence as bigdecimal so to represent pounds simply chop off the pence. 
    Can be deleted if revert to pence values again.
  */
  def applyFromInt(n1: Option[Int], 
                   n2: Option[Int], 
                   n3: Option[Int], 
                   n4: Option[Int], 
                   n5: Option[Int], 
                   n6: Option[Int]): Year2015Amounts = {
    Year2015Amounts(n1.map(BigDecimal(_)), n2.map(BigDecimal(_)), n3.map(BigDecimal(_)), n4.map(BigDecimal(_)), n5.map(BigDecimal(_)), n6.map(BigDecimal(_)))
  }
  /* 
    Form for now takes whole pounds as numbers instead of pence as bigdecimal so to represent pounds simply chop off the pence. 
    Can be deleted if revert to pence values again.
  */
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
