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
  val dbValidator = optional(bigDecimal(10,2)).verifying("db.error.bounds", value=> if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true)
  val dcValidator = optional(bigDecimal(10,2)).verifying("dc.error.bounds", value=> if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true)
  val validator = optional(bigDecimal(10,2)).verifying("error.bounds", value=> if (value.isDefined) value.get.longValue >= 0 && value.get.longValue < 100000000 else true)

  val form: Form[CalculatorFormType] = Form(
    mapping("definedBenefits" -> mapping(s"amount_${CY0}"->dbValidator,
                                         s"amount_${CY1}"->dbValidator,
                                         s"amount_${CY2}"->dbValidator,
                                         s"amount_${CY3}"->dbValidator,
                                         s"amount_${CY4}"->dbValidator,
                                         s"amount_${CY5}"->dbValidator,
                                         s"amount_${CY6}"->dbValidator,
                                         s"amount_${CY7}"->dbValidator,
                                         s"amount_${CY8}"->dbValidator)(Amounts.apply)(Amounts.unapply),
            "definedContributions" -> mapping(s"amount_${CY0}"->dcValidator,
                                              s"amount_${CY1}"->dcValidator,
                                              s"amount_${CY2}"->dcValidator,
                                              s"amount_${CY3}"->dcValidator,
                                              s"amount_${CY4}"->dcValidator,
                                              s"amount_${CY5}"->dcValidator,
                                              s"amount_${CY6}"->dcValidator,
                                              s"amount_${CY7}"->dcValidator,
                                              s"amount_${CY8}"->dcValidator)(Amounts.apply)(Amounts.unapply),
            "year2015" -> mapping("definedBenefit_2015_p1"->dbValidator,
                                  "definedContribution_2015_p1"->dcValidator,
                                  "definedBenefit_2015_p2"->dbValidator,
                                  "definedContribution_2015_p2"->dcValidator,
                                  "postTriggerDcAmount2015P1"->validator,
                                  "postTriggerDcAmount2015P2"->validator)(Year2015Amounts.apply)(Year2015Amounts.unapply),
            "triggerDate" -> optional(text)
    )(CalculatorFormFields.apply)(CalculatorFormFields.unapply)
  )

  val nonValidatingForm: Form[CalculatorFormType] = Form(
    mapping("definedBenefits" -> mapping(s"amount_${CY0}"->optional(bigDecimal(10,2)),
                                         s"amount_${CY1}"->optional(bigDecimal(10,2)),
                                         s"amount_${CY2}"->optional(bigDecimal(10,2)),
                                         s"amount_${CY3}"->optional(bigDecimal(10,2)),
                                         s"amount_${CY4}"->optional(bigDecimal(10,2)),
                                         s"amount_${CY5}"->optional(bigDecimal(10,2)),
                                         s"amount_${CY6}"->optional(bigDecimal(10,2)),
                                         s"amount_${CY7}"->optional(bigDecimal(10,2)),
                                         s"amount_${CY8}"->optional(bigDecimal(10,2)))(Amounts.apply)(Amounts.unapply),
            "definedContributions" -> mapping(s"amount_${CY0}"->optional(bigDecimal(10,2)),
                                              s"amount_${CY1}"->optional(bigDecimal(10,2)),
                                              s"amount_${CY2}"->optional(bigDecimal(10,2)),
                                              s"amount_${CY3}"->optional(bigDecimal(10,2)),
                                              s"amount_${CY4}"->optional(bigDecimal(10,2)),
                                              s"amount_${CY5}"->optional(bigDecimal(10,2)),
                                              s"amount_${CY6}"->optional(bigDecimal(10,2)),
                                              s"amount_${CY7}"->optional(bigDecimal(10,2)),
                                              s"amount_${CY8}"->optional(bigDecimal(10,2)))(Amounts.apply)(Amounts.unapply),
            "year2015" -> mapping("definedBenefit_2015_p1"->optional(bigDecimal(10,2)),
                                  "definedContribution_2015_p1"->optional(bigDecimal(10,2)),
                                  "definedBenefit_2015_p2"->optional(bigDecimal(10,2)),
                                  "definedContribution_2015_p2"->optional(bigDecimal(10,2)),
                                  "postTriggerDcAmount2015P1"->optional(bigDecimal(10,2)),
                                  "postTriggerDcAmount2015P2"->optional(bigDecimal(10,2)))(Year2015Amounts.apply)(Year2015Amounts.unapply),
            "triggerDate" -> optional(text)
    )(CalculatorFormFields.apply)(CalculatorFormFields.unapply)
  )

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
