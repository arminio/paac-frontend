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

object YesNoMPAATriggerEventAmountController extends YesNoMPAATriggerEventAmountController {
  override val keystore: KeystoreService = KeystoreService
}

trait YesNoMPAATriggerEventAmountController extends RedirectController {
  val keystore: KeystoreService

  private val yesNoFormKey = "yesNo"

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](TE_YES_NO_KEY).map {
      (yesNo) =>
        val fields = Map(yesNo match {
          case Some(value) => (yesNoFormKey, value)
          case None => (yesNoFormKey, "Yes")
        })
        Ok(views.html.yesno_mpaa_trigger_amount(YesNoMPAATriggerEventForm.form.bind(fields).discardingErrors))
    }
  }

  val onSubmit = withSession { implicit request =>
    YesNoMPAATriggerEventForm.form.bindFromRequest().fold(
      formWithErrors => { Future.successful(Ok(views.html.yesno_mpaa_trigger_amount(YesNoMPAATriggerEventForm.form))) },
      input => {
        keystore.store(input, TE_YES_NO_KEY)
        if (input == "Yes")
          YesNoTrigger(PageState(isTE=true)) go Forward
        else
          keystore.save(List((None,P1_TRIGGER_DC_KEY),(None,P2_TRIGGER_DC_KEY),(None,TRIGGER_DATE_KEY)),"").flatMap(_=>YesNoTrigger() go Forward)
      }
    )
  }

  val onBack = withSession { implicit request =>
    YesNoTrigger() go Backward
  }
}
