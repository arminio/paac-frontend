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
    val key = "definedBenefit_2014"
    keystore.read[String](key).map {
        (amount) =>
        val fields = Map(amount match {
          case None => (key, "0.00")
          case Some("0") => (key, "0.00")
          case Some(value) => (key, (value.toInt/100.00).toString)
        })
        Ok(views.html.pensionInputs(PensionInputForm.form.bind(fields)))
    }
  }

  val onSubmit = withSession { implicit request =>

    PensionInputForm.form.bindFromRequest().fold(
      formWithErrors => { Future.successful(Ok(views.html.pensionInputs(PensionInputForm.form))) },
      input => {
        val (amount:Long, key:String) = input.toDefinedBenefit(2014).getOrElse(("definedBenefit_2014", 0L))
        keystore.store[String](amount.toString, key)
        Future.successful(Redirect(onSubmitRedirect))
      }
    )

  }
}



