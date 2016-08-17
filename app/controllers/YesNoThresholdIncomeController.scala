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

object YesNoThresholdIncomeController extends YesNoThresholdIncomeController with AppSettings {
  def keystore: KeystoreService = KeystoreService
}

trait YesNoThresholdIncomeController extends RedirectController {
  def onPageLoad(year:Int) = withReadSession { implicit request =>
    val value = request.data.get(s"${TI_YES_NO_KEY_PREFIX}${year}") match {
      case Some(value) => value
      case None => "Yes"
    }
    Future.successful(Ok(views.html.yesno_for_threshold_income(YesNoMPAATriggerEventForm.form.fill(value), year)))
  }

  val onSubmit = withWriteSession { implicit request =>
    val year = request.data(CURRENT_INPUT_YEAR_KEY)
    YesNoMPAATriggerEventForm.form.bindFromRequest().fold(
      formWithErrors => Future.successful(Ok(views.html.yesno_for_threshold_income(YesNoMPAATriggerEventForm.form, year.toInt))),
      input => {
        var sessionData = request.data ++ Map((s"${TI_YES_NO_KEY_PREFIX}${year}"->input))
        if (input == "No") {
          sessionData = sessionData ++ Map((s"${AI_PREFIX}${year}"->""))
        }
        YesNoIncome() go Forward.using(sessionData)
      }
    )
  }

  def onBack(year:Int) = withWriteSession { implicit request =>
    YesNoIncome() go Backward
  }
}
