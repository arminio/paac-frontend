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

case class Period1TriggerDCModel(triggerDefinedContribution_2015_p1: BigDecimal) extends TriggerDCFields

trait Period1TriggerDCForm extends TriggerDCFormFactory {
  def apply(): Form[_ <: TriggerDCFields] = {
    if (isPoundsAndPence)
      Form[Period1TriggerDCModel](mapping(pencePeriod1TEDC)(Period1TriggerDCModel.apply)(Period1TriggerDCModel.unapply))
    else
      Form[Period1TriggerDCModel](mapping(poundsPeriod1TEDC)(toModel)(Period1TriggerDCModel.unapply))
  }

  protected val toModel: (Long) => Period1TriggerDCModel = (i: Long) =>
    Period1TriggerDCModel(BigDecimal(i))
}

object Period1TriggerDCForm extends Period1TriggerDCForm with AppSettings