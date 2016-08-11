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

case class Year2015DefinedBenefitModel(definedBenefit_2015_p1: BigDecimal, definedBenefit_2015_p2: BigDecimal) extends Year2015Fields

trait Year2015DefinedBenefitForm extends Year2015FormFactory {
  def apply(): Form[_ <: Year2015Fields] = {
    if (isPoundsAndPence)
      Form[Year2015DefinedBenefitModel](mapping(penceP1DB, penceP2DB)(Year2015DefinedBenefitModel.apply)(Year2015DefinedBenefitModel.unapply))
    else
      Form[Year2015DefinedBenefitModel](mapping(poundsP1DB, poundsP2DB)(toModel)(Year2015DefinedBenefitModel.unapply))
  }

  protected val toModel: (Int, Int) => Year2015DefinedBenefitModel = (i: Int, j: Int) =>
    Year2015DefinedBenefitModel(BigDecimal(i),BigDecimal(j))
}

object Year2015DefinedBenefitForm extends Year2015DefinedBenefitForm