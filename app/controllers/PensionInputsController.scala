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

import form.CalculatorForm
import service._
import models._
import service.KeystoreService._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import scala.concurrent.Future
import play.api.mvc.Request

object PensionInputsController extends PensionInputsController {
  // $COVERAGE-OFF$
  def keystore: KeystoreService = KeystoreService
  // $COVERAGE-ON$
}

trait PensionInputsController extends RedirectController {

  val onPageLoad = withReadSession { implicit request =>
    val cy = request.data(CURRENT_INPUT_YEAR_KEY)
    if (cy.isEmpty || cy.toInt <= 0)
      Start() go Edit
    else
      showPage(CalculatorForm.bind(request.data).discardingErrors, cy, request.data bool IS_EDIT_KEY)
  }

  val onSubmit = withWriteSession { implicit request =>
    val cy = request.form("year")
    val isEdit = request.form bool IS_EDIT_KEY
    val form = CalculatorForm.form.bindFromRequest()

    form.fold(
      formWithErrors =>
        showPage(formWithErrors, cy, isEdit),
      fields => {
        val data = fields.toDefinedBenefit(cy.toInt)
        data match {
          case None => showPage(form.withError(s"definedBenefits.amount_${cy}", "db.error.bounds"), cy, isEdit)
          case Some(value) => {
            val sessionData = request.data ++ Map(value.swap).mapValues(_.toString)
            PensionInput() go Forward.using(sessionData)
          }
        }
      }
    )
  }

  val onBack = withWriteSession { implicit request =>
    PensionInput() go Backward
  }

  protected def showPage(form: Form[CalculatorFormFields], year: String, isEditing: Boolean)(implicit request: Request[Any]) = {
    Future.successful(Ok(views.html.pensionInputs(form, year, isEditing)))
  }
}
