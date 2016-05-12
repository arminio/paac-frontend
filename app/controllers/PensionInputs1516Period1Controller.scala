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

/**
  * 2015/16 Period-1 : Pre-Alignment Tax Year
  */
object PensionInputs1516Period1Controller extends PensionInputs1516Period1Controller {
  override val keystore: KeystoreService = KeystoreService
}

trait PensionInputs1516Period1Controller extends BaseFrontendController {
  val keystore: KeystoreService

  private val onSubmitRedirect: Call = routes.YesNo1516Period2Controller.onPageLoad()

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](List(KeystoreService.P1_DB_KEY, KeystoreService.P1_DC_KEY, KeystoreService.DB_FLAG, KeystoreService.DC_FLAG)).map {
      (fieldsMap) =>
        Ok(views.html.pensionInputs_1516_period1(CalculatorForm.bind(fieldsMap).discardingErrors,
                                                 fieldsMap(KeystoreService.DB_FLAG).toBoolean,
                                                 fieldsMap(KeystoreService.DC_FLAG).toBoolean))
    }
  }

  val onSubmit = withSession { implicit request =>
    CalculatorForm.form.bindFromRequest().fold(
      // TODO: When we do validation story, please forward this to onPageLoad method with selected SchemeType flags
      formWithErrors => { Future.successful(Ok(views.html.pensionInputs_1516_period1(formWithErrors))) },
      input => {
        keystore.save(List(input.to1516Period1DefinedBenefit, input.to1516Period1DefinedContribution), "").map((_)=>Redirect(onSubmitRedirect))
      }
    )
  }
}
