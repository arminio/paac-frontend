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
        if (dateAsStr == "") {
          Future.successful(Redirect(routes.DateOfMPAATriggerEventController.onPageLoad))
        } else {
          val f = CalculatorForm.bind(values)
          Future.successful(Ok(views.html.postTriggerPensionInputs(f, f.get)))
        }
    }
  }

  val onSubmit = withSession { implicit request =>
    CalculatorForm.form.bindFromRequest().fold(
      formWithErrors => {
        val f = CalculatorForm.nonValidatingForm.bindFromRequest()
        Future.successful( Ok(views.html.postTriggerPensionInputs(formWithErrors, f.get)))
      },
      input => {
        val triggerP1 = input.triggerDatePeriod.get.isPeriod1
        val triggerP2 = input.triggerDatePeriod.get.isPeriod2
        if ((triggerP1 && input.year2015.postTriggerDcAmount2015P1 == None) ||
            (triggerP2 && input.year2015.postTriggerDcAmount2015P2 == None)
           ) {
          val f = CalculatorForm.nonValidatingForm.bindFromRequest()
          Future.successful( Ok(views.html.postTriggerPensionInputs(f.withError("error.bounds", "error.bounds", 0, 99999999.99), f.get)) )
        } else {
          val toSave: Option[(Long,String)] = if (triggerP1) { input.toP1TriggerDefinedContribution } else { input.toP2TriggerDefinedContribution }
          keystore.save[String,Long](List(toSave), "").flatMap {
            (a) =>
            wheretoNext(Redirect(routes.ReviewTotalAmountsController.onPageLoad))
          }
        }
      }
    )
  }
}
