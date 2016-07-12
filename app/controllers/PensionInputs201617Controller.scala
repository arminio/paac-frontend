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

import form.CalculatorForm
import service.KeystoreService
import service.KeystoreService._

import scala.concurrent.Future

object PensionInputs201617Controller extends PensionInputs201617Controller {
  override val keystore: KeystoreService = KeystoreService
}

trait PensionInputs201617Controller extends RedirectController {
  val keystore: KeystoreService

  private val onSubmitRedirect = routes.YesNoThresholdIncomeController.onPageLoad

  val onBack = withSession { implicit request =>
    wheretoBack(Redirect(routes.SelectSchemeController.onPageLoad(2016)))
  }

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](List(DB_PREFIX, DC_PREFIX, s"${DB_FLAG_PREFIX}2016", s"${DC_FLAG_PREFIX}2016")).map {
      (fieldsMap) =>
        Ok(views.html.pensionInputs_201617(CalculatorForm.bind(fieldsMap).discardingErrors,
          fieldsMap(s"${DB_FLAG_PREFIX}2016").toBoolean,
          fieldsMap(s"${DC_FLAG_PREFIX}2016").toBoolean))
    }
  }

  val onSubmit = withSession { implicit request =>
    keystore.read[String](List(s"${DB_FLAG_PREFIX}2016", s"${DC_FLAG_PREFIX}2016", IS_EDIT_KEY)).flatMap {
      (fieldsMap) =>
        val isDB = fieldsMap(s"${DB_FLAG_PREFIX}2016").toBoolean
        val isDC = fieldsMap(s"${DC_FLAG_PREFIX}2016").toBoolean
        val isEdit = fieldsMap(IS_EDIT_KEY).toBoolean

        CalculatorForm.form.bindFromRequest().fold(
          formWithErrors => { Future.successful(Ok(views.html.pensionInputs_201617(formWithErrors, isDB, isDC))) },
          input => {
            val isDBError = !input.toDefinedBenefit(2016).isDefined && isDB
            val isDCError = !input.toDefinedContribution(2016).isDefined && isDC
            if (isDBError || isDCError) {
              var form = CalculatorForm.form.bindFromRequest()
              if (isDBError) {
                form = form.withError("year2016.definedBenefit", "db.error.bounds")
              }
              if (isDCError) {
                form = form.withError("year2016.definedContribution", "dc.error.bounds")
              }
              Future.successful(Ok(views.html.pensionInputs_201617(form, isDB, isDC)))
            } else {
              keystore.save(List(input.toDefinedBenefit(2016), input.toDefinedContribution(2016)), "").flatMap{
                (_)=>
                  if (isEdit) {
                    goTo(-1, true, isEdit, false, Redirect(onSubmitRedirect))
                  } else {
                      wheretoNext(Redirect(onSubmitRedirect))
                    }
                  }
              }
          }
        )
    }
  }
}
