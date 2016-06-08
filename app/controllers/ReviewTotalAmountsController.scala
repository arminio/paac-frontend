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
import service.KeystoreService._
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
        List(DB_PREFIX + y, DC_PREFIX + y)
      case y if y == 2015 =>
        List(P1_DB_KEY,
             P1_DC_KEY,
             P2_DB_KEY,
             P2_DC_KEY,
             P1_TRIGGER_DC_KEY,
             P2_TRIGGER_DC_KEY)
      case y if y > 2015 =>
        List(DB_PREFIX + y, DC_PREFIX + y, TH_PREFIX + y,
          AI_PREFIX + y, TA_PREFIX + y)
      }

    keystore.read[String](List.range(2008, settings.THIS_YEAR + 1).flatMap(yearAmountKeys(_)))
  }

  val onPageLoad = withSession { implicit request =>
    fetchAmounts.flatMap { (amountsMap) =>
      keystore.read[String](TRIGGER_DATE_KEY).flatMap {
        (td) =>
        val result = keystore.save[String](List((Some(false.toString()), IS_EDIT_KEY), (Some("-1"), CURRENT_INPUT_YEAR_KEY)), "").map {
          (_) =>
          val values = amountsMap ++ Map((TRIGGER_DATE_KEY, td.getOrElse("")))
          val f = CalculatorForm.bind(values, true)
          val model = f.get
          val c = model.toPageValues.reverse.find(_.isTriggered)
          CalculatorForm.bind(values).fold(
            formWithErrors => {
              Ok(views.html.review_amounts(formWithErrors, model.hasDefinedBenefits(), model.hasDefinedContributions(), model.hasTriggerDate(), c))
            },
            form => {
              Ok(views.html.review_amounts(f, model.hasDefinedBenefits(), model.hasDefinedContributions(), model.hasTriggerDate(), c))
            }
          )
        }
        result
      }
    }
  }

  def onEditAmount(year:Int) = withSession { implicit request =>
    keystore.store(true.toString(), IS_EDIT_KEY)
    keystore.read(List(TE_YES_NO_KEY)).flatMap {
      (fieldsMap) =>
      goTo(year, false, true, fieldsMap(TE_YES_NO_KEY) == "Yes", Redirect(routes.ReviewTotalAmountsController.onPageLoad))
    }
  }

  val onSubmit = withSession { implicit request =>
    fetchAmounts().flatMap { (amounts) =>
      keystore.read[String](TRIGGER_DATE_KEY).flatMap {
        (td) =>
        val values = amounts ++ Map((TRIGGER_DATE_KEY, td.getOrElse("")))
        CalculatorForm.bind(values).fold(
          formWithErrors => {
            val f = CalculatorForm.bind(amounts, true)
            val model = f.get
            val c = model.toPageValues.reverse.find(_.isTriggered)
            Future.successful(Ok(views.html.review_amounts(formWithErrors, model.hasDefinedBenefits(),
              model.hasDefinedContributions(), model.hasTriggerDate(), c)))
          },
          input => {
            val contributions = input.toContributions()
            connector.connectToPAACService(contributions).flatMap{
              response =>
              keystore.read[String](List(DB_FLAG, DC_FLAG)).flatMap {
                (fieldMap) =>
                Future.successful(Ok(views.html.results(response, fieldMap(DB_FLAG).toBoolean, fieldMap(DC_FLAG).toBoolean)))
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
