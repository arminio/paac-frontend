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
import service.KeystoreService._
import play.api.mvc._
import scala.concurrent.Future
import form._
import play.api.data.Form
import play.api.mvc.Request


object AdjustedIncome1617InputController extends AdjustedIncome1617InputController {
  def keystore: KeystoreService = KeystoreService
}

trait AdjustedIncome1617InputController extends RedirectController {

  val onPageLoad = withReadSession { implicit request =>
    val cy = request.data int CURRENT_INPUT_YEAR_KEY
    val isEdit = request.form bool IS_EDIT_KEY
    if (cy <= 2015 || cy == -1 ) {
      CheckYourAnswers() go Edit
    } else {
      val form = AIForm.form(cy).bind(request.data).discardingErrors
      Future.successful(Ok(views.html.adjusted_income_1617_input(form, cy.toString, isEdit)))
    }
  }

  val onSubmit = withWriteSession { implicit request =>

    val cy = request.form("year").toInt
    val isEdit = request.form bool IS_EDIT_KEY
    val form = AIForm.form(cy).bindFromRequest()

    form.fold(
      formWithErrors => showPage(formWithErrors, cy.toString, isEdit),
      input => AdjustedIncome() go Forward.using(request.data ++ input.data(cy))
    )
  }

  val onBack = withSession { implicit request =>
    AdjustedIncome() go Backward
  }

  protected def showPage(form: Form[_ <: AIFields], year: String, isEditing: Boolean)(implicit request: Request[Any]) = {
    Future.successful(Ok(views.html.adjusted_income_1617_input(form, year, isEditing)))
  }
}
