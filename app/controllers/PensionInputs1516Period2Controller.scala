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
    keystore.read[String](List(KeystoreService.P2_DB_KEY, KeystoreService.P2_DC_KEY, KeystoreService.DB_KEY, KeystoreService.DC_KEY)).map {
      (fieldsMap) =>
        Ok(views.html.pensionInputs_1516_period2(CalculatorForm.bind(fieldsMap).discardingErrors,
                                                  fieldsMap(KeystoreService.DB_KEY).toBoolean,
                                                  fieldsMap(KeystoreService.DC_KEY).toBoolean))
    }
  }

  val onSubmit = withSession { implicit request =>
    keystore.read[String](KeystoreService.DC_KEY).flatMap {
      (value) =>
      CalculatorForm.form.bindFromRequest().fold(
        // TODO: When we do validation story, please forward this to onPageLoad method with selected SchemeType flags
        formWithErrors => { Future.successful(Ok(views.html.pensionInputs_1516_period2(formWithErrors))) },
        input => {
          keystore.save(List(input.to1516Period2DefinedBenefit, input.to1516Period2DefinedContribution), "").flatMap {
            (_)=>
            if (value.getOrElse("false").toBoolean) {
              Future.successful(Redirect(onSubmitRedirect))
            } else {
              wheretoNext[String](Redirect(routes.ReviewTotalAmountsController.onPageLoad()))
            }
          }
        }
      )
    }
  }
}
