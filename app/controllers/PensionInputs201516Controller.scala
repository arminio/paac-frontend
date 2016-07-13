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
import form.CalculatorForm

/**
  * 2015/16 Period-1 : Pre-Alignment Tax Year
  */
object PensionInputs201516Controller extends PensionInputs201516Controller {
  override val keystore: KeystoreService = KeystoreService
}

trait PensionInputs201516Controller extends RedirectController {
  val keystore: KeystoreService

  val onPageLoad = withSession { implicit request =>
    val toRead = List(DB_FLAG_PREFIX, DC_FLAG_PREFIX).map(_.toString+"2015") ++ List(P1_DB_KEY, P1_DC_KEY, P2_DB_KEY, P2_DC_KEY, IS_EDIT_KEY)
    keystore.read[String](toRead).map {
      (fieldsMap) =>
        Ok(views.html.pensionInputs_201516(CalculatorForm.bind(fieldsMap).discardingErrors,
          fieldsMap bool toRead(0),
          fieldsMap bool toRead(1),
          fieldsMap bool IS_EDIT_KEY))
    }
  }

  val onSubmit = withSession { implicit request =>
    val data = formRequestData
    val isDB = data("isDefinedBenefit").toBoolean
    val isDC = data("isDefinedContribution").toBoolean
    val isEdit = data("isEdit").toBoolean

    CalculatorForm.form.bindFromRequest().fold(
      formWithErrors => { Future.successful(Ok(views.html.pensionInputs_201516(formWithErrors, isDB, isDC, isEdit))) },
      input => {
        val isDBErrorP1 = !input.to1516Period1DefinedBenefit.isDefined && isDB
        val isDBErrorP2 = !input.to1516Period2DefinedBenefit.isDefined && isDB
        val isDCErrorP1 = !input.to1516Period1DefinedContribution.isDefined && isDC
        val isDCErrorP2 = !input.to1516Period2DefinedContribution.isDefined && isDC
        if ((isDBErrorP1 || isDCErrorP1) || (isDBErrorP2 || isDCErrorP2)) {
          var form = CalculatorForm.form.bindFromRequest()
          if (isDBErrorP1) {
            form = form.withError("year2015.definedBenefit_2015_p1", "db.error.bounds")
          }
          if (isDBErrorP2){
            form = form.withError("year2015.definedBenefit_2015_p2", "db.error.bounds")
          }
          if (isDCErrorP1){
            form = form.withError("year2015.definedContribution_2015_p1", "dc.error.bounds")
          }
          if (isDCErrorP2) {
            form = form.withError("year2015.definedContribution_2015_p2", "dc.error.bounds")
          }
          Future.successful(Ok(views.html.pensionInputs_201516(form, isDB, isDC, isEdit)))
        } else {
          keystore.save(List(input.to1516Period1DefinedBenefit, input.to1516Period1DefinedContribution, input.to1516Period2DefinedBenefit, input.to1516Period2DefinedContribution), "").flatMap(_=> PensionInput() go Forward)
        }
      }
    )
  }

  val onBack = withSession { implicit request =>
    PensionInput() go Backward
  }
}
