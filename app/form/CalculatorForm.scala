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

import models._
import play.api.data._
import play.api.data.Form
import play.api.data.Forms._
import service.KeystoreService
import service.KeystoreService._

object CalculatorForm extends models.ThisYear {
  settings: ThisYear =>

  type CalculatorFormType = CalculatorFormFields
  val CY0 = settings.THIS_YEAR
  val CY1 = settings.THIS_YEAR-1
  val CY2 = settings.THIS_YEAR-2
  val CY3 = settings.THIS_YEAR-3
  val CY4 = settings.THIS_YEAR-4
  val CY5 = settings.THIS_YEAR-5
  val CY6 = settings.THIS_YEAR-6
  val CY7 = settings.THIS_YEAR-7
  val CY8 = settings.THIS_YEAR-8

  def isPoundsAndPence(): Boolean = {
    settings.POUNDS_AND_PENCE
  }

  protected def formInPoundsAndPence(poundsAndPence: Mapping[Option[BigDecimal]], pounds: Mapping[Option[Int]]) = {
    Form(mapping("definedBenefits" -> mapping(s"amount_${CY0}"->poundsAndPence,
                                              s"amount_${CY1}"->poundsAndPence,
                                              s"amount_${CY2}"->poundsAndPence,
                                              s"amount_${CY3}"->poundsAndPence,
                                              s"amount_${CY4}"->poundsAndPence,
                                              s"amount_${CY5}"->poundsAndPence,
                                              s"amount_${CY6}"->poundsAndPence,
                                              s"amount_${CY7}"->poundsAndPence,
                                              s"amount_${CY8}"->poundsAndPence)(Amounts.apply)(Amounts.unapply),
              "definedContributions" -> mapping(s"amount_${CY0}"->poundsAndPence,
                                                s"amount_${CY1}"->poundsAndPence,
                                                s"amount_${CY2}"->poundsAndPence,
                                                s"amount_${CY3}"->poundsAndPence,
                                                s"amount_${CY4}"->poundsAndPence,
                                                s"amount_${CY5}"->poundsAndPence,
                                                s"amount_${CY6}"->poundsAndPence,
                                                s"amount_${CY7}"->poundsAndPence,
                                                s"amount_${CY8}"->poundsAndPence)(Amounts.apply)(Amounts.unapply),
              "adjustedIncome" -> mapping(s"amount_${CY0}"->poundsAndPence,
                                          s"amount_${CY1}"->poundsAndPence,
                                          s"amount_${CY2}"->poundsAndPence,
                                          s"amount_${CY3}"->poundsAndPence,
                                          s"amount_${CY4}"->poundsAndPence,
                                          s"amount_${CY5}"->poundsAndPence,
                                          s"amount_${CY6}"->poundsAndPence,
                                          s"amount_${CY7}"->poundsAndPence,
                                          s"amount_${CY8}"->poundsAndPence)(Amounts.apply)(Amounts.unapply),
              "year2015" -> mapping("definedBenefit_2015_p1"->poundsAndPence,
                                    "definedContribution_2015_p1"->poundsAndPence,
                                    "definedBenefit_2015_p2"->poundsAndPence,
                                    "definedContribution_2015_p2"->poundsAndPence,
                                    "postTriggerDcAmount2015P1"->poundsAndPence,
                                    "postTriggerDcAmount2015P2"->poundsAndPence)
                                    (Year2015Amounts.apply)
                                    (Year2015Amounts.unapply),
              "triggerAmount" -> pounds,
              "triggerDate" -> optional(text)
      )(CalculatorFormFields.apply)(CalculatorFormFields.unapply))
  }

  protected def formInPounds(pounds: Mapping[Option[Int]]) = {
    Form(mapping("definedBenefits" -> mapping(s"amount_${CY0}"->pounds,
                                              s"amount_${CY1}"->pounds,
                                              s"amount_${CY2}"->pounds,
                                              s"amount_${CY3}"->pounds,
                                              s"amount_${CY4}"->pounds,
                                              s"amount_${CY5}"->pounds,
                                              s"amount_${CY6}"->pounds,
                                              s"amount_${CY7}"->pounds,
                                              s"amount_${CY8}"->pounds)(Amounts.applyFromInt)(Amounts.unapplyToInt),
              "definedContributions" -> mapping(s"amount_${CY0}"->pounds,
                                                s"amount_${CY1}"->pounds,
                                                s"amount_${CY2}"->pounds,
                                                s"amount_${CY3}"->pounds,
                                                s"amount_${CY4}"->pounds,
                                                s"amount_${CY5}"->pounds,
                                                s"amount_${CY6}"->pounds,
                                                s"amount_${CY7}"->pounds,
                                                s"amount_${CY8}"->pounds)(Amounts.applyFromInt)(Amounts.unapplyToInt),
              "adjustedIncome" -> mapping(s"amount_${CY0}"->pounds,
                                          s"amount_${CY1}"->pounds,
                                          s"amount_${CY2}"->pounds,
                                          s"amount_${CY3}"->pounds,
                                          s"amount_${CY4}"->pounds,
                                          s"amount_${CY5}"->pounds,
                                          s"amount_${CY6}"->pounds,
                                          s"amount_${CY7}"->pounds,
                                          s"amount_${CY8}"->pounds)(Amounts.applyFromInt)(Amounts.unapplyToInt),
              "year2015" -> mapping("definedBenefit_2015_p1"->pounds,
                                    "definedContribution_2015_p1"->pounds,
                                    "definedBenefit_2015_p2"->pounds,
                                    "definedContribution_2015_p2"->pounds,
                                    "postTriggerDcAmount2015P1"->pounds,
                                    "postTriggerDcAmount2015P2"->pounds)
                                    (Year2015Amounts.applyFromInt)
                                    (Year2015Amounts.unapplyToInt),
              "triggerAmount" -> pounds,
              "triggerDate" -> optional(text)
      )(CalculatorFormFields.apply)(CalculatorFormFields.unapply))
  }

  def formDef(isValidating: Boolean = false): Form[CalculatorFormType] = {
    val poundsAndPence = if (isValidating)
              optional(bigDecimal(10,2)).verifying(" errorbounds", value=> if (value.isDefined)
                                                                             value.get.longValue >= 0 && value.get.longValue <= 5000000
                                                                           else
                                                                             true)
        else
          optional(bigDecimal(10,2))
    val pounds = if (isValidating)
                   optional(number(min=0,max=5000000))
                 else
                   optional(number)
    if (isPoundsAndPence) {
      formInPoundsAndPence(poundsAndPence, pounds)
    } else {
      formInPounds(pounds)
    }
  }

  val form = formDef(true)
  val nonValidatingForm = formDef(false)

  /** Utility method to aid marshalling keystore values into form either with or without validation. */
  def bind(data: Map[String, String], nonValidatingForm: Boolean = false): Form[CalculatorFormType] = {
    val year2015 = List(("year2015.definedBenefit_2015_p1", data.getOrElse("amount2015P1", data.getOrElse(P1_DB_KEY,""))),
                        ("year2015.definedContribution_2015_p1", data.getOrElse("dcAmount2015P1", data.getOrElse(P1_DC_KEY,""))),
                        ("year2015.definedBenefit_2015_p2", data.getOrElse("amount2015P2", data.getOrElse(P2_DB_KEY,""))),
                        ("year2015.definedContribution_2015_p2", data.getOrElse("dcAmount2015P2", data.getOrElse(P2_DC_KEY,""))),
                        ("year2015.postTriggerDcAmount2015P1", data.getOrElse(P1_TRIGGER_DC_KEY,"")),
                        ("year2015.postTriggerDcAmount2015P2", data.getOrElse(P2_TRIGGER_DC_KEY,"")))
    val amounts = List.range(settings.THIS_YEAR-8, settings.THIS_YEAR + 1).flatMap {
      (year)=>
      if (year != 2015)
        List((s"definedBenefits.amount_${year}", data.getOrElse("definedBenefit_" + year, "")),
             (s"definedContributions.amount_${year}", data.getOrElse("definedContribution_" + year, "")),
             (s"adjustedIncome.amount_${year}", data.getOrElse("adjustedIncome_" + year, "")))
      else
        year2015
    }
    val yearAmounts = amounts.map((pair)=>(pair._1,toAmount(pair._2, isPoundsAndPence)))
    val triggerDateValue = data.get(TRIGGER_DATE_KEY).map((date)=>("triggerDate", date)).getOrElse(("triggerDate", ""))
    val triggerAmountValue = data.get(TRIGGER_DC_KEY).map((amount)=>("triggerAmount", toAmount(amount,isPoundsAndPence))).getOrElse(("triggerAmount", ""))
    val values: List[(String,String)] = yearAmounts ++ List(triggerDateValue, triggerAmountValue)
    if (nonValidatingForm) CalculatorForm.nonValidatingForm.bind(Map(values: _*)) else CalculatorForm.form.bind(Map(values: _*))
  }

  def toAmount(poundsAndPence: String, isPoundsAndPence: Boolean): String = {
    if (poundsAndPence.isEmpty) {
      poundsAndPence
    } else if(isPoundsAndPence) {
      f"${(poundsAndPence.toInt / 100.00)}%2.2f".trim
    } else {
      f"${(poundsAndPence.toInt / 100.00)}%2.0f".trim
    }
  }
}
