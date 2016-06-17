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
import service.KeystoreService
import scala.concurrent.Future

object DateOfMPAATriggerEventController extends DateOfMPAATriggerEventController {
  override val keystore: KeystoreService = KeystoreService
}

trait DateOfMPAATriggerEventController extends RedirectController {
  val keystore: KeystoreService

  private val onSubmitRedirect: Call = routes.PostTriggerPensionInputsController.onPageLoad

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](KeystoreService.TRIGGER_DATE_KEY).map {
      (date) =>
        val dateAsStr = date.getOrElse("")
        if (dateAsStr == "") {
          Ok(views.html.date_of_mpaa_trigger_event(DateOfMPAATriggerEventForm.form))
        } else {
          val parts = dateAsStr.split("-").map(_.toInt)
          val model = DateOfMPAATriggerEventPageModel(Some(new LocalDate(parts(0),parts(1),parts(2))))
          Ok(views.html.date_of_mpaa_trigger_event(DateOfMPAATriggerEventForm.form.fill(model)))
        }
    }
  }

  val onSubmit = withSession { implicit request =>
    DateOfMPAATriggerEventForm.form.bindFromRequest().fold(
      formWithErrors => { 
        Future.successful(Ok(views.html.date_of_mpaa_trigger_event(formWithErrors))) 
      },
      input => {
        // should store as json and read out as json but sticking with string throughout
        keystore.store(input.dateOfMPAATriggerEvent.map(_.toString).getOrElse(""), KeystoreService.TRIGGER_DATE_KEY).flatMap {
          (_)=>
          if (input.dateOfMPAATriggerEvent.isDefined) {
            val date = input.dateOfMPAATriggerEvent.get

            if ((date.getYear() < 2015) || (date.getYear() >= 2017) ||
                (date.getYear() == 2015 && date.getMonthOfYear() < 4) || 
                (date.getYear() == 2015 && date.getMonthOfYear() == 4 && date.getDayOfMonth() < 6) ||
                (date.getYear() == 2016 && date.getMonthOfYear() > 4 ) ||
                (date.getYear() == 2016 && date.getMonthOfYear() == 4 && date.getDayOfMonth() >= 6)) {
              val form = DateOfMPAATriggerEventForm.form.bindFromRequest().withError("dateOfMPAATriggerEvent", "paac.mpaa.ta.date.page.invalid.date")
              Future.successful(Ok(views.html.date_of_mpaa_trigger_event(form)))
            } else {
              keystore.read[String](KeystoreService.IS_EDIT_KEY).map {
                (maybeIsEdit) =>
                if (maybeIsEdit.getOrElse("false").toBoolean) {
                  Results.Redirect(routes.ReviewTotalAmountsController.onPageLoad())
                } else {
                  Redirect(onSubmitRedirect)
                }
              }
            }
          } else {
            val form = DateOfMPAATriggerEventForm.form.bindFromRequest().withError("dateOfMPAATriggerEvent", "error.invalid.date.format")
            Future.successful(Ok(views.html.date_of_mpaa_trigger_event(form)))
          }
        }
      }
    )
  }
}
