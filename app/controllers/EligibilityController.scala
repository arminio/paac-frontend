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

import form.EligibilityForm
import play.api.mvc._
import scala.concurrent.Future
import service.KeystoreService

object EligibilityController extends EligibilityController{
  override val keystore: KeystoreService = KeystoreService
}

trait EligibilityController  extends BaseFrontendController {
  val keystore: KeystoreService

  private val onSubmitRedirect: Call = routes.SelectSchemeController.onPageLoad()

  val onPageLoad = withSession { implicit request =>
    Future.successful(Ok(views.html.eligibility(EligibilityForm.form)))
  }

  val onSubmit = withSession { implicit request =>
    EligibilityForm.form.bindFromRequest().fold(
      formWithErrors => { Future.successful(Ok(views.html.eligibility(EligibilityForm.form))) },
      input => {
        //val (eligibility:Boolean) = input.equals("")
        keystore.store[String](input, EligibilityForm.eligibility)
        Future.successful(Redirect(onSubmitRedirect))
      }
    )
  }
}