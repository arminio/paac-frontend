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

case class Post2015Model(definedBenefit: BigDecimal, definedContribution: BigDecimal) extends Post2015Fields

trait Post2015DBDCForm extends Post2015FormFactory {
  def apply(year: Int): Form[_ <: Post2015Fields] = {
    if (isPoundsAndPence)
      Form[Post2015Model](mapping(penceDB(year), penceDC(year))(Post2015Model.apply)(Post2015Model.unapply))
    else
      Form[Post2015Model](mapping(poundsDB(year), poundsDC(year))(toModel)(Post2015Model.unapply))
  }

  protected val toModel: (Int, Int) => Post2015Model = (i: Int, j: Int) =>
    Post2015Model(BigDecimal(i),BigDecimal(j))
}

object Post2015DBDCForm extends Post2015DBDCForm
