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
import service.KeystoreService._
import scala.concurrent.Future
import models._

object DateOfMPAATriggerEventController extends DateOfMPAATriggerEventController {
  override val keystore: KeystoreService = KeystoreService
}

trait DateOfMPAATriggerEventController extends RedirectController {
  val keystore: KeystoreService

  private val onSubmitRedirect: Call = routes.PostTriggerPensionInputsController.onPageLoad

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](List(KeystoreService.TRIGGER_DATE_KEY,P1_TRIGGER_DC_KEY,P2_TRIGGER_DC_KEY)).map {
      (values) =>
        val dateAsStr = values(TRIGGER_DATE_KEY)
        if (dateAsStr == "") {
          Ok(views.html.date_of_mpaa_trigger_event(DateOfMPAATriggerEventForm.form,values))
        } else {
          val parts = dateAsStr.split("-").map(_.toInt)
          val model = DateOfMPAATriggerEventPageModel(Some(new LocalDate(parts(0),parts(1),parts(2))))
          Ok(views.html.date_of_mpaa_trigger_event(DateOfMPAATriggerEventForm.form.fill(model),values))
        }
    }
  }

  val onSubmit = withSession { implicit request =>
    val data: Map[String, String] = request.body.asFormUrlEncoded.getOrElse(Map[String, Seq[String]]()).mapValues(_.head)
    DateOfMPAATriggerEventForm.form.bindFromRequest().fold(
      formWithErrors => { 
        Future.successful(Ok(views.html.date_of_mpaa_trigger_event(formWithErrors, data))) 
      },
      input => {
        // should store as json and read out as json but sticking with string throughout
        keystore.store(input.dateOfMPAATriggerEvent.map(_.toString).getOrElse(""), TRIGGER_DATE_KEY).flatMap {
          (_)=>
          if (input.dateOfMPAATriggerEvent.isDefined) {
            val date = input.dateOfMPAATriggerEvent.get
            if (isValidDate(input.dateOfMPAATriggerEvent.get)) {
              val form = DateOfMPAATriggerEventForm.form.bindFromRequest().withError("dateOfMPAATriggerEvent", "paac.mpaa.ta.date.page.invalid.date")
              Future.successful(Ok(views.html.date_of_mpaa_trigger_event(form, data)))
            } else {
              keystore.read[String](KeystoreService.IS_EDIT_KEY).flatMap {
                (maybeIsEdit) =>
                if (maybeIsEdit.getOrElse("false").toBoolean) {
                  // In 'edit' mode therefore re-save values into 
                  val newDate = PensionPeriod(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth())
                  val originalDate = data(TRIGGER_DATE_KEY).split("-").map(_.toInt)
                  val oldDate = PensionPeriod(originalDate(0), originalDate(1), originalDate(2))
                  if (oldDate.isPeriod1 && newDate.isPeriod2 || oldDate.isPeriod2 && newDate.isPeriod1) {
                    // flip trigger values
                    val v1: (Option[String],String) = if (newDate.isPeriod1) (Some(data(P2_TRIGGER_DC_KEY)+"00"),P1_TRIGGER_DC_KEY) else (Some(data(P1_TRIGGER_DC_KEY)+"00"), P2_TRIGGER_DC_KEY)
                    val v2: (Option[String],String) = (Some("0"), if (newDate.isPeriod1) P2_TRIGGER_DC_KEY else P1_TRIGGER_DC_KEY)
                    keystore.save[String](List(v1,v2),"").map((_)=>Results.Redirect(routes.ReviewTotalAmountsController.onPageLoad()))
                  } else {
                    Future.successful(Results.Redirect(routes.ReviewTotalAmountsController.onPageLoad()))
                  }
                } else {
                  Future.successful(Redirect(onSubmitRedirect))
                }
              }
            }
          } else {
            val form = DateOfMPAATriggerEventForm.form.bindFromRequest().withError("dateOfMPAATriggerEvent", "error.invalid.date.format")
            Future.successful(Ok(views.html.date_of_mpaa_trigger_event(form,data)))
          }
        }
      }
    )
  }

  private def isValidDate(date: LocalDate): Boolean  = {
    (date.getYear() < 2015) || (date.getYear() >= 2017) ||
    (date.getYear() == 2015 && date.getMonthOfYear() < 4) ||
    (date.getYear() == 2015 && date.getMonthOfYear() == 4 && date.getDayOfMonth() < 6) ||
    (date.getYear() == 2016 && date.getMonthOfYear() > 4 ) ||
    (date.getYear() == 2016 && date.getMonthOfYear() == 4 && date.getDayOfMonth() >= 6)
  }
}