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
import form.CalculatorForm


object AdjustedIncome1617InputController extends AdjustedIncome1617InputController {
  override val keystore: KeystoreService = KeystoreService
}

trait AdjustedIncome1617InputController extends RedirectController {
  val keystore: KeystoreService

  val onPageLoad = withSession { implicit request =>
    Future.successful(Ok(views.html.adjusted_income_1617_input(CalculatorForm.form)))
  }

  val onSubmit = withSession { implicit request =>
       CalculatorForm.form.bindFromRequest().fold(
        formWithErrors => { Future.successful(Ok(views.html.adjusted_income_1617_input(formWithErrors))) },
        input => {
          Future.successful(Ok(views.html.adjusted_income_1617_input(CalculatorForm.form)))
        }
      )
  }
}
