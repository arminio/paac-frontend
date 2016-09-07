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

import form.{DateOfMPAATriggerEventPageModel, DateOfMPAATriggerEventForm}
import org.joda.time.LocalDate
import service._
import service.KeystoreService._
import scala.concurrent.Future
import play.api.data.Form
import play.api.mvc.Request
import config.AppSettings

object DateOfMPAATriggerEventController extends DateOfMPAATriggerEventController with AppSettings {
  def keystore: KeystoreService = KeystoreService
}

trait DateOfMPAATriggerEventController extends RedirectController {

  val onPageLoad = withReadSession { implicit request =>
    val model: DateOfMPAATriggerEventPageModel = request.data
    val isEdit = request.data.get(IS_EDIT_KEY).map(_.toBoolean).getOrElse(false)
    showPage(DateOfMPAATriggerEventForm.form.fill(model), isEdit)
  }

  val onSubmit = withWriteSession { implicit request =>
    val form = DateOfMPAATriggerEventForm.form.bindFromRequest()
    val reqData = request.data
    val isEdit = reqData.get(IS_EDIT_KEY).map(_.toBoolean).getOrElse(false)
    form.fold (
      formWithErrors => {
        val model: DateOfMPAATriggerEventPageModel = reqData
        showPage(formWithErrors, isEdit)
      },
      input => {
        input.dateOfMPAATriggerEvent.map {
          (date)=>
            // Get all selected tax years >= 2015 whose DC flag is true
            val selectedDCTaxYears:Seq[Int] = reqData.getOrElse(SELECTED_INPUT_YEARS_KEY,"2014").split(",").map(_.toInt).filter(_ >= 2015)
                                                      .filter( year => reqData.getOrElse(DC_FLAG_PREFIX + year,"false").toBoolean).sorted

            val args = List(selectedDCTaxYears.head.toString, (selectedDCTaxYears.last + 1).toString)
            if (!isValidDate(date,selectedDCTaxYears)) {
              val newForm = form.withError("dateOfMPAATriggerEvent", "paac.mpaa.ta.date.page.invalid.date", args:_*)
              showPage(newForm, isEdit)
            } else {
              val sessionData = reqData ++ input.toSessionData(isEdit).map(_.swap).toMap
              TriggerDate() go Forward.using(sessionData)
          }
        } getOrElse {
          val newForm = form.withError("dateOfMPAATriggerEvent", "error.invalid.date.format")
          showPage(newForm, isEdit)
        }
      }
    )
  }

  val onBack = withWriteSession { implicit request =>
    TriggerDate() go Backward
  }

  protected def showPage(form: Form[DateOfMPAATriggerEventPageModel], isEdit: Boolean)(implicit request: Request[_]) = {
    Future.successful(Ok(views.html.date_of_mpaa_trigger_event(form, isEdit)))
  }

  private def isValidDate(date: LocalDate, selectedTaxYears: Seq[Int]): Boolean  = {
    selectedTaxYears.exists((year) => date.isAfter(new LocalDate(year, 4, 5)) &&  date.isBefore(new LocalDate(year + 1, 4, 6)))
  }

}