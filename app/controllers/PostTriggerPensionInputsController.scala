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

package controllers

import form.CalculatorForm
import org.joda.time.LocalDate
import service.KeystoreService

import scala.concurrent.Future

object PostTriggerPensionInputsController extends PostTriggerPensionInputsController {
  override val keystore: KeystoreService = KeystoreService
}

trait PostTriggerPensionInputsController extends RedirectController {
  val keystore: KeystoreService

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](List(KeystoreService.TRIGGER_DATE_KEY, KeystoreService.P1_TRIGGER_DC_KEY, KeystoreService.P2_TRIGGER_DC_KEY)).flatMap {
      (values) =>
        val dateAsStr = values(KeystoreService.TRIGGER_DATE_KEY)
        if (dateAsStr.isEmpty) {
          Future.successful(Redirect(routes.DateOfMPAATriggerEventController.onPageLoad))
        } else {
          val taxYear = selectedTaxYear(dateAsStr).getOrElse("2017")
          val triggeredDate = flexiAccessDate(dateAsStr)
          Future.successful(Ok(views.html.postTriggerPensionInputs(CalculatorForm.bind(values),taxYear,triggeredDate)))
        }
    }
  }

  val onSubmit = withSession { implicit request =>
    val f = CalculatorForm.form.bindFromRequest()
    keystore.read[String](KeystoreService.TRIGGER_DATE_KEY).flatMap {
      (dateAsStr) =>
        val taxYear = selectedTaxYear(dateAsStr.get).getOrElse("2017")
        val triggeredDate = flexiAccessDate(dateAsStr.get)
        f.fold(
          formWithErrors => {
            Future.successful(Ok(views.html.postTriggerPensionInputs(formWithErrors, taxYear, triggeredDate)))
          },
          input => {
            val triggerP1 = input.triggerDatePeriod.get.isPeriod1
            val triggerP2 = input.triggerDatePeriod.get.isPeriod2
            if ((triggerP1 && !input.year2015.postTriggerDcAmount2015P1.isDefined) ||
              (triggerP2 && !input.year2015.postTriggerDcAmount2015P2.isDefined)
            ) {
              Future.successful(Ok(views.html.postTriggerPensionInputs(f.withError("error.bounds", "error.bounds", 0, 5000000.00),
                                                                        taxYear, triggeredDate)))
            } else {
              val toSave: Option[(Long, String)] = if (triggerP1) {
                input.toP1TriggerDefinedContribution
              } else {
                input.toP2TriggerDefinedContribution
              }
              keystore.save[String, Long](List(toSave), "").flatMap {
                (a) =>
                  wheretoNext(Redirect(routes.ReviewTotalAmountsController.onPageLoad))
              }
            }
          }
        )
    }
  }

  val onBack = withSession { implicit request =>
    wheretoBack(Redirect(routes.DateOfMPAATriggerEventController.onPageLoad))
  }

  private def flexiAccessDate(date:String): String = {
    date.split("-").reverse.mkString(" ")
  }

  def selectedTaxYear(date: String):Option[String] = new LocalDate(date) match {
    case date1 if (!date1.isBefore(new LocalDate("2015-4-6")) && !date1.isAfter(new LocalDate("2016-4-5"))) => Some("2015")
    case date2 if (!date2.isBefore(new LocalDate("2016-4-6")) && !date2.isAfter(new LocalDate("2017-4-5"))) => Some("2016")
    case _ => None
  }
}