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

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

case class Pre1516Data(totalAmount: Int)

object Pre1516Data {
  implicit val formats = Json.format[Pre1516Data]
}

object Pre1516DataForm extends Pre1516DataForm

trait Pre1516DataForm {

  val form: Form[Pre1516Data] = Form(
    mapping(
      "totalAmount" -> number
    )(Pre1516Data.apply)(Pre1516Data.unapply)
  )
}
