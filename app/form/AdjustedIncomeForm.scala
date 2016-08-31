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
import form.utilities._
import config.AppSettings

case class AdjustedIncomeModel(adjustedIncome: BigDecimal) extends AIFields

trait AdjustedIncomeForm extends AIFormFactory {
  def apply(year: Int): Form[_ <: AIFields] = {
    if (isPoundsAndPence)
      Form[AdjustedIncomeModel](mapping(penceDB(year))(AdjustedIncomeModel.apply)(AdjustedIncomeModel.unapply))
    else
      Form[AdjustedIncomeModel](mapping(poundsDB(year))(toModel)(AdjustedIncomeModel.unapply))
  }

  protected val toModel: (Long) => AdjustedIncomeModel = (i: Long) =>
    AdjustedIncomeModel(BigDecimal(i))
}

object AdjustedIncomeForm extends AdjustedIncomeForm with AppSettings
