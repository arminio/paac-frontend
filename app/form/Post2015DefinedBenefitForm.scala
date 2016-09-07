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

case class Post2015DefinedBenefitModel(definedBenefit: BigDecimal) extends Post2015Fields

trait Post2015DefinedBenefitForm extends Post2015FormFactory {
  def apply(year: Int): Form[_ <: Post2015Fields] = {
    if (isPoundsAndPence)
      Form[Post2015DefinedBenefitModel](mapping(penceDB(year))(Post2015DefinedBenefitModel.apply)(Post2015DefinedBenefitModel.unapply))
    else
      Form[Post2015DefinedBenefitModel](mapping(poundsDB(year))(toModel)(Post2015DefinedBenefitModel.unapply))
  }

  protected val toModel: (Long) => Post2015DefinedBenefitModel = (i: Long) =>
    Post2015DefinedBenefitModel(BigDecimal(i))
}

object Post2015DefinedBenefitForm extends Post2015DefinedBenefitForm with AppSettings