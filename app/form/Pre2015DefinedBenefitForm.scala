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

case class Pre2015DefinedBenefitModel(definedBenefit: BigDecimal) extends Pre2015Fields

trait Pre2015DefinedBenefitForm extends Pre2015FormFactory {
  def apply(year: Int): Form[_ <: Pre2015Fields] = {
    if (isPoundsAndPence)
      Form[Pre2015DefinedBenefitModel](mapping(penceDB(year))(Pre2015DefinedBenefitModel.apply)(Pre2015DefinedBenefitModel.unapply))
    else
      Form[Pre2015DefinedBenefitModel](mapping(poundsDB(year))(toModel)(Pre2015DefinedBenefitModel.unapply))
  }

  protected val toModel: (Int) => Pre2015DefinedBenefitModel = (i: Int) =>
    Pre2015DefinedBenefitModel(BigDecimal(i))
}

object Pre2015DefinedBenefitForm extends Pre2015DefinedBenefitForm
