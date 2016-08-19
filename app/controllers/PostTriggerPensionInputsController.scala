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
import config.AppSettings
import org.joda.time.LocalDate

object PostTriggerPensionInputsController extends PostTriggerPensionInputsController with AppSettings {
  def keystore: KeystoreService = KeystoreService
}

trait PostTriggerPensionInputsController extends RedirectController {

  val onPageLoad = withReadSession { implicit request =>
    if (request.data(TRIGGER_DATE_KEY).isEmpty) {
      TriggerDate() go Edit
    } else {
      val isEdit = request.data bool IS_EDIT_KEY
      val triggerDate: PensionPeriod = request.data(TRIGGER_DATE_KEY)
      // Get all selected tax years >= 2015 whose DC flag is true
      val selectedDCTaxYears:Seq[Int] = request.data.getOrElse(SELECTED_INPUT_YEARS_KEY,"2014").split(",").map(_.toInt).filter(_ >= 2015)
        .filter( year => request.data.getOrElse(DC_FLAG_PREFIX + year,"false").toBoolean).sorted
      val triggeredTaxYear = getTriggeredTaxYear(triggerDate,selectedDCTaxYears)

      showPage(TriggerDCForm.form(triggerDate.isPeriod1, triggerDate.isPeriod2)
              .bind(convert(request.data)).discardingErrors, triggerDate, triggeredTaxYear, isEdit)
    }
  }

  val onSubmit = withWriteSession { implicit request =>
    val isEdit = request.data bool IS_EDIT_KEY
    val triggerDate: PensionPeriod = request.data(TRIGGER_DATE_KEY)
      // Get all selected tax years >= 2015 whose DC flag is true
    val selectedDCTaxYears:Seq[Int] = request.data.getOrElse(SELECTED_INPUT_YEARS_KEY,"2014").split(",").map(_.toInt).filter(_ >= 2015)
                                             .filter( year => request.data.getOrElse(DC_FLAG_PREFIX + year,"false").toBoolean).sorted
    val triggeredTaxYear = getTriggeredTaxYear(triggerDate,selectedDCTaxYears)

    TriggerDCForm.form(triggerDate.isPeriod1, triggerDate.isPeriod2).bindFromRequest().fold(
      formWithErrors => showPage(formWithErrors, triggerDate, triggeredTaxYear, isEdit),
      input => TriggerAmount() go Forward.using(request.data ++ input.data)
    )
  }

  val onBack = withWriteSession { implicit request =>
    TriggerAmount() go Backward
  }

  protected def showPage(form: Form[_ <: TriggerDCFields], triggerDate: PensionPeriod, triggeredTaxYear:Int, isEdit: Boolean)(implicit request: Request[_]) = {
    Future.successful(Ok(views.html.postTriggerPensionInputs(form, triggerDate, triggeredTaxYear, isEdit)))
  }

  private def getTriggeredTaxYear(triggeredDate: PensionPeriod, selectedTaxYears: Seq[Int]):Int = {
    val date:LocalDate = new LocalDate(triggeredDate.year,triggeredDate.month,triggeredDate.day)
    selectedTaxYears.find((year) => date.isAfter(new LocalDate(year, 4, 5)) && date.isBefore(new LocalDate(year + 1, 4, 6))).getOrElse(-1)
  }

}