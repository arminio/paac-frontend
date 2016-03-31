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
import scala.concurrent.Future
import form._
import service._
import controllers.routes
import connector.CalculatorConnector

object ReviewTotalAmountsController extends ReviewTotalAmountsController {
  override val keystore: KeystoreService = KeystoreService
  override val connector: CalculatorConnector = CalculatorConnector
}

trait ReviewTotalAmountsController extends BaseFrontendController {
  val keystore: KeystoreService
  val connector: CalculatorConnector

  private def fetchAmounts()(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[Map[String,String]] = {
    def fetchAmount(key: String, defaultAmount: String) : Future[(String,String)] = keystore.read[String](key).map { (amount) =>
        amount match {
          case None => (key, defaultAmount)
          case Some(value) => (key, value)
        }
      }
    def fetchYearAmounts(year: Int) : List[Future[(String,String)]] = year match {
      case y if y < 2015 =>
        List(fetchAmount("definedBenefit_"+y, "0.00"))
      case y if y == 2015 => 
        List("definedBenefit_"+y, "moneyPurchase_"+y).map(fetchAmount(_, "0.00"))
      case y if y >= 2015 => 
        List("definedBenefit_"+y, "moneyPurchase_"+y, "thresholdIncome_"+y, "adjustedIncome_"+y).map(fetchAmount(_, "0.00"))
      }

    val amounts : Future[List[(String,String)]] = Future.sequence(List.range(2006, 2050).flatMap(fetchYearAmounts(_)))
    amounts.map(_.toMap)
  }

  val onPageLoad = withSession { implicit request =>
    fetchAmounts().map { (amountsMap) =>
      CalculatorForm.form.bind(amountsMap).fold(
        formWithErrors => Ok(views.html.review_amounts(formWithErrors)) ,
        form => Ok(views.html.review_amounts(CalculatorForm.form.bind(amountsMap)))
      )
    }
  }

  val onSubmit = withSession { implicit request =>
    fetchAmounts().flatMap { (amounts) =>
      CalculatorForm.form.bind(amounts).fold(
        formWithErrors => Future.successful(Ok(views.html.review_amounts(formWithErrors))),
        input => connector.connectToPAACService(input.toContributions()).map(response => Ok(views.html.results(response)))
      )
    }
  }
}

