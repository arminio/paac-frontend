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
import play.api.data.Mapping

// scalastyle:off magic.number
package object utilities {
  val DEFAULT_MAX = 5000000

  def poundsAndPenceField(isValidating: Boolean = false) = {
    val field = bigDecimal(10,2)
    if (isValidating)
          field.verifying("errorbounds", value=> value.longValue >= 0 && value.longValue <= DEFAULT_MAX)
    else
      field
  }

  def poundsField(isValidating: Boolean = false) = {
    if (isValidating)
       number(min=0,max=DEFAULT_MAX)
     else
       number
  }

  def toMap(caseClassInstance: AnyRef) =
    (Map[String,Any]() /: caseClassInstance.getClass.getDeclaredFields) {
      (a,f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(caseClassInstance))
    }

  implicit def intTuple4ToBigDecimal(maybeTuple: Option[(Int,Int,Int,Int)]): Option[(BigDecimal,BigDecimal,BigDecimal,BigDecimal)] = {
    maybeTuple.map {
      (t) =>
      (BigDecimal(t._1),BigDecimal(t._2),BigDecimal(t._3),BigDecimal(t._4))
    }
  }

  implicit def bigDecimalTuple4ToInt(maybeTuple: Option[(BigDecimal,BigDecimal,BigDecimal,BigDecimal)]): Option[(Int,Int,Int,Int)] = {
    maybeTuple.map {
      (t) =>
      (t._1.intValue,t._2.intValue,t._3.intValue,t._4.intValue)
    }
  }

  implicit def bigDecimalTuple2ToInt(maybeTuple: Option[(BigDecimal,BigDecimal)]): Option[(Int,Int)] = {
    maybeTuple.map {
      (t) =>
      (t._1.intValue,t._2.intValue)
    }
  }

  implicit def bigDecimalToInt(maybeDecimal: Option[BigDecimal]): Option[Int] = {
    maybeDecimal.map {
      (v) =>
      (v.intValue)
    }
  }
}