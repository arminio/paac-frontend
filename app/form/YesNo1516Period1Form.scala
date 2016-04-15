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

import models.{TaxPeriod, Contribution,InputAmounts}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint
import scala.math._
import play.api.data.validation._
import play.api.i18n.Messages

trait YesNo1516Period1{
  val yesNo = "yesnoFor1516P1"
}

object YesNo1516Period1Form extends YesNo1516Period1 {

  type YesNo1516Period1Form = String

  val form: Form[YesNo1516Period1Form] = Form(
    mapping(
      "yesNo" -> text
    )((yesNo) => yesNo)((yesNo: YesNo1516Period1Form) => Some(yesNo))
)
}