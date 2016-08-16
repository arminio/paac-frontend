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
import play.api.data.Mapping
import service.KeystoreService._
import config.Settings

trait AIFields {
  def data(year: Int): Map[String,String] = toMap(this).map{
    (entry)=>
    (s"${entry._1}_${year}", (entry._2.toString.toDouble*100).floor.toInt.toString)
  }
}

trait AIFormFactory extends Settings {
  settings: Settings =>

  def apply(year: Int): Form[_ <: AIFields]

  protected def isPoundsAndPence(): Boolean = settings.POUNDS_AND_PENCE
  protected def poundsDB(year: Int): (String, Mapping[Int])= (AI_PREFIX+year) -> poundsField(true)
  protected def penceDB(year: Int): (String, Mapping[BigDecimal])= (AI_PREFIX+year) -> poundsAndPenceField(true)
}

trait AIForm {
  def form(year: Int): Form[_ <: AIFields] = AdjustedIncomeForm(year)
}

object AIForm extends AIForm

