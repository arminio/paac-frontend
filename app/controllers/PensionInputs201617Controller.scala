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
import service._
import service.KeystoreService._

import scala.concurrent.Future

object PensionInputs201617Controller extends PensionInputs201617Controller {
  override val keystore: KeystoreService = KeystoreService
}

trait PensionInputs201617Controller extends RedirectController {
  val keystore: KeystoreService

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](List(CURRENT_INPUT_YEAR_KEY)).flatMap {
      (fieldsMap) =>
      val cy = fieldsMap int CURRENT_INPUT_YEAR_KEY
      if (cy <= 2015 || cy == -1)
        Start() go Edit
      else {
        val toRead = List(DB_FLAG_PREFIX, DC_FLAG_PREFIX, DB_PREFIX, DC_PREFIX).map(_.toString+cy) ++ List(IS_EDIT_KEY)
        keystore.read[String](toRead).map {
          (values) =>
            Ok(views.html.pensionInputs_201617(CalculatorForm.bind(values).discardingErrors,
              (values bool toRead(0)),
              (values bool toRead(1)),
              (values bool IS_EDIT_KEY),
              (fieldsMap int CURRENT_INPUT_YEAR_KEY)
              ))
        }
      }
    }
  }

  val onSubmit = withSession { implicit request =>
    val data = formRequestData
    val isDB = data("isDefinedBenefit").toBoolean
    val isDC = data("isDefinedContribution").toBoolean
    val isEdit = data("isEdit").toBoolean
    val year = data("year").toInt
    CalculatorForm.form.bindFromRequest().fold(
      formWithErrors => { Future.successful(Ok(views.html.pensionInputs_201617(formWithErrors, isDB, isDC, isEdit, year))) },
      input => {
        val isDBError = !input.toDefinedBenefit(year).isDefined && isDB
        val isDCError = !input.toDefinedContribution(year).isDefined && isDC
        if (isDBError || isDCError) {
          var form = CalculatorForm.form.bindFromRequest()
          if (isDBError) {
            form = form.withError("year2016.definedBenefit", "db.error.bounds")
          }
          if (isDCError) {
            form = form.withError("year2016.definedContribution", "dc.error.bounds")
          }
          Future.successful(Ok(views.html.pensionInputs_201617(form, isDB, isDC, isEdit, year)))
        } else
          keystore.save(List(input.toDefinedBenefit(year), input.toDefinedContribution(year)), "").flatMap(_=>PensionInput() go Forward)
      }
    )
  }

  val onBack = withSession { implicit request =>
    PensionInput() go Backward
  }
}
