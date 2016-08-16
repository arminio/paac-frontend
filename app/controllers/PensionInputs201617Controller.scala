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
import play.api.data.Form
import service.KeystoreService._
import play.api.mvc.Request
import scala.concurrent.Future
import play.api.Logger
import form._
import config.AppSettings

object PensionInputs201617Controller extends PensionInputs201617Controller with AppSettings {
  def keystore: KeystoreService = KeystoreService
}

trait PensionInputs201617Controller extends RedirectController {

  val onPageLoad = withReadSession { implicit request =>
    val year = request.data int CURRENT_INPUT_YEAR_KEY
    if (year <= 2015 || year == -1) {
      Start() go Edit
    } else {
      val isDB = request.data bool s"${DB_FLAG_PREFIX}${year}"
      val isDC = request.data bool s"${DC_FLAG_PREFIX}${year}"
      val isEdit = request.data bool IS_EDIT_KEY
      showPage(Post2015Form.form(isDB, isDC, year).bind(convert(request.data)).discardingErrors, isDB, isDC, isEdit, year)
    }
  }

  val onSubmit = withWriteSession { implicit request =>
    val year = request.data int CURRENT_INPUT_YEAR_KEY
    val isDB = request.data bool s"${DB_FLAG_PREFIX}${year}"
    val isDC = request.data bool s"${DC_FLAG_PREFIX}${year}"
    val isEdit = request.data bool IS_EDIT_KEY

    Post2015Form.form(isDB, isDC, year).bindFromRequest().fold(
      formWithErrors => showPage(formWithErrors, isDB, isDC, isEdit, year),
      input => PensionInput() go Forward.using(request.data ++ input.data(year))
    )
  }

  val onBack = withSession { implicit request =>
    PensionInput() go Backward
  }

  protected def showPage(form: Form[_ <: Post2015Fields] , isDB: Boolean, isDC: Boolean, isEdit: Boolean, year: Int)(implicit request: Request[_]) = {
    Future.successful(Ok(views.html.pensionInputs_201617(form, isDB, isDC, isEdit, year)))
  }
}
