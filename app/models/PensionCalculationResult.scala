
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
