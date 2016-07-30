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
import models._
import play.api.data.Form
import service.KeystoreService._
import play.api.mvc.Request
import scala.concurrent.Future

object PostTriggerPensionInputsController extends PostTriggerPensionInputsController {
  def keystore: KeystoreService = KeystoreService
}

trait PostTriggerPensionInputsController extends RedirectController {

  val onPageLoad = withReadSession { implicit request =>
    val dateAsStr = request.data(TRIGGER_DATE_KEY)
    if (dateAsStr.isEmpty)
      Future.successful(Redirect(routes.DateOfMPAATriggerEventController.onPageLoad)) // todo
    else
      showPage(CalculatorForm.bind(request.data), request.data bool IS_EDIT_KEY)
  }

  val onSubmit = withWriteSession { implicit request =>
    val f = CalculatorForm.form.bindFromRequest()
    val isEdit = request.form bool IS_EDIT_KEY
    val data = request.form
    f.fold(
      formWithErrors => showPage(formWithErrors, isEdit),
      input => {
        val triggerP1 = input.triggerDatePeriod.get.isPeriod1
        val triggerP2 = input.triggerDatePeriod.get.isPeriod2
        if ((triggerP1 && !input.year2015.postTriggerDcAmount2015P1.isDefined) ||
            (triggerP2 && !input.year2015.postTriggerDcAmount2015P2.isDefined) ||
            (!triggerP1 && !triggerP2 && !input.triggerAmount.isDefined))
          showPage(f.withError("error.bounds", "error.bounds", 0, 5000000.00), isEdit)
        else {
          val formData = List(input.toP1TriggerDefinedContribution.getOrElse(("",P1_TRIGGER_DC_KEY)),
                              input.toP2TriggerDefinedContribution.getOrElse(("",P2_TRIGGER_DC_KEY)),
                              (input.triggerAmount.map(_*100L).getOrElse(0L), TRIGGER_DC_KEY)).map(_.swap).toMap.mapValues(_.toString)

          val sessionData = request.data ++ formData
          TriggerAmount() go Forward.using(sessionData)
        }
      }
    )
  }

  val onBack = withSession { implicit request =>
    TriggerAmount() go Backward
  }

  protected def showPage(form: Form[CalculatorFormFields], isEdit: Boolean)(implicit request: Request[_]) = {
    Future.successful(Ok(views.html.postTriggerPensionInputs(form, isEdit)))
  }
}