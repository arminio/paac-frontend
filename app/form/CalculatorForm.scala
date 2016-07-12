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
import play.api.data.Form
import play.api.data.Forms._
import service.KeystoreService

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

  def formDef(isValidating: Boolean = false): Form[CalculatorFormType] = {
    val t = if (isValidating) 
          //optional(bigDecimal(10,2)).verifying(" errorbounds", value=> if (value.isDefined) value.get.longValue >= 0 && value.get.longValue <= 5000000 else true)
          optional(number(min=0,max=5000000)) 
        else 
          //optional(bigDecimal(10,2))
          optional(number)

    // need to figure out the types here so that form can be truly dynamic at present requires recompilation for apply and unapply 
    //val amountsApply = if (isPence) Amounts.apply _ else Amounts.applyFromInt _
    //val amountsUnapply = if (isPence) Amounts.unapply _ else Amounts.unapplyFromInt _
    Form(mapping("definedBenefits" -> mapping(s"amount_${CY0}"->t,
                                              s"amount_${CY1}"->t,
                                              s"amount_${CY2}"->t,
                                              s"amount_${CY3}"->t,
                                              s"amount_${CY4}"->t,
                                              s"amount_${CY5}"->t,
                                              s"amount_${CY6}"->t,
                                              s"amount_${CY7}"->t,
                                              s"amount_${CY8}"->t)(Amounts.applyFromInt)(Amounts.unapplyToInt),
              "definedContributions" -> mapping(s"amount_${CY0}"->t,
                                                s"amount_${CY1}"->t,
                                                s"amount_${CY2}"->t,
                                                s"amount_${CY3}"->t,
                                                s"amount_${CY4}"->t,
                                                s"amount_${CY5}"->t,
                                                s"amount_${CY6}"->t,
                                                s"amount_${CY7}"->t,
                                                s"amount_${CY8}"->t)(Amounts.applyFromInt)(Amounts.unapplyToInt),
              "adjustedIncome" -> mapping(s"amount_${CY0}"->t,
                                          s"amount_${CY1}"->t,
                                          s"amount_${CY2}"->t,
                                          s"amount_${CY3}"->t,
                                          s"amount_${CY4}"->t,
                                          s"amount_${CY5}"->t,
                                          s"amount_${CY6}"->t,
                                          s"amount_${CY7}"->t,
                                          s"amount_${CY8}"->t)(Amounts.applyFromInt)(Amounts.unapplyToInt),
              "year2015" -> mapping("definedBenefit_2015_p1"->t,
                                    "definedContribution_2015_p1"->t,
                                    "definedBenefit_2015_p2"->t,
                                    "definedContribution_2015_p2"->t,
                                    "postTriggerDcAmount2015P1"->t,
                                    "postTriggerDcAmount2015P2"->t)
                                    (Year2015Amounts.applyFromInt)
                                    (Year2015Amounts.unapplyToInt),
              "triggerAmount" -> t,
              "triggerDate" -> optional(text)
      )(CalculatorFormFields.apply)(CalculatorFormFields.unapply)
    )
  }

  val form = formDef(true)
  val nonValidatingForm = formDef(false)

  /** Utility method to aid marshalling keystore values into form either with or without validation. */
  def bind(data: Map[String, String], nonValidatingForm: Boolean = false): Form[CalculatorFormType] = {
    val year2015 = List(("year2015.definedBenefit_2015_p1", data.getOrElse("amount2015P1", data.getOrElse(KeystoreService.P1_DB_KEY,""))),
                        ("year2015.definedContribution_2015_p1", data.getOrElse("dcAmount2015P1", data.getOrElse(KeystoreService.P1_DC_KEY,""))),
                        ("year2015.definedBenefit_2015_p2", data.getOrElse("amount2015P2", data.getOrElse(KeystoreService.P2_DB_KEY,""))),
                        ("year2015.definedContribution_2015_p2", data.getOrElse("dcAmount2015P2", data.getOrElse(KeystoreService.P2_DC_KEY,""))),
                        ("year2015.postTriggerDcAmount2015P1", data.getOrElse(KeystoreService.P1_TRIGGER_DC_KEY,"")),
                        ("year2015.postTriggerDcAmount2015P2", data.getOrElse(KeystoreService.P2_TRIGGER_DC_KEY,"")))
    val yearAmounts = List.range(settings.THIS_YEAR-8, settings.THIS_YEAR + 1).flatMap {
      (year)=>
      if (year != 2015){
        List((s"definedBenefits.amount_${year}", data.getOrElse("definedBenefit_" + year, "")),
             (s"definedContributions.amount_${year}", data.getOrElse("definedContribution_" + year, "")))
      } else {
        year2015
      }
    }
    val maybeDate: Option[String] = data.get(KeystoreService.TRIGGER_DATE_KEY)
    val values: List[(String,String)] = maybeDate.map((date)=>yearAmounts ++ List(("triggerDate", date))).getOrElse(yearAmounts)
    if (nonValidatingForm) CalculatorForm.nonValidatingForm.bind(Map(values: _*)) else CalculatorForm.form.bind(Map(values: _*))
  }
}
