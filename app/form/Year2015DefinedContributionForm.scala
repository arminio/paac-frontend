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
import config.Settings
import config.AppSettings

case class Year2015DefinedContributionModel(definedContribution_2015_p1: BigDecimal, definedContribution_2015_p2: BigDecimal) extends Year2015Fields

trait Year2015DefinedContributionForm extends Year2015FormFactory {
  def apply(): Form[_ <: Year2015Fields] = {
    if (isPoundsAndPence)
      Form[Year2015DefinedContributionModel](mapping(penceP1DC, penceP2DC)(Year2015DefinedContributionModel.apply)(Year2015DefinedContributionModel.unapply))
    else
      Form[Year2015DefinedContributionModel](mapping(poundsP1DC, poundsP2DC)(toModel)(Year2015DefinedContributionModel.unapply))
  }

  protected val toModel: (Long, Long) => Year2015DefinedContributionModel = (i: Long, j: Long) =>
    Year2015DefinedContributionModel(BigDecimal(i),BigDecimal(j))
}

object Year2015DefinedContributionForm extends Year2015DefinedContributionForm with AppSettings