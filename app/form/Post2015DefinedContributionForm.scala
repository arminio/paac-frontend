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

case class Post2015DefinedContributionModel(definedContribution: BigDecimal) extends Post2015Fields

trait Post2015DefinedContributionForm extends Post2015FormFactory {
  def apply(year: Int): Form[_ <: Post2015Fields] = {
    if (isPoundsAndPence)
      Form[Post2015DefinedContributionModel](mapping(penceDC(year))(Post2015DefinedContributionModel.apply)(Post2015DefinedContributionModel.unapply))
    else
      Form[Post2015DefinedContributionModel](mapping(poundsDC(year))(toModel)(Post2015DefinedContributionModel.unapply))
  }

  protected val toModel: (Int) => Post2015DefinedContributionModel = (i: Int) =>
    Post2015DefinedContributionModel(BigDecimal(i))
}

object Post2015DefinedContributionForm extends Post2015DefinedContributionForm with AppSettings