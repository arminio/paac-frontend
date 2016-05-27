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

trait PensionInputs1516Period2Controller extends RedirectController {
  val keystore: KeystoreService

  private val onSubmitRedirect: Call = routes.YesNoMPAATriggerEventAmountController.onPageLoad()

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](List(KeystoreService.P2_DB_KEY, KeystoreService.P2_DC_KEY, KeystoreService.DB_FLAG, KeystoreService.DC_FLAG)).map {
      (fieldsMap) =>
        Ok(views.html.pensionInputs_1516_period2(CalculatorForm.bind(fieldsMap).discardingErrors,
                                                  fieldsMap(KeystoreService.DB_FLAG).toBoolean,
                                                  fieldsMap(KeystoreService.DC_FLAG).toBoolean))
    }
  }

  val onSubmit = withSession { implicit request =>
    keystore.read[String](List(KeystoreService.DB_FLAG, KeystoreService.DC_FLAG)).flatMap {
      (fieldsMap) =>
      val isDB = fieldsMap(KeystoreService.DB_FLAG).toBoolean
      val isDC = fieldsMap(KeystoreService.DC_FLAG).toBoolean

      CalculatorForm.form.bindFromRequest().fold(
        formWithErrors => { Future.successful(Ok(views.html.pensionInputs_1516_period2(formWithErrors, isDB, isDC))) },
        input => {
          val isDBError = !input.to1516Period2DefinedBenefit.isDefined && isDB
          val isDCError = !input.to1516Period2DefinedContribution.isDefined && isDC
          if (isDBError || isDCError) {
            var form = CalculatorForm.form.bindFromRequest()
            if (isDBError) {
              form = form.withError("year2015.definedBenefit_2015_p2", "error.bounds", play.api.i18n.Messages("db"))
            }
            if (isDCError) {
              form = form.withError("year2015.definedContribution_2015_p2", "error.bounds", play.api.i18n.Messages("dc"))
            }
            Future.successful(Ok(views.html.pensionInputs_1516_period2(form, isDB, isDC)))
          } else {
            keystore.save(List(input.to1516Period2DefinedBenefit, input.to1516Period2DefinedContribution), "").flatMap{
              (_)=>
              if (isDC) {
                Future.successful(Redirect(onSubmitRedirect))
              } else {
                wheretoNext(Redirect(routes.ReviewTotalAmountsController.onPageLoad()))
              }
            }
          }
        }
      )
    }
  }
}
