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
import play.api.mvc._
import service._
import service.KeystoreService._
import scala.concurrent.Future
import models._
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
    form.fold (
      formWithErrors => {
        val model: DateOfMPAATriggerEventPageModel = request.data
        showPage(formWithErrors, model.copy(dateOfMPAATriggerEvent=None, originalDate=request.form("originalDate")))
      },
      input => {
        input.dateOfMPAATriggerEvent.map {
          (date)=>
          if (!isValidDate(date)) {
            val newForm = form.withError("dateOfMPAATriggerEvent", "paac.mpaa.ta.date.page.invalid.date")
            showPage(newForm, input)
          } else {
            val sessionData = request.data ++ input.toSessionData.map(_.swap).toMap
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

  private def isValidDate(date: LocalDate): Boolean  = {
    (date.getYear() == 2016) ||
    (date.getYear() == 2015 && date.getMonthOfYear() > 4) ||
    (date.getYear() == 2015 && date.getMonthOfYear() == 4 && date.getDayOfMonth() >= 6) ||
    (date.getYear() == 2017 && date.getMonthOfYear() < 4) ||
    (date.getYear() == 2017 && date.getMonthOfYear() == 4 && date.getDayOfMonth() < 6)
  }
}