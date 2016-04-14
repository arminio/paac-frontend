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

object PensionInputs1516P1Controller extends PensionInputs1516P1Controller {
  override val keystore: KeystoreService = KeystoreService
}

trait PensionInputs1516P1Controller extends BaseFrontendController {
  val keystore: KeystoreService

  private val onSubmitRedirect: Call = routes.PensionInputs1516P2Controller.onPageLoad()

  val onPageLoad = withSession { implicit request =>
    val key = "definedBenefit_2015_p1"
    keystore.read[String](key).map {
        (amount) =>
        val fields = Map(amount match {
          case None => (key, "")
          case Some("0") => (key, "0.00")
          case Some(value) => (key, f"${(value.toInt/100.00)}%2.2f")
        })
        Ok(views.html.pensionInputs_1516_p1(CalculatorForm.form.bind(fields).discardingErrors))
    }
  }

  val onSubmit = withSession { implicit request =>
    CalculatorForm.form.bindFromRequest().fold(
      formWithErrors => { Future.successful(Ok(views.html.pensionInputs_1516_p1(formWithErrors))) },
      input => {
        val (amount:Long, key:String) = input.to1516Period1DefinedBenefit.getOrElse(("definedBenefit_2015_p1", 0L))
        keystore.store[String](amount.toString, key)
        Future.successful(Redirect(onSubmitRedirect))
      }
    )

  }
}