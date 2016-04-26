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

import play.api.mvc.{Results, AnyContent, Request}
import service.KeystoreService

import scala.concurrent.Future
import form.TaxYearSelectionForm

object TaxYearSelectionController extends TaxYearSelectionController {
  override val keystore: KeystoreService = KeystoreService

}
  trait TaxYearSelectionController extends BaseFrontendController {
    val keystore: KeystoreService


    def onYearSelected = withSession { implicit request =>
      val data : Map[String,Seq[String]] = request.body.asFormUrlEncoded.getOrElse(Map[String,Seq[String]]())
      val key:String = data.keySet.find(_.contains("TaxYear")).getOrElse("TaxYear2015")
      val year = key.drop(7).toInt
      val wheretogo = if (year < 2015) {
        routes.PensionInputsController.onPageLoad()
      } else if (year == 2015) {
       routes.PensionInputs1516Period1Controller.onPageLoad()
      } else {
        routes.PensionInputsController.onPageLoad()
      }
      Future.successful(Results.Redirect(wheretogo))
    }




    val onPageLoad = withSession { implicit request =>
      Future.successful(Ok(views.html.taxyearselection(TaxYearSelectionForm.form)) )
    }

}
