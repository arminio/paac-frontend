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
import play.api.data.Mapping
import service.KeystoreService._

trait Post2015Fields {
  def data(year: Int): Map[String,String] = toMap(this).map{
    (entry)=>
    (s"${entry._1}_${year}", (entry._2.toString.toDouble*100).floor.toLong.toString)
  }
}

trait Post2015FormFactory extends Settings {
  settings: Settings =>

  def apply(year: Int): Form[_ <: Post2015Fields]

  protected def isPoundsAndPence(): Boolean = settings.POUNDS_AND_PENCE
  protected def poundsDB(year: Long): (String, Mapping[Long])= (DB_PREFIX + year) -> poundsLongField(true)
  protected def penceDB(year: Long): (String, Mapping[BigDecimal])= (DB_PREFIX + year) -> poundsAndPenceField(true)
  protected def poundsDC(year: Long): (String, Mapping[Long])= (DC_PREFIX + year) -> poundsLongField(true)
  protected def penceDC(year: Long): (String, Mapping[BigDecimal])= (DC_PREFIX + year) -> poundsAndPenceField(true)
}

trait Post2015Form {
  def form(isDB: Boolean, isDC: Boolean, year: Int): Form[_ <: Post2015Fields] = (isDB, isDC) match {
    case (true,true) => Post2015DBDCForm(year)
    case (true, false) => Post2015DefinedBenefitForm(year)
    case _ => Post2015DefinedContributionForm(year)
  }
}

object Post2015Form extends Post2015Form

