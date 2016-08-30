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

case class TriggerDCModel(triggerDefinedContribution: BigDecimal) extends TriggerDCFields

trait TriggerDefinedContributionForm extends TriggerDCFormFactory {
  def apply(): Form[_ <: TriggerDCFields] = {
    if (isPoundsAndPence)
      Form[TriggerDCModel](mapping(penceTEDC)(TriggerDCModel.apply)(TriggerDCModel.unapply))
    else
      Form[TriggerDCModel](mapping(poundsTEDC)(toModel)(TriggerDCModel.unapply))
  }

  protected val toModel: (Long) => TriggerDCModel = (i: Long) =>
    TriggerDCModel(BigDecimal(i))
}

object TriggerDefinedContributionForm extends TriggerDefinedContributionForm with AppSettings