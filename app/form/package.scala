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

import play.api.data.Forms._
import play.api.data._
import play.api.data.validation._
import play.api.data.format._
import scala.util.matching.Regex

// scalastyle:off magic.number
package object utilities {
  val DEFAULT_MAX = 5000000

  def poundsAndPenceField(isValidating: Boolean = false) = {
    implicit val binder:Formatter[BigDecimal] = bigDecimalFormatter(Some((10,2)))
    val field = Forms.of[BigDecimal] as binder
    if (isValidating)
          field.verifying("errorbounds", value=> value.longValue >= 0 && value.longValue <= DEFAULT_MAX)
    else
      field
  }

  def poundsField(isValidating: Boolean = false) = {
    implicit val binder:Formatter[Int] = numberFormatter(_.toInt)
    if (isValidating)
      Forms.of[Int] verifying (Constraints.min(0, false), Constraints.max(DEFAULT_MAX, false))
     else
      Forms.of[Int]
  }

  def poundsLongField(isValidating: Boolean = false) = {
    implicit val binder:Formatter[Long] = numberFormatter(_.toLong)
    if (isValidating)
      Forms.of[Long].verifying(x => x > 0 && x <= DEFAULT_MAX)
     else
      Forms.of[Long]
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

  implicit def bigDecimalToLong(maybeDecimal: Option[BigDecimal]): Option[Long] = {
    maybeDecimal.map {
      (v) =>
      (v.longValue)
    }
  }

  private def parsing[T](parse: String => T, Pattern: Regex, handleError: (String, Throwable) => Seq[FormError])(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
    Formats.stringFormat.bind(key, data).right.flatMap { s =>
      s match {
        case "" => {
          Left(Seq[FormError](FormError(key, "error.required", Nil)))
        }
        case Pattern(numberString) => {
          scala.util.control.Exception.allCatch[T]
          .either(parse(numberString))
          .left.map(handleError(key,_))
        }
        case _ => {
          Left(Seq[FormError](FormError(key, "error.number", Nil)))
        }
      }
    }
  }

  private def numberFormatter[T](convert: String => T): Formatter[T] = {
    val errorHandler: (String, Throwable) => Seq[FormError] = (key, e) => Seq(FormError(key, "error.number", Nil))
    new Formatter[T] {
      override val format = Some("format.numeric" -> Nil)
      def bind(key: String, data: Map[String, String]) = parsing(convert, "(-*\\d+)".r, errorHandler)(key, data)
      def unbind(key: String, value: T) = Map(key -> value.toString)
    }
  }

  private def bigDecimalFormatter(precision: Option[(Int, Int)]): Formatter[BigDecimal] = {
    val errorHandler: (String, Throwable) => Seq[FormError] = (key, e) => Seq(
              precision match {
                case Some((p, s)) => FormError(key, "error.real.precision", Seq(p, s))
                case None => FormError(key, "error.real", Nil)
              }
            )
    val convert: String => BigDecimal = s => {
      val bd = BigDecimal(s)
      precision.map({
        case (p, s) =>
          if (bd.precision - bd.scale > p - s) {
            throw new java.lang.ArithmeticException("Invalid precision")
          }
          bd.setScale(s)
      }).getOrElse(bd)
    }

    new Formatter[BigDecimal] {
      override val format = Some("format.real" -> Nil)
      def bind(key: String, data: Map[String, String]) = parsing(convert, "(-*[\\d.]+)".r, errorHandler)(key, data)
      def unbind(key: String, value: BigDecimal) = Map(key -> precision.map({ p => value.setScale(p._2) }).getOrElse(value).toString)
    }
  }
}