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

package form

import org.joda.time.LocalDate
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import service.KeystoreService._
import uk.gov.hmrc.play.mappers.DateTuple._
import models.PensionPeriod._
import models._

trait TriggerDateModel {
  def dateOfMPAATriggerEvent: Option[LocalDate]
  def originalDate: String
  def p1dctrigger: String
  def p2dctrigger: String
  def dctrigger: String

  def toSessionData(isEdit: Boolean): List[(String,String)] = {
    val maybeData = dateOfMPAATriggerEvent.map {
      (date)=>
      val data = List[(String,String)]((date.toString, TRIGGER_DATE_KEY))
      if (isEdit) {
        // In 'edit' mode therefore re-save values into
        val newDate: PensionPeriod = date
        val oldDate: PensionPeriod = originalDate
        if (oldDate.isPeriod1 && newDate.isPeriod2 || oldDate.isPeriod2 && newDate.isPeriod1) {
          // flip trigger values
          val v1 = if (newDate.isPeriod1) (p2dctrigger,P1_TRIGGER_DC_KEY) else (p1dctrigger, P2_TRIGGER_DC_KEY)
          val v2 = ("0", if (newDate.isPeriod1) P2_TRIGGER_DC_KEY else P1_TRIGGER_DC_KEY)
          List[(String,String)](v1, v2) ++ data
        } else if (!newDate.isPeriod1 && !newDate.isPeriod2 && (oldDate.isPeriod1 || oldDate.isPeriod2)) {
          if (oldDate.isPeriod1)
            List(("0",P1_TRIGGER_DC_KEY),("0",P2_TRIGGER_DC_KEY),(p1dctrigger,TRIGGER_DC_KEY)) ++ data
          else
            List(("0",P1_TRIGGER_DC_KEY),("0",P2_TRIGGER_DC_KEY),(p2dctrigger,TRIGGER_DC_KEY)) ++ data
        } else if (newDate.isPeriod1 || newDate.isPeriod2 && (!oldDate.isPeriod1 && !oldDate.isPeriod2)) {
          if (newDate.isPeriod1)
            List((dctrigger,P1_TRIGGER_DC_KEY),("0",P2_TRIGGER_DC_KEY),("0",TRIGGER_DC_KEY)) ++ data
          else
            List(("0",P1_TRIGGER_DC_KEY),(dctrigger,P2_TRIGGER_DC_KEY),("0",TRIGGER_DC_KEY)) ++ data
        } else {
          data
        }
      } else {
        data
      }
    }
    maybeData.getOrElse(List[(String,String)]())
  }
}

case class DateOfMPAATriggerEventPageModel(dateOfMPAATriggerEvent: Option[LocalDate],
                                           originalDate: String,
                                           p1dctrigger: String,
                                           p2dctrigger: String,
                                           dctrigger: String) extends TriggerDateModel

object DateOfMPAATriggerEventPageModel {
  implicit val formats = Json.format[DateOfMPAATriggerEventPageModel]

  implicit def toModel(data: Map[String,String]): DateOfMPAATriggerEventPageModel = {
    val dateAsStr = data.get(TRIGGER_DATE_KEY).getOrElse("")
    val p1dc = data.get(P1_TRIGGER_DC_KEY).getOrElse("")
    val p2dc = data.get(P2_TRIGGER_DC_KEY).getOrElse("")
    val dc = data.get(TRIGGER_DC_KEY).getOrElse("")
    println(p1dc)
    println(p2dc)
    println(dc)
    val model = if (dateAsStr.isEmpty) {
      DateOfMPAATriggerEventPageModel(None, "", p1dc, p2dc, dc)
    } else {
      val parts = dateAsStr.split("-").map(_.toInt)
      val date = Some(new LocalDate(parts(0),parts(1),parts(2)))
      DateOfMPAATriggerEventPageModel(date, dateAsStr, p1dc, p2dc, dc)
    }
    println(model)
    model
  }
}

trait DateOfMPAATriggerEvent{
  val mpaaDate = "dateOfMPAATriggerEvent"
}

trait DateOfMPAATriggerEventForm extends DateOfMPAATriggerEvent {
  val form: Form[DateOfMPAATriggerEventPageModel] = Form(
    mapping(
      mpaaDate -> dateTuple(validate = true),
      "originalDate" -> text,
      P1_TRIGGER_DC_KEY -> text,
      P2_TRIGGER_DC_KEY -> text,
      TRIGGER_DC_KEY -> text
    )(DateOfMPAATriggerEventPageModel.apply)(DateOfMPAATriggerEventPageModel.unapply)
  )
}

object DateOfMPAATriggerEventForm extends DateOfMPAATriggerEvent with DateOfMPAATriggerEventForm
