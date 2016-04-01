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
import form.{PensionInputFormFields, CalculatorForm, SelectSchemeForm, PensionInputForm}

object PensionInputsController extends PensionInputsController {

  override val keystore: KeystoreService = KeystoreService
  override val connector: PensionInputsController = PensionInputsController

}

trait PensionInputsController  extends BaseFrontendController {

  val keystore: KeystoreService
  val connector: PensionInputsController

  private val onSubmitRedirect: Call = routes.ReviewTotalAmountsController.onPageLoad()

  val onPageLoad = withSession { implicit request =>
    Future.successful(Ok(views.html.pensionInputs(PensionInputForm.form)))
  }

  val onSubmit = withSession { implicit request =>

    PensionInputForm.form.bindFromRequest().fold(
      formWithErrors => { Future.successful(Ok(views.html.selectScheme(SelectSchemeForm.form))) },
      input => {
        keystore.store[BigDecimal](input.amount2014, "definedBenefit_2014")
        Future.successful(Redirect(onSubmitRedirect))
      }
    )

  }
}



