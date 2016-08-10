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

case class Post1516Data(period1DCAmount: Int, period1DBAmount: Int, period2DCAmount: Int, period2DBAmount: Int)

object Post1516Data {
  implicit val formats = Json.format[Post1516Data]
}

object Post1516DataForm extends Post1516DataForm

trait Post1516DataForm {

  val form: Form[Post1516Data] = Form(
    mapping(
      "period1DCAmount" -> number,
      "period1DBAmount" -> number,
      "period2DCAmount" -> number,
      "period2DBAmount" -> number
    )(Post1516Data.apply)(Post1516Data.unapply)
  )
}
