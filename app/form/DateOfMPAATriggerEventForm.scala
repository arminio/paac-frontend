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
import play.api.i18n.Messages
import play.api.libs.json.Json
import uk.gov.hmrc.play.mappers.DateTuple._

case class DateOfMPAATriggerEventPageModel(dateOfMPAATriggerEvent: Option[LocalDate])

object DateOfMPAATriggerEventPageModel {
  implicit val formats = Json.format[DateOfMPAATriggerEventPageModel]
}

trait DateOfMPAATriggerEvent{
  val mpaaDate = "dateOfMPAATriggerEvent"
}

object DateOfMPAATriggerEventForm extends DateOfMPAATriggerEvent with DateOfMPAATriggerEventForm

trait DateOfMPAATriggerEventForm extends DateOfMPAATriggerEvent {

  val form: Form[DateOfMPAATriggerEventPageModel] = Form(
    mapping(
      mpaaDate -> dateTuple(validate = true)
    )(DateOfMPAATriggerEventPageModel.apply)(DateOfMPAATriggerEventPageModel.unapply)
  )
}
