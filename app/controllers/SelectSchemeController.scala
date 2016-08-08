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
import play.api.data.Form
import form._
import models._
import scala.concurrent.Future
import form.SelectSchemeForm
import play.api.mvc.Request

object SelectSchemeController extends SelectSchemeController {
  def keystore: KeystoreService = KeystoreService
}

trait SelectSchemeController  extends RedirectController {
  def onPageLoad(year:Int) = withReadSession { implicit request =>
    val m = Map(("definedBenefit"->request.data.get(s"${DB_FLAG_PREFIX}${year}").getOrElse("")),
                ("definedContribution"->request.data.get(s"${DC_FLAG_PREFIX}${year}").getOrElse("")),
                ("firstDCYear"->request.data.get(FIRST_DC_YEAR_KEY).getOrElse("")),
                ("year"->year.toString))
    Future.successful(showPage(SelectSchemeForm.form.bind(m).discardingErrors, year))
  }

  val onSubmit = withWriteSession { implicit request =>
    val f = SelectSchemeForm.form.bindFromRequest()
    f.fold(
      formWithErrors => {
        Future.successful(showPage(formWithErrors, request.form("year").toInt))
      },
      input => {
        val year = input.year
        if (!input.definedBenefit && !input.definedContribution) {
          val form = f.withError("paac.scheme.selection.error","paac.scheme.selection.error")
          Future.successful(showPage(form, year))
        } else {
          val firstDCYear = if (input.definedContribution && (input.firstDCYear.isEmpty || input.firstDCYear.toInt < year)) year.toString
                            else if (!input.firstDCYear.isEmpty && !input.definedContribution && input.firstDCYear.toInt == year) ""
                            else input.firstDCYear.toString
          val data = request.data ++ List((FIRST_DC_YEAR_KEY, firstDCYear),
                          (s"${DC_FLAG_PREFIX}${year}", s"${input.definedContribution}"),
                          (s"${DB_FLAG_PREFIX}${year}", s"${input.definedBenefit}")).toMap

          val toRemove = if (!input.definedContribution) {
            val l = if (input.year == 2015) List(P1_DC_KEY, P2_DC_KEY) else List(DC_PREFIX+year)
            val triggerDate: PensionPeriod = if (request.data.contains(TRIGGER_DATE_KEY)) request.data(TRIGGER_DATE_KEY) else ""
            if (triggerDate.taxYear == year)
              l ++ List(TRIGGER_DATE_KEY, P1_TRIGGER_DC_KEY, P2_TRIGGER_DC_KEY, TRIGGER_DC_KEY)
            else
              l
          } else if (!input.definedBenefit) {
            if (input.year == 2015) List(P1_DB_KEY, P2_DB_KEY) else List(DB_PREFIX+year)
          } else {
            List()
          }
          val sessionData = toRemove.foldLeft(data) {
            (lst,key) =>
            lst - key
          }
          SelectScheme() go Forward.using(sessionData)
        }
      }
    )
  }

  def onBack(year:Int) = withWriteSession { implicit request =>
    SelectScheme() go Backward
  }

  protected def showPage(form: Form[SelectSchemeModel], year: Int)(implicit request: Request[_]) = {
    Ok(views.html.selectScheme(form, year))
  }
}
