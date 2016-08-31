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

case class Year2015Model(definedBenefit_2015_p1: BigDecimal,
                         definedBenefit_2015_p2: BigDecimal,
                         definedContribution_2015_p1: BigDecimal,
                         definedContribution_2015_p2: BigDecimal) extends Year2015Fields

trait Year2015DBDCForm extends Year2015FormFactory {
  def apply(): Form[_ <: Year2015Fields] = {
    if (isPoundsAndPence)
      Form[Year2015Model](mapping(penceP1DB, penceP2DB, penceP1DC, penceP2DC)(Year2015Model.apply)(Year2015Model.unapply))
    else
      Form[Year2015Model](mapping(poundsP1DB, poundsP2DB, poundsP1DC, poundsP2DC)(toModel)(Year2015Model.unapply))
  }

  protected val toModel: (Long, Long, Long, Long) => Year2015Model = (i: Long, j: Long, k: Long, l: Long) =>
    Year2015Model(BigDecimal(i),BigDecimal(j),BigDecimal(k),BigDecimal(l))
}

object Year2015DBDCForm extends Year2015DBDCForm with AppSettings
