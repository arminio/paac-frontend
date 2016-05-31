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

import play.api.mvc._
import service.KeystoreService
import scala.concurrent.Future

object TaxYearSelectionController extends TaxYearSelectionController {
  override val keystore: KeystoreService = KeystoreService
}
  trait TaxYearSelectionController extends RedirectController {
    val keystore: KeystoreService
    private val onSubmitRedirect: Call = routes.PensionInputsController.onPageLoad()

  def onYearSelected = withSession { implicit request =>
    val data: Map[String, Seq[String]] = request.body.asFormUrlEncoded.getOrElse(Map[String, Seq[String]]())
    val kkey = data.view.map { case (k,v) => if (k == "csrfToken" ) "" else k.drop(7) }.drop(1) mkString (",")

    if (kkey.length() > 0) {
      val year: String = ""
      keystore.save[String](List((kkey, KeystoreService.SELECTED_INPUT_YEARS_KEY), (year, KeystoreService.CURRENT_INPUT_YEAR_KEY))).flatMap{
        _ =>
        wheretoNext(Redirect(routes.StartPageController.startPage()))
      }
    } else {
      Future.successful(Ok(views.html.taxyearselection(Array[String](), true)))
    }
  }

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](KeystoreService.SELECTED_INPUT_YEARS_KEY).map {
      (taxyears) =>
      Ok(views.html.taxyearselection(taxyears.getOrElse("").split(","), false))
    }
  }
}
