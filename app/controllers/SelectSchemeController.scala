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
import form.{PensionInputForm, SelectSchemeForm}

object SelectSchemeController extends SelectSchemeController {

  override val keystore: KeystoreService = KeystoreService
  override val connector: SelectSchemeController = SelectSchemeController

}

trait SelectSchemeController  extends BaseFrontendController {
  val keystore: KeystoreService
  val connector: SelectSchemeController

  val onPageLoad:Action[AnyContent] = withSession { implicit request =>
    Future.successful(Ok(views.html.selectScheme(SelectSchemeForm.form)))
  }

  val onSubmit:Action[AnyContent] = withSession { implicit request =>

    SelectSchemeForm.form.bindFromRequest().fold(
      formWithErrors => { Future.successful(Ok(views.html.selectScheme(SelectSchemeForm.form))) },
      input => {
        keystore.store[String](input, SelectSchemeForm.schemeType)
        Future.successful(Ok(views.html.pensionInputs(PensionInputForm.form)))
      }
    )
  }

}


