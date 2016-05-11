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

import service.KeystoreService
import play.api.mvc._
import scala.concurrent.Future
import form.CalculatorForm

object PensionInputsController extends PensionInputsController {
  override val keystore: KeystoreService = KeystoreService
}

trait PensionInputsController extends RedirectController {
  val keystore: KeystoreService

  private val onSubmitRedirect = routes.ReviewTotalAmountsController.onPageLoad

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](KeystoreService.CURRENT_INPUT_YEAR_KEY).flatMap {
      (currentYear) =>
      val cy = currentYear.getOrElse("2014")
      if (cy == "2015" || cy == "-1") {
        Future.successful(Redirect(onSubmitRedirect))
      } else {
        val dbKeyStoreKey = KeystoreService.DB_PREFIX + cy
        val dcKeyStoreKey = KeystoreService.DC_PREFIX + cy
        keystore.read[String](List(dbKeyStoreKey, dcKeyStoreKey, KeystoreService.DB_FLAG, KeystoreService.DC_FLAG)).map {
          (fieldsMap) =>
            Ok(views.html.pensionInputs(CalculatorForm.form.bind(fieldsMap).discardingErrors, cy,
                                        fieldsMap(KeystoreService.DB_FLAG).toBoolean,
                                        fieldsMap(KeystoreService.DC_FLAG).toBoolean))
        }
      }
    }
  }

  val onSubmit = withSession { implicit request =>
    keystore.read[String](KeystoreService.CURRENT_INPUT_YEAR_KEY).flatMap {
      (currentYear) =>
      val cy = currentYear.getOrElse("2014")
      CalculatorForm.form.bindFromRequest().fold(
        formWithErrors => { Future.successful(Ok(views.html.pensionInputs(formWithErrors, cy))) },
        input => {
          val keyStoreKey = KeystoreService.DB_PREFIX+cy
          val (amount:Long, key:String) = input.toDefinedBenefit(cy.toInt).getOrElse((keyStoreKey, 0L))
          keystore.store[String](amount.toString, key).flatMap{
            (_) =>
            wheretoNext[String]( Redirect(onSubmitRedirect))
          }
        }
      )
    }
  }
}
