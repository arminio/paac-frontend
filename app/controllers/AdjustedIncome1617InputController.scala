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

import service._
import play.api.mvc._

import scala.concurrent.Future
import form.CalculatorForm
import service.KeystoreService._


object AdjustedIncome1617InputController extends AdjustedIncome1617InputController {
  override val keystore: KeystoreService = KeystoreService
}

trait AdjustedIncome1617InputController extends RedirectController {
  val keystore: KeystoreService

  private val onSubmitRedirect = routes.ReviewTotalAmountsController.onPageLoad

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](List(CURRENT_INPUT_YEAR_KEY)).flatMap {
      (cyMap) =>
        val cy = cyMap int CURRENT_INPUT_YEAR_KEY
        if (cy <= 2015 || cy == -1 )
          Future.successful(Redirect(onSubmitRedirect))
        else {
          keystore.read[String](List(AI_PREFIX + cy,IS_EDIT_KEY)).map {
            (fieldsMap) =>
            Ok(views.html.adjusted_income_1617_input(CalculatorForm.bind(fieldsMap).discardingErrors, cy.toString, (fieldsMap bool IS_EDIT_KEY)))
          }
        }
    }
  }

  val onSubmit = withSession { implicit request =>
    val data = formRequestData
    val cy = data("year").toInt
    val isEdit = data("isEdit").toBoolean
    CalculatorForm.form.bindFromRequest ().fold (
      formWithErrors =>
        Future.successful (Ok (views.html.adjusted_income_1617_input(formWithErrors, cy.toString, isEdit))),
      input => {
        val isAIError = !input.toAdjustedIncome(cy).isDefined
        if (isAIError) {
          var form = CalculatorForm.form.bindFromRequest()
          form = form.withError("adjustedIncome.amount_"+cy, "ai.error.bounds")
          Future.successful(Ok(views.html.adjusted_income_1617_input(form, cy.toString, isEdit)))
        } else
          keystore.save(List(input.toAdjustedIncome(cy)), "").flatMap((_)=>AdjustedIncome() go Forward)
      }
    )
  }

  val onBack = withSession { implicit request =>
    AdjustedIncome() go Backward
  }
}
