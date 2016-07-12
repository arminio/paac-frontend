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
import form.SelectSchemeForm

object SelectSchemeController extends SelectSchemeController {
  override val keystore: KeystoreService = KeystoreService
}

trait SelectSchemeController  extends RedirectController {
  val keystore: KeystoreService

  def onPageLoad(year:Int) = withSession { implicit request =>
    implicit val marshall = KeystoreService.toStringPair _
    val toRead = List(DB_FLAG_PREFIX, DC_FLAG_PREFIX).map(""+_+year) ++ List(FIRST_DC_YEAR_KEY)
    keystore.read[String](toRead).map{
      (v)=>
      val m = Map(("definedBenefit"->v(toRead(0))),
                  ("definedContribution"->v(toRead(1))),
                  ("firstDCYear"->v(toRead(2))),
                  ("year"->year.toString))
      Ok(views.html.selectScheme(SelectSchemeForm.form.bind(m).discardingErrors, year))
    }
  }

  val onSubmit = withSession { implicit request =>
    SelectSchemeForm.form.bindFromRequest().fold(
      formWithErrors => Future.successful(Ok(views.html.selectScheme(formWithErrors, formRequestData(request)("year").toInt))),
      input => {
        val year = input.year
        if (!input.definedBenefit && !input.definedContribution) {
          val form = SelectSchemeForm.form.withError("paac.scheme.selection.error","paac.scheme.selection.error")
          Future.successful(Ok(views.html.selectScheme(form, year)))
        } else {
          val firstDCYear = if (input.definedContribution && (input.firstDCYear.isEmpty || input.firstDCYear.toInt < year)) year.toString 
                            else if (!input.firstDCYear.isEmpty && !input.definedContribution && input.firstDCYear.toInt == year) "" 
                            else input.firstDCYear.toString
          keystore.save[String](List((s"${input.definedBenefit}", s"${DB_FLAG_PREFIX}${year}"),
                                     (s"${input.definedContribution}", s"${DC_FLAG_PREFIX}${year}"),
                                     (firstDCYear, FIRST_DC_YEAR_KEY))).flatMap(_=>SelectScheme() go Forward) 
        }
      }
    )
  }

  val onBack = withSession { implicit request =>
    SelectScheme() go Backward
  }
}
