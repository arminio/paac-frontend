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

import connector.CalculatorConnector
import form.EligibilityForm
import play.api.mvc._
import scala.concurrent.Future
import service.KeystoreService

object EligibilityController extends EligibilityController{
  override val connector: CalculatorConnector = CalculatorConnector
  override val keystore: KeystoreService = KeystoreService
}

trait EligibilityController  extends BaseFrontendController {
  val connector: CalculatorConnector
  val keystore: KeystoreService

  val onPageLoad:Action[AnyContent] = withSession { implicit request =>
    Future.successful(Ok(views.html.eligibility(EligibilityForm.form)))
  }

  val onSubmit:Action[AnyContent] = withSession { implicit request =>
    EligibilityForm.form.bindFromRequest().fold(
      formWithErros => { Future.successful(Ok(views.html.eligibility(EligibilityForm.form))) },
      input => {
        keystore.store[String](input, EligibilityForm.Eligibility)
        Future.successful(Ok(views.html.selectScheme(SelectSchemeForm.form)))
      }
    )
  }
  val onBack:Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.startPage("")))
  }
}