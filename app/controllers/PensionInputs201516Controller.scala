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
import models.CalculatorFormFields
import play.api.data.Form
import play.api.mvc.Request

/**
  * 2015/16 Period-1 : Pre-Alignment Tax Year
  */
object PensionInputs201516Controller extends PensionInputs201516Controller {
  def keystore: KeystoreService = KeystoreService
}

trait PensionInputs201516Controller extends RedirectController {

  val onPageLoad = withReadSession { implicit request =>
    showPage(CalculatorForm.bind(request.data).discardingErrors,
             request.data bool s"${DB_FLAG_PREFIX}2015",
             request.data bool s"${DC_FLAG_PREFIX}2015",
             request.data bool IS_EDIT_KEY)
  }

  val onSubmit = withWriteSession { implicit request =>
    val data = request.form
    val isDB = data bool "isDefinedBenefit"
    val isDC = data bool "isDefinedContribution"
    val isEdit = data bool "isEdit"

    val form = CalculatorForm.form.bindFromRequest()
    form.fold(
      formWithErrors => {
        showPage(formWithErrors, isDB, isDC, isEdit)
      },
      input => {
        val isDBErrorP1 = ("year2015.definedBenefit_2015_p1", !input.to1516Period1DefinedBenefit.isDefined && isDB)
        val isDBErrorP2 = ("year2015.definedBenefit_2015_p2", !input.to1516Period2DefinedBenefit.isDefined && isDB)
        val isDCErrorP1 = ("year2015.definedContribution_2015_p1", !input.to1516Period1DefinedContribution.isDefined && isDC)
        val isDCErrorP2 = ("year2015.definedContribution_2015_p2", !input.to1516Period2DefinedContribution.isDefined && isDC)
        val errors = List(isDBErrorP1, isDBErrorP2, isDCErrorP1, isDCErrorP2)
        if (errors.exists(_._2)) {
          val formWithErrors = errors.foldLeft(form) {
            (newForm, error) =>
            newForm.withError(error._1, if (error._1.contains("definedBenefit")) "db.error.bounds" else "dc.error.bounds")
          }
          showPage(formWithErrors, isDB, isDC, isEdit)
        } else {
          val data = List(input.to1516Period1DefinedBenefit,
                          input.to1516Period1DefinedContribution,
                          input.to1516Period2DefinedBenefit,
                          input.to1516Period2DefinedContribution).foldLeft(List[(Long, String)]()) {
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

  protected def showPage(form: Form[CalculatorFormFields] , isDB: Boolean, isDC: Boolean, isEdit: Boolean)(implicit request: Request[_]) = {
    Future.successful(Ok(views.html.pensionInputs_201516(form, isDB, isDC, isEdit)))
  }
}
