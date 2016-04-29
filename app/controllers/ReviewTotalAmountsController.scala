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
    def fetchAmount(key: String) : Future[Option[(String,String)]] = keystore.read[String](key).map { (amount) =>
        amount match {
          case None => None
          case Some("0") => Some((key, "0.00"))
          case Some(value) => Some((key, f"${(value.toInt/100.00)}%2.2f"))
        }
      }
    def fetchYearAmounts(year: Int) : List[Future[Option[(String,String)]]] = year match {
      case y if y < 2015 =>
        List("definedBenefit_"+y).map(fetchAmount(_))
      case y if y == 2015 => 
        List("definedBenefit_2015_p1", "definedBenefit_2015_p2", "definedContribution_2015_p1", "definedContribution_2015_p2").map(fetchAmount(_))
      case y if y > 2015 => 
        List("definedBenefit_"+y, "definedContribution_"+y, "thresholdIncome_"+y, "adjustedIncome_"+y, "taperedAllowance_"+y).map(fetchAmount(_))
      }

    val currentYear = (new java.util.GregorianCalendar()).get(java.util.Calendar.YEAR)
    val amounts : Future[List[Option[(String,String)]]] = Future.sequence(List.range(2006, currentYear+1).flatMap(fetchYearAmounts(_)))
    amounts.map{ 
      (maybeYearAmountTuples: List[Option[(String,String)]])  =>
      maybeYearAmountTuples.filter(_ != None).map(_.head).toMap
    }
  }

  val onPageLoad = withSession { implicit request =>
    fetchAmounts().map { (amountsMap) =>
      CalculatorForm.form.bind(amountsMap).fold(
        formWithErrors => Ok(views.html.review_amounts(formWithErrors, true, true)),
        form => {
          val f = CalculatorForm.form.bind(amountsMap)
          Ok(views.html.review_amounts(f, f.get.hasDefinedBenefits(), f.get.hasDefinedContributions()))
        }
      )
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
      CalculatorForm.form.bind(amounts).fold(
        formWithErrors => {
          Future.successful(Ok(views.html.review_amounts(formWithErrors, true, true)))
        },
        input => connector.connectToPAACService(input.toContributions()).map(response => Ok(views.html.results(response)))
      )
    }
  }
}

