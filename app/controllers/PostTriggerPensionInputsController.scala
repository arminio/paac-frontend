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

import service._
import form._
import models.PensionPeriod
import play.api.data.Form
import service.KeystoreService._
import play.api.mvc.Request
import scala.concurrent.Future
import models.PensionPeriod._

object PostTriggerPensionInputsController extends PostTriggerPensionInputsController {
  def keystore: KeystoreService = KeystoreService
}

trait PostTriggerPensionInputsController extends RedirectController {

  val onPageLoad = withReadSession { implicit request =>
    val isEdit = request.data bool IS_EDIT_KEY
    if (request.data(TRIGGER_DATE_KEY).isEmpty) {
      TriggerDate() go Edit
    } else {
      val triggerDate: PensionPeriod = request.data(TRIGGER_DATE_KEY)
      showPage(TriggerDCForm.form(triggerDate.isPeriod1, triggerDate.isPeriod2).bind(convert(request.data)).discardingErrors, triggerDate, isEdit)
    }
  }

  val onSubmit = withWriteSession { implicit request =>
    val isEdit = request.data bool IS_EDIT_KEY
    val triggerDate: PensionPeriod = request.data(TRIGGER_DATE_KEY)

    TriggerDCForm.form(triggerDate.isPeriod1, triggerDate.isPeriod2).bindFromRequest().fold(
      formWithErrors => showPage(formWithErrors, triggerDate, isEdit),
      input => TriggerAmount() go Forward.using(request.data ++ input.data)
    )
  }

  val onBack = withSession { implicit request =>
    TriggerAmount() go Backward
  }

  protected def showPage(form: Form[_ <: TriggerDCFields], triggerDate: PensionPeriod, isEdit: Boolean)(implicit request: Request[_]) = {
    Future.successful(Ok(views.html.postTriggerPensionInputs(form, triggerDate, isEdit)))
  }
}