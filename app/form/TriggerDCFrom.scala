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
import models.ThisYear
import play.api.data.Mapping
import service.KeystoreService._

trait TriggerDCFields {
  def data(year: Int): Map[String,String] = toMap(this).map{
    (entry)=>
    (s"${entry._1}_${year}", (entry._2.toString.toDouble*100).floor.toInt.toString)
  }
}

trait TriggerDCFormFactory extends ThisYear {
  settings: ThisYear =>

  def apply(year: Int): Form[_ <: TriggerDCFields]

  protected def isPoundsAndPence(): Boolean = settings.POUNDS_AND_PENCE
  protected def poundsPeriod1TEDC(year: Int): (String, Mapping[Int])= (P1_TRIGGER_DC_KEY) -> poundsField(true)
  protected def pencePeriod1TEDC(year: Int): (String, Mapping[BigDecimal])= (P1_TRIGGER_DC_KEY) -> poundsAndPenceField(true)
  protected def poundsPeriod2TEDC(year: Int): (String, Mapping[Int])= (P2_TRIGGER_DC_KEY) -> poundsField(true)
  protected def pencePeriod2TEDC(year: Int): (String, Mapping[BigDecimal])= (P2_TRIGGER_DC_KEY) -> poundsAndPenceField(true)
  protected def poundsTEDC(year: Int): (String, Mapping[Int])= (TRIGGER_DC_KEY) -> poundsField(true)
  protected def penceTEDC(year: Int): (String, Mapping[BigDecimal])= (TRIGGER_DC_KEY) -> poundsAndPenceField(true)
}

trait TriggerDCForm {
  def form(isPeriod1: Boolean, isPeriod2: Boolean, year: Int): Form[_ <: TriggerDCFields] =
    (isPeriod1, isPeriod2) match {
      case (true, false)  => Period1TriggerDCForm(year)
      case (false, true)  => Period2TriggerDCForm(year)
      case _              => TriggerDefinedContributionForm(year)
    }
}

object TriggerDCForm extends TriggerDCForm

