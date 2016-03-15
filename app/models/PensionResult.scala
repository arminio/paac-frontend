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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, JsPath, Writes}

sealed trait Result
sealed trait PensionCalculationResult extends Result

case class TaxYearResults(input: Contribution,
                          summaryResult: SummaryResult) extends PensionCalculationResult
case class SummaryResult(chargableAmount: Long = 0,
                         exceedingAAAmount: Long = 0) extends PensionCalculationResult

object SummaryResult {
  implicit val summaryResultWrites: Writes[SummaryResult] = (
    (JsPath \ "chargableAmount").write[Long] and
      (JsPath \ "exceedingAAAmount").write[Long]
    )(unlift(SummaryResult.unapply))

  implicit val summaryResultReads: Reads[SummaryResult] = (
    (JsPath \ "chargableAmount").read[Long] and
      (JsPath \ "exceedingAAAmount").read[Long]
    )(SummaryResult.apply _)
}

object TaxYearResults {
  implicit val summaryWrites: Writes[TaxYearResults] = (
    (JsPath \ "input").write[Contribution] and
      (JsPath \ "summaryResult").write[SummaryResult]
    )(unlift(TaxYearResults.unapply))

  implicit val summaryReads: Reads[TaxYearResults] = (
    (JsPath \ "input").read[Contribution] and
      (JsPath \ "summaryResult").read[SummaryResult]
    )(TaxYearResults.apply _)
}
