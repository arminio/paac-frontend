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

trait Year2015Fields {
  def data(): Map[String,String] = toMap(this).mapValues((v)=>(v.toString.toDouble*100).floor.toInt.toString)
}

trait Year2015FormFactory extends Settings {
  settings: Settings =>

  def apply(): Form[_ <: Year2015Fields]

  protected def isPoundsAndPence(): Boolean = settings.POUNDS_AND_PENCE
  protected def penceP1DB(): (String, Mapping[BigDecimal])= P1_DB_KEY -> poundsAndPenceField(true)
  protected def poundsP1DB(): (String, Mapping[Int])= P1_DB_KEY -> poundsField(true)
  protected def penceP2DB(): (String, Mapping[BigDecimal])= P2_DB_KEY -> poundsAndPenceField(true)
  protected def poundsP2DB(): (String, Mapping[Int])= P2_DB_KEY -> poundsField(true)
  protected def penceP1DC(): (String, Mapping[BigDecimal])= P1_DC_KEY -> poundsAndPenceField(true)
  protected def poundsP1DC(): (String, Mapping[Int])= P1_DC_KEY -> poundsField(true)
  protected def penceP2DC(): (String, Mapping[BigDecimal])= P2_DC_KEY -> poundsAndPenceField(true)
  protected def poundsP2DC(): (String, Mapping[Int])= P2_DC_KEY -> poundsField(true)
}

trait Year2015Form {
  def form(isDB: Boolean, isDC: Boolean): Form[_ <: Year2015Fields] = (isDB, isDC) match {
    case (true,true) => Year2015DBDCForm()
    case (true, false) => Year2015DefinedBenefitForm()
    case _ => Year2015DefinedContributionForm()
  }
}

object Year2015Form extends Year2015Form

