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

object YesNoThresholdIncomeController extends YesNoThresholdIncomeController {
  override val keystore: KeystoreService = KeystoreService
}

trait YesNoThresholdIncomeController extends RedirectController {
  val keystore: KeystoreService

  private val yesNoFormKey = "yesNo"

  val onPageLoad = withSession { implicit request =>
    keystore.read(List(CURRENT_INPUT_YEAR_KEY)).flatMap {
      (fieldsMap) =>
      val year = fieldsMap int CURRENT_INPUT_YEAR_KEY
      keystore.read[String](s"${TI_YES_NO_KEY_PREFIX}${year}").map {
        (yesNo) =>
          val value = yesNo match {
            case Some(value) => value.toString
            case None => "Yes"
          }
          Ok(views.html.yesno_for_threshold_income(YesNoMPAATriggerEventForm.form.fill(value), year))
      }
    }
  }

  val onSubmit = withSession { implicit request =>
    val data = formRequestData
    val year = data("year").toInt
    YesNoMPAATriggerEventForm.form.bindFromRequest().fold(
      formWithErrors => { Future.successful(Ok(views.html.yesno_for_threshold_income(YesNoMPAATriggerEventForm.form, year))) },
      input => {
        keystore.store(input, s"${TI_YES_NO_KEY_PREFIX}${year}")
        if (input == "Yes")
          YesNoIncome() go Forward
        else
          keystore.save(List((None,s"${AI_PREFIX}${year}")),"").flatMap(_=>YesNoIncome() go Forward)
      }
    )
  }

  val onBack = withSession { implicit request =>
    YesNoIncome() go Backward
  }
}
