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

object DateOfMPAATriggerEventController extends DateOfMPAATriggerEventController {
  def keystore: KeystoreService = KeystoreService
}

trait DateOfMPAATriggerEventController extends RedirectController {

  val onPageLoad = withReadSession { implicit request =>
    val model: DateOfMPAATriggerEventPageModel = request.data
    showPage(DateOfMPAATriggerEventForm.form.fill(model), model)
  }

  val onSubmit = withWriteSession { implicit request =>
    val form = DateOfMPAATriggerEventForm.form.bindFromRequest()
    val reqData = request.data
    form.fold (
      formWithErrors => {
        val model: DateOfMPAATriggerEventPageModel = reqData
        showPage(formWithErrors, model.copy(dateOfMPAATriggerEvent=None, originalDate=request.form("originalDate")))
      },
      input => {
        input.dateOfMPAATriggerEvent.map {
          (date)=>
            // Get all selected tax years >= 2015 whose DC flag is true
            val selectedTaxYears:Seq[Int] = reqData.get(SELECTED_INPUT_YEARS_KEY).getOrElse("2014").split(",").map(_.toInt).filter(_ >= 2015)
                                                      .filter( year => reqData.getOrElse(DC_FLAG_PREFIX+year,"false").toBoolean)
            if (!isValidDate(date,selectedTaxYears)) {
              val newForm = form.withError("dateOfMPAATriggerEvent", "paac.mpaa.ta.date.page.invalid.date")
              showPage(newForm, input)
            } else {
              val sessionData = reqData ++ input.toSessionData.map(_.swap).toMap
              TriggerDate() go Forward.using(sessionData)
          }
        } getOrElse {
          val newForm = form.withError("dateOfMPAATriggerEvent", "error.invalid.date.format")
          showPage(newForm, input)
        }
      }
    )
  }

  val onBack = withSession { implicit request =>
    TriggerDate() go Backward
  }

  protected def showPage(form: Form[DateOfMPAATriggerEventPageModel], model: DateOfMPAATriggerEventPageModel)(implicit request: Request[_]) = {
    Future.successful(Ok(views.html.date_of_mpaa_trigger_event(form,model)))
  }

  private def isValidDate(date: LocalDate, selectedTaxYears: Seq[Int]): Boolean  = {
    selectedTaxYears.map((year) => (new LocalDate(year, 4, 5), new LocalDate(year + 1, 4, 6)))
      .exists { pair => date.isAfter(pair._1) && date.isBefore(pair._2) }
  }

}