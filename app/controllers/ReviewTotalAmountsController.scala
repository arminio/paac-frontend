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

import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}

import play.api.mvc._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import form._
import service._
import connector.CalculatorConnector

object ReviewTotalAmountsController extends ReviewTotalAmountsController {
  override val keystore: KeystoreService = KeystoreService
  override val connector: CalculatorConnector = CalculatorConnector
}

trait ReviewTotalAmountsController extends BaseFrontendController {
  val keystore: KeystoreService
  val connector: CalculatorConnector

  def fetchAmounts()(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[Map[String,String]] = {
    def yearAmountKeys(year: Int) : List[String] = year match {
      case y if y < 2015 =>
        List(KeystoreService.DB_PREFIX+y)
      case y if y == 2015 => 
        List(KeystoreService.P1_DB_KEY, 
             KeystoreService.P1_DC_KEY, 
             KeystoreService.P2_DB_KEY, 
             KeystoreService.P2_DC_KEY, 
             KeystoreService.P1_TRIGGER_DC_KEY, 
             KeystoreService.P2_TRIGGER_DC_KEY)
      case y if y > 2015 => 
        List(KeystoreService.DB_PREFIX+y, KeystoreService.DC_PREFIX+y, KeystoreService.TH_PREFIX+y, KeystoreService.AI_PREFIX+y, KeystoreService.TA_PREFIX+y)
      }

    val currentYear = config.PaacConfiguration.year()
    keystore.read[String](List.range(2006, currentYear+1).flatMap(yearAmountKeys(_)))
  }

  val onPageLoad = withSession { implicit request =>
    fetchAmounts.flatMap { (amountsMap) =>
      keystore.read[String](KeystoreService.TRIGGER_DATE_KEY).map {
        (td) =>
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

  def onEditAmount(year:Long) = withSession { implicit request =>
    if (year < 2015) {
      Future.successful(Results.Redirect(routes.PensionInputsController.onPageLoad()))
    } else if (year == 20151) {
      Future.successful(Results.Redirect(routes.PensionInputs1516Period1Controller.onPageLoad()))
    } else if (year == 20152) {
      Future.successful(Results.Redirect(routes.PensionInputs1516Period2Controller.onPageLoad()))
    } else {
      Future.successful(Results.Redirect(routes.ReviewTotalAmountsController.onPageLoad()))
    }
  }

  val onSubmit = withSession { implicit request =>
    fetchAmounts().flatMap { (amounts) =>
      keystore.read[String](KeystoreService.TRIGGER_DATE_KEY).flatMap {
        (td) =>
        val values = amounts ++ Map((KeystoreService.TRIGGER_DATE_KEY, td.getOrElse("")))
        CalculatorForm.bind(values).fold(
          formWithErrors => {
            val f = CalculatorForm.bind(amounts, true)
            val model = f.get
            val c = model.toContributions.find((c)=>c.amounts != None && c.amounts.get.triggered != None && c.amounts.get.triggered.get == true)
            Future.successful(Ok(views.html.review_amounts(formWithErrors,  model.hasDefinedBenefits(), model.hasDefinedContributions(), model.hasTriggerDate(), c)))
          },
          input => {
            val contributions = input.toContributions()
            connector.connectToPAACService(contributions).map(response => Ok(views.html.results(response)))
          }
        )
      }
    }
  }
}
