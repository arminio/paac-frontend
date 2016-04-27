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

object PensionInputs1516Period2Controller extends PensionInputs1516Period2Controller {
  override val keystore: KeystoreService = KeystoreService
}

trait PensionInputs1516Period2Controller extends BaseFrontendController {
  val keystore: KeystoreService

  private val kesystoreDBKey = "definedBenefit_2015_p2"
  private val kesystoreDCKey = "definedContribution_2015_p1"
  private var selectedSchemeTypeKey: String = "schemeType"
  private var selectedSchemeType: String = _
  private val onSubmitRedirect: Call = routes.PensionInputsController.onPageLoad()

  val onPageLoad = withSession { implicit request =>
    var fields:Map[String, String] = Map()

    keystore.read[String](selectedSchemeTypeKey).map {
      (schemeType) =>
        selectedSchemeType = schemeType match {
          case None => ""
          case Some(value) => value
        }
    }

    keystore.read[String](kesystoreDBKey).map {
      (amount) =>
        fields ++= Map(amount match {
          case None => (kesystoreDBKey, "")
          case Some("0") => (kesystoreDBKey, "0.00")
          case Some(value) => (kesystoreDBKey, f"${(value.toInt/100.00)}%2.2f")
        })
    }
    keystore.read[String](kesystoreDCKey).map {
      (amount) =>
        fields ++= Map(amount match {
          case None => (kesystoreDCKey, "")
          case Some("0") => (kesystoreDCKey, "0.00")
          case Some(value) => (kesystoreDCKey, f"${(value.toInt/100.00)}%2.2f")
        })
        Ok(views.html.pensionInputs_1516_period2(CalculatorForm.form.bind(fields).discardingErrors,selectedSchemeType))
    }
    //Future(Ok(views.html.pensionInputs_1516_period2(CalculatorForm.form.bind(fields).discardingErrors)))

  }

  val onSubmit = withSession { implicit request =>
    CalculatorForm.form.bindFromRequest().fold(
      formWithErrors => { Future.successful(Ok(views.html.pensionInputs_1516_period2(formWithErrors,selectedSchemeType))) },
      input => {
        val (dbAmount:Long, dbKey:String) = input.to1516Period2DefinedBenefit.getOrElse((kesystoreDBKey, 0L))
        keystore.store[String](dbAmount.toString, dbKey)
        val (dcAmount:Long, dcKey:String) = input.to1516Period2DefinedContribution.getOrElse((kesystoreDCKey, 0L))
        keystore.store[String](dcAmount.toString, dcKey)
        Future.successful(Redirect(onSubmitRedirect))
      }
    )
  }
}



