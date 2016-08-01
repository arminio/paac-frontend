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
  def keystore: KeystoreService = KeystoreService
}

trait AdjustedIncome1617InputController extends RedirectController {

  val onPageLoad = withReadSession { implicit request =>
    val cy = request.data int CURRENT_INPUT_YEAR_KEY
    if (cy <= 2015 || cy == -1 ) {
      CheckYourAnswers() go Edit
    } else {
      val form = CalculatorForm.bind(request.data).discardingErrors
      Future.successful(Ok(views.html.adjusted_income_1617_input(form, cy.toString, (request.data bool IS_EDIT_KEY))))
    }
  }

  val onSubmit = withWriteSession { implicit request =>
    val data = request.form
    val cy = data("year").toInt
    val isEdit = data("isEdit").toBoolean
    CalculatorForm.form.bindFromRequest().fold (
      formWithErrors =>
        Future.successful(Ok(views.html.adjusted_income_1617_input(formWithErrors, cy.toString, isEdit))),
      input => {
        input.toAdjustedIncome(cy) match {
          case None => {
            var form = CalculatorForm.form.bindFromRequest()
            form = form.withError(s"adjustedIncome.amount_${cy}", "ai.error.bounds")
            Future.successful(Ok(views.html.adjusted_income_1617_input(form, cy.toString, isEdit)))
          }
          case Some(value) => {
            val sessionData = request.data ++ Map(value.swap).mapValues(_.toString)
            AdjustedIncome() go Forward.using(sessionData)
          }
        }
      }
    )
  }

  val onBack = withSession { implicit request =>
    AdjustedIncome() go Backward
  }
}
