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
import models.CalculatorFormFields
import play.api.data.Form
import service.KeystoreService._
import play.api.mvc.Request
import scala.concurrent.Future

object PensionInputs201617Controller extends PensionInputs201617Controller {
  def keystore: KeystoreService = KeystoreService
}

trait PensionInputs201617Controller extends RedirectController {

  val onPageLoad = withReadSession { implicit request =>
    val year = request.data int CURRENT_INPUT_YEAR_KEY
    if (year <= 2015 || year == -1)
      Start() go Edit
    else {
      val isDB = request.data bool s"${DB_FLAG_PREFIX}${year}"
      val isDC = request.data bool s"${DC_FLAG_PREFIX}${year}"
      showPage(CalculatorForm.bind(request.data).discardingErrors, isDB, isDC, (request.data bool IS_EDIT_KEY), year)
    }
  }

  val onSubmit = withWriteSession { implicit request =>
    val year = request.form int "year"
    val isDB = request.form bool "isDefinedBenefit"
    val isDC = request.form bool "isDefinedContribution"
    val isEdit = request.form bool "isEdit"
    var form = CalculatorForm.form.bindFromRequest()

    form.fold(
      formWithErrors => {
        showPage(formWithErrors, isDB, isDC, isEdit, year)
      },
      input => {
        val isDBError = ("year2016.definedBenefit", !input.toDefinedBenefit(year).isDefined && isDB)
        val isDCError = ("year2016.definedContribution", !input.toDefinedContribution(year).isDefined && isDC)
        val errors = List(isDBError, isDBError)
        if (errors.exists(_._2)) {
          val formWithErrors = errors.foldLeft(form) {
            (newForm, error) =>
            newForm.withError(error._1, if (error._1.contains("definedBenefit")) "db.error.bounds" else "dc.error.bounds")
          }
          showPage(formWithErrors, isDB, isDC, isEdit, year)
        } else {
          val data = List(input.toDefinedBenefit(year),input.toDefinedContribution(year)).foldLeft(List[(Long, String)]()) {
            (lst, entry)=>
            entry match {
              case Some(v) => lst ++ List(v)
              case None => lst
            }
          }
          val sessionData = request.data ++ data.map(_.swap).toMap.mapValues(_.toString)
          PensionInput() go Forward.using(sessionData)
        }
      }
    )
  }

  val onBack = withSession { implicit request =>
    PensionInput() go Backward
  }

  protected def showPage(form: Form[CalculatorFormFields] , isDB: Boolean, isDC: Boolean, isEdit: Boolean, year: Int)(implicit request: Request[_]) = {
    Future.successful(Ok(views.html.pensionInputs_201617(form, isDB, isDC, isEdit, year)))
  }
}
