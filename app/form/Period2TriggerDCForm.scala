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

case class Period2TriggerDCModel(triggerDefinedContribution_2015_p2: BigDecimal) extends TriggerDCFields

trait Period2TriggerDCForm extends TriggerDCFormFactory {
  def apply(): Form[_ <: TriggerDCFields] = {
    if (isPoundsAndPence)
      Form[Period2TriggerDCModel](mapping(pencePeriod2TEDC)(Period2TriggerDCModel.apply)(Period2TriggerDCModel.unapply))
    else
      Form[Period2TriggerDCModel](mapping(poundsPeriod2TEDC)(toModel)(Period2TriggerDCModel.unapply))
  }

  protected val toModel: (Long) => Period2TriggerDCModel = (i: Long) =>
    Period2TriggerDCModel(BigDecimal(i))
}

object Period2TriggerDCForm extends Period2TriggerDCForm with AppSettings