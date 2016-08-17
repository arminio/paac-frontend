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
import config.AppSettings

object PensionInputs201516Controller extends PensionInputs201516Controller with AppSettings {
  def keystore: KeystoreService = KeystoreService
}

trait PensionInputs201516Controller extends RedirectController {

  val onPageLoad = withReadSession { implicit request =>
    val isDB = request.data bool s"${DB_FLAG_PREFIX}2015"
    val isDC = request.data bool s"${DC_FLAG_PREFIX}2015"
    showPage(Year2015Form.form(isDB, isDC).bind(convert(request.data)).discardingErrors, isDB, isDC, request.data bool IS_EDIT_KEY)
  }

  val onSubmit = withWriteSession { implicit request =>
    val isDB = request.data bool s"${DB_FLAG_PREFIX}2015"
    val isDC = request.data bool s"${DC_FLAG_PREFIX}2015"
    val form = Year2015Form.form(isDB, isDC).bindFromRequest()
    form.fold(
      formWithErrors => showPage(formWithErrors, isDB, isDC, request.data bool "isEdit"),
      input => PensionInput() go Forward.using(request.data ++ input.data)
    )
  }

  val onBack = withWriteSession { implicit request =>
    PensionInput() go Backward
  }

  protected def showPage(form: Form[_ <: Year2015Fields] , isDB: Boolean, isDC: Boolean, isEdit: Boolean)(implicit request: Request[_]) =
    Future.successful(Ok(views.html.pensionInputs_201516(form, isDB, isDC, isEdit)))
}
