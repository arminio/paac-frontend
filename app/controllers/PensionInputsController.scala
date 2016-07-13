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

object PensionInputsController extends PensionInputsController {
  override val keystore: KeystoreService = KeystoreService
}

trait PensionInputsController extends RedirectController {
  val keystore: KeystoreService

  protected def show(form: Form[CalculatorFormFields], year: Int, isEditing: Boolean)(implicit request: Request[Any]) = {
    Ok(views.html.pensionInputs(form, year.toString, isEditing))
  }

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](List(CURRENT_INPUT_YEAR_KEY,IS_EDIT_KEY)).flatMap {
      (fieldsMap) =>
      val cy = fieldsMap(CURRENT_INPUT_YEAR_KEY)
      if (cy.isEmpty || cy.toInt <= 0)
        Start() go Edit
      else
        keystore.read[String](List((DB_PREFIX + cy),(DC_PREFIX + cy))).map((v)=>show(CalculatorForm.bind(v).discardingErrors, cy.toInt, (fieldsMap bool IS_EDIT_KEY)))
    }
  }

  val onSubmit = withSession { implicit request =>
    val data = formRequestData
    val cy = data("year").toInt
    val form = CalculatorForm.form.bindFromRequest()
    form.fold(
      formWithErrors =>
        Future.successful(show(formWithErrors, cy, data("isEdit").toBoolean)),
      fields => {
        if (!fields.toDefinedBenefit(cy).isDefined)
          Future.successful(show(form.withError("definedBenefits.amount_"+cy, "db.error.bounds"), 
                                 cy, 
                                 data("isEdit").toBoolean))
        else
          keystore.save(List(fields.toDefinedBenefit(cy)), "").flatMap(_=>PensionInput() go Forward)
      }
    )
  }

  val onBack = withSession { implicit request =>
    PensionInput() go Backward
  }
}
