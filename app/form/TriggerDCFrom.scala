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

import form.utilities._
import play.api.data.Form
import play.api.data.Forms._
import config.Settings
import config.AppSettings
import play.api.data.Mapping
import service.KeystoreService._

trait TriggerDCFields {
  def data: Map[String,String] = toMap(this).map{
    (entry)=>
      val value = f"${(entry._2.toString.toDouble*100)}%2.0f".trim
      (entry._1, value)
  }
}

trait TriggerDCFormFactory extends Settings {
  settings: Settings =>

  def apply(): Form[_ <: TriggerDCFields]

  protected def isPoundsAndPence(): Boolean = settings.POUNDS_AND_PENCE
  protected def poundsPeriod1TEDC: (String, Mapping[Long])= (P1_TRIGGER_DC_KEY) -> poundsLongField(true)
  protected def pencePeriod1TEDC: (String, Mapping[BigDecimal])= (P1_TRIGGER_DC_KEY) -> poundsAndPenceField(true)
  protected def poundsPeriod2TEDC: (String, Mapping[Long])= (P2_TRIGGER_DC_KEY) -> poundsLongField(true)
  protected def pencePeriod2TEDC: (String, Mapping[BigDecimal])= (P2_TRIGGER_DC_KEY) -> poundsAndPenceField(true)
  protected def poundsTEDC: (String, Mapping[Long])= (TRIGGER_DC_KEY) -> poundsLongField(true)
  protected def penceTEDC: (String, Mapping[BigDecimal])= (TRIGGER_DC_KEY) -> poundsAndPenceField(true)
}

trait TriggerDCForm {
  def form(isPeriod1: Boolean, isPeriod2: Boolean): Form[_ <: TriggerDCFields] =
    (isPeriod1, isPeriod2) match {
      case (true, false)  => Period1TriggerDCForm()
      case (false, true)  => Period2TriggerDCForm()
      case _              => TriggerDefinedContributionForm()
    }
}

object TriggerDCForm extends TriggerDCForm

