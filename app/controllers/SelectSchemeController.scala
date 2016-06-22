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
import service.KeystoreService._
import play.api.mvc._
import scala.concurrent.Future
import form.SelectSchemeForm

object SelectSchemeController extends SelectSchemeController {
  override val keystore: KeystoreService = KeystoreService
}

trait SelectSchemeController  extends RedirectController {
  val keystore: KeystoreService

  private val onSubmitRedirect: Call = routes.PensionInputs201516Controller.onPageLoad()

  def onPageLoad(year:Int) = withSession { implicit request =>
    keystore.read[String](List(s"${DB_FLAG_PREFIX}${year}", s"${DC_FLAG_PREFIX}${year}")).map{
      (v)=>
      val m = Map(("definedBenefit"->v(s"${DB_FLAG_PREFIX}${year}")),("definedContribution"->v(s"${DC_FLAG_PREFIX}${year}")))
      Ok(views.html.selectScheme(SelectSchemeForm.form.bind(m).discardingErrors, year))
    }
  }

  val onSubmit = withSession { implicit request =>
    val year = formRequestData(request)("year").toInt
    SelectSchemeForm.form.bindFromRequest().fold(
      formWithErrors => Future.successful(Ok(views.html.selectScheme(formWithErrors, year))),
      input => {
        if (!input.definedBenefit && !input.definedContribution) {
          val form = SelectSchemeForm.form.withError("paac.scheme.selection.error","paac.scheme.selection.error")
          Future.successful(Ok(views.html.selectScheme(form, year)))
        } else {
          keystore.save[String](List((s"${input.definedBenefit}", s"${DB_FLAG_PREFIX}${year}"),
                                     (s"${input.definedContribution}", s"${DC_FLAG_PREFIX}${year}"))).map {
            (_)=> 
            Redirect(onSubmitRedirect)
          }
        }
      }
    )
  }

  def onBack(year:Int) = withSession { implicit request =>
    // once 2016 and later implemented we need to pass on the year
    wheretoBack(Redirect(routes.TaxYearSelectionController.onPageLoad))
  }
}
