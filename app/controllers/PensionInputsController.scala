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
      showPage(Pre2015Form.form(cy.toInt).bind(convert(request.data)).discardingErrors, cy, request.data bool IS_EDIT_KEY)
  }

  val onSubmit = withWriteSession { implicit request =>
    val cy = request.form("year").toInt
    val isEdit = request.form bool IS_EDIT_KEY
    val form = Pre2015Form.form(cy).bindFromRequest()

    form.fold(
      formWithErrors => showPage(formWithErrors, cy.toString, isEdit),
      input => PensionInput() go Forward.using(request.data ++ input.data(cy))
    )
  }

  val onBack = withWriteSession { implicit request =>
    PensionInput() go Backward
  }

  protected def showPage(form: Form[_ <: Pre2015Fields], year: String, isEditing: Boolean)(implicit request: Request[Any]) = {
    Future.successful(Ok(views.html.pensionInputs(form, year, isEditing)))
  }
}
