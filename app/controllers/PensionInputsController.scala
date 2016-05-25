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
import service.KeystoreService._
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
    keystore.read[String](CURRENT_INPUT_YEAR_KEY).flatMap {
      (currentYear) =>
      val cy = currentYear.getOrElse("2014")
      if (cy == "2015" || cy == "-1") {
        Future.successful(Redirect(onSubmitRedirect))
      } else {
        keystore.read[String](List((DB_PREFIX + cy),(DC_PREFIX + cy), DB_FLAG, DC_FLAG)).map {
          (fieldsMap) =>
            Ok(views.html.pensionInputs(CalculatorForm.bind(fieldsMap).discardingErrors, cy,
                                        fieldsMap(KeystoreService.DB_FLAG).toBoolean,
                                        fieldsMap(KeystoreService.DC_FLAG).toBoolean))
        }
      }
    }
  }

  val onSubmit = withSession { implicit request =>
    implicit val marshall = KeystoreService.toStringPair _
    keystore.read[String](List(CURRENT_INPUT_YEAR_KEY, DB_FLAG, DC_FLAG)).flatMap {
      (fieldsMap) =>
      val cy = fieldsMap(CURRENT_INPUT_YEAR_KEY).toInt
      CalculatorForm.form.bindFromRequest().fold(
        formWithErrors => { Future.successful(Ok(views.html.pensionInputs(formWithErrors, 
                                                                          cy.toString(),
                                                                          fieldsMap(DB_FLAG).toBoolean,
                                                                          fieldsMap(DC_FLAG).toBoolean))) },
        input => {
          keystore.save(List(input.toDefinedBenefit(cy), input.toDefinedContribution(cy)), "").flatMap {
            (_)=>
            wheretoNext[String]( Redirect(onSubmitRedirect))
          }
        }
      )
    }
  }

  val onBack = withSession { implicit request =>
    wheretoBack[String](Redirect(routes.TaxYearSelectionController.onPageLoad))
  }
}
