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

import form.YesNoMPAATriggerEventForm
import play.api.mvc._
import service._
import service.KeystoreService._
import scala.concurrent.Future
import config.AppSettings

object YesNoMPAATriggerEventAmountController extends YesNoMPAATriggerEventAmountController with AppSettings {
  def keystore: KeystoreService = KeystoreService
}

trait YesNoMPAATriggerEventAmountController extends RedirectController {
  val onPageLoad = withReadSession { implicit request =>
    val value = request.data.get(TE_YES_NO_KEY) match {
      case Some(value) => value
      case None => "Yes"
    }
    Future.successful(Ok(views.html.yesno_mpaa_trigger_amount(YesNoMPAATriggerEventForm.form.fill(value))))
  }

  val onSubmit = withWriteSession { implicit request =>
    YesNoMPAATriggerEventForm.form.bindFromRequest().fold(
      formWithErrors => Future.successful(Ok(views.html.yesno_mpaa_trigger_amount(YesNoMPAATriggerEventForm.form))),
      input => {
        var sessionData = request.data ++ Map((TE_YES_NO_KEY->input))
        if (input == "No") {
          sessionData = sessionData ++ Map((P1_TRIGGER_DC_KEY->""),
                                           (P2_TRIGGER_DC_KEY->""),
                                           (TRIGGER_DC_KEY->""),
                                           (TRIGGER_DATE_KEY->""))
        }
        YesNoTrigger() go Forward.using(sessionData)
      }
    )
  }

  val onBack = withWriteSession { implicit request =>
    YesNoTrigger() go Backward
  }
}
