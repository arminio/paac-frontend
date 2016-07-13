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
import service._
import service.KeystoreService._

import scala.concurrent.Future

object PostTriggerPensionInputsController extends PostTriggerPensionInputsController {
  override val keystore: KeystoreService = KeystoreService
}

trait PostTriggerPensionInputsController extends RedirectController {
  val keystore: KeystoreService

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](List(TRIGGER_DATE_KEY, P1_TRIGGER_DC_KEY, P2_TRIGGER_DC_KEY, TRIGGER_DC_KEY, IS_EDIT_KEY)).flatMap {
      (values) =>
        val dateAsStr = values(TRIGGER_DATE_KEY)
        if (dateAsStr.isEmpty)
          Future.successful(Redirect(routes.DateOfMPAATriggerEventController.onPageLoad))
        else
          Future.successful(Ok(views.html.postTriggerPensionInputs(CalculatorForm.bind(values), values bool IS_EDIT_KEY)))
    }
  }

  val onSubmit = withSession { implicit request =>
    val f = CalculatorForm.form.bindFromRequest()
    val data = formRequestData
    f.fold(
      formWithErrors => Future.successful(Ok(views.html.postTriggerPensionInputs(formWithErrors, data("isEdit").toBoolean))),
      input => {
        val triggerP1 = input.triggerDatePeriod.get.isPeriod1
        val triggerP2 = input.triggerDatePeriod.get.isPeriod2
        if ((triggerP1 && !input.year2015.postTriggerDcAmount2015P1.isDefined) ||
            (triggerP2 && !input.year2015.postTriggerDcAmount2015P2.isDefined) ||
            (!triggerP1 && !triggerP2 && !input.triggerAmount.isDefined))
          Future.successful(Ok(views.html.postTriggerPensionInputs(f.withError("error.bounds", "error.bounds", 0, 5000000.00), data("isEdit").toBoolean)))
        else {
          val toSave: List[Option[(Long, String)]] = List(input.toP1TriggerDefinedContribution, input.toP2TriggerDefinedContribution, Some((input.triggerAmount.map(_*100L).getOrElse(0L), TRIGGER_DC_KEY)))
          keystore.save[String, Long](toSave, "").flatMap(_=>TriggerAmount() go Forward)
        }
      }
    )
  }

  val onBack = withSession { implicit request =>
    TriggerAmount() go Backward
  }
}