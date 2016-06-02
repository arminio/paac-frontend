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

package controllers

import connector.CalculatorConnector
import form._
import play.api.mvc._
import service._
import uk.gov.hmrc.play.http.HeaderCarrier
import models._
import scala.concurrent.Future

object ReviewTotalAmountsController extends ReviewTotalAmountsController {
  override val keystore: KeystoreService = KeystoreService
  override val connector: CalculatorConnector = CalculatorConnector
}

trait ReviewTotalAmountsController extends RedirectController with models.ThisYear {
  settings: models.ThisYear =>

  val keystore: KeystoreService
  val connector: CalculatorConnector

  def fetchAmounts()(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[Map[String,String]] = {
    def yearAmountKeys(year: Int) : List[String] = year match {
      case y if y < 2015 =>
        List(KeystoreService.DB_PREFIX + y, KeystoreService.DC_PREFIX + y)
      case y if y == 2015 =>
        List(KeystoreService.P1_DB_KEY,
             KeystoreService.P1_DC_KEY,
             KeystoreService.P2_DB_KEY,
             KeystoreService.P2_DC_KEY,
             KeystoreService.P1_TRIGGER_DC_KEY,
             KeystoreService.P2_TRIGGER_DC_KEY)
      case y if y > 2015 =>
        List(KeystoreService.DB_PREFIX + y, KeystoreService.DC_PREFIX + y, KeystoreService.TH_PREFIX + y,
          KeystoreService.AI_PREFIX + y, KeystoreService.TA_PREFIX + y)
      }

    keystore.read[String](List.range(2008, settings.THIS_YEAR + 1).flatMap(yearAmountKeys(_)))
  }

  val onPageLoad = withSession { implicit request =>
    fetchAmounts.flatMap { (amountsMap) =>
      keystore.read[String](KeystoreService.TRIGGER_DATE_KEY).map {
        (td) =>
        keystore.store(false.toString(), KeystoreService.IS_EDIT_KEY)
        val values = amountsMap ++ Map((KeystoreService.TRIGGER_DATE_KEY, td.getOrElse("")))
        val f = CalculatorForm.bind(values, true)
        val model = f.get
        val c = model.toContributions.find((c)=>c.amounts != None && c.amounts.get.triggered != None && c.amounts.get.triggered.get == true)
        CalculatorForm.bind(values).fold(
          formWithErrors => {
            Ok(views.html.review_amounts(formWithErrors, model.hasDefinedBenefits(), model.hasDefinedContributions(), model.hasTriggerDate(), c))
          },
          form => {
            Ok(views.html.review_amounts(f, model.hasDefinedBenefits(), model.hasDefinedContributions(), model.hasTriggerDate(), c))
          }
        )
      }
    }
  }

  def onEditAmount(year:Int) = withSession { implicit request =>
    keystore.store(true.toString(), KeystoreService.IS_EDIT_KEY)
    keystore.read(List(KeystoreService.TE_YES_NO_KEY)).flatMap {
      (fieldsMap) =>
      goTo(year, false, true, fieldsMap(KeystoreService.TE_YES_NO_KEY) == "Yes", Redirect(routes.ReviewTotalAmountsController.onPageLoad))
    }
  }

  val onSubmit = withSession { implicit request =>
    def fetchTriggered(l:List[TaxYearResults]):Option[TaxYearResults] = l.find(_.input.isTriggered)
    def fetchNotTriggered(l:List[TaxYearResults]):Option[TaxYearResults] = l.find(!_.input.isTriggered)
    fetchAmounts().flatMap { (amounts) =>
      keystore.read[String](KeystoreService.TRIGGER_DATE_KEY).flatMap {
        (td) =>
        val values = amounts ++ Map((KeystoreService.TRIGGER_DATE_KEY, td.getOrElse("")))
        CalculatorForm.bind(values).fold(
          formWithErrors => {
            val f = CalculatorForm.bind(amounts, true)
            val model = f.get
            val c = model.toContributions.find((c)=>c.amounts != None && c.amounts.get.triggered != None && c.amounts.get.triggered.get == true)
            Future.successful(Ok(views.html.review_amounts(formWithErrors, model.hasDefinedBenefits(),
              model.hasDefinedContributions(), model.hasTriggerDate(), c)))
          },
          input => {
            val contributions = input.toContributions()
            connector.connectToPAACService(contributions).flatMap{
              response =>
              keystore.read[String](List(KeystoreService.DB_FLAG, KeystoreService.DC_FLAG)).flatMap {
                (fieldMap) =>
                // TO DO move to paac backend service
                val triggerAmountRow = response.find(_.input.isTriggered)
                val results = if (triggerAmountRow.isDefined) {
                  val year2015ResultsMap = response.groupBy(_.input.taxPeriodStart.year)(2015).groupBy(_.input.isPeriod1)
                  val period1Results = year2015ResultsMap.get(true).get
                  val period2Results = year2015ResultsMap.get(false).get
                  val non2015Results = response.filterNot((t)=>t.input.isPeriod1||t.input.isPeriod2)
                  val results: List[TaxYearResults] = if (period1Results.size == 2) {
                    val p1Triggered = fetchTriggered(period1Results).get
                    val p1NotTriggered = fetchNotTriggered(period1Results).get
                    val newP1 = p1Triggered.copy(input=p1NotTriggered.input)
                    non2015Results ++ List(newP1) ++ List(fetchTriggered(period2Results).get)
                  } else if (period2Results.size == 2) {
                    val p2Triggered = fetchTriggered(period2Results).get
                    val p2NotTriggered = fetchNotTriggered(period2Results).get
                    val newP2 = p2Triggered.copy(input=p2NotTriggered.input)
                    non2015Results ++ List(fetchNotTriggered(period1Results).get) ++ List(newP2)
                  } else {
                    response
                  }
                  results
                } else {
                  response
                }
                Future.successful(Ok(views.html.results(results, fieldMap(KeystoreService.DB_FLAG).toBoolean, fieldMap(KeystoreService.DC_FLAG).toBoolean)))
              }
            }
          }
        )
      }
    }
  }

  val onBack = withSession { implicit request =>
    wheretoBack(Redirect(routes.PensionInputsController.onPageLoad))
  }
}
