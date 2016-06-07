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
import service.KeystoreService._
import scala.concurrent.Future
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

object TaxYearSelectionController extends TaxYearSelectionController {
  override val keystore: KeystoreService = KeystoreService
}

trait TaxYearSelectionController extends RedirectController {
  val keystore: KeystoreService
  private val onSubmitRedirect: Call = routes.PensionInputsController.onPageLoad()

  def resetData(kkey: String, previous: String)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[String], request: Request[Any]): Unit = {
    if (previous != ""){
      val deletedYears = previous.split(",").diff(kkey.split(","))
      val keysToReset = deletedYears.flatMap {
        (year)=>
        if (year.size > 0) {
          if (year == "2015") {
            List(P1_DB_KEY, P1_DC_KEY, P2_DB_KEY, P2_DC_KEY, P1_TRIGGER_DC_KEY, P2_TRIGGER_DC_KEY, TRIGGER_DATE_KEY, TE_YES_NO_KEY, P1_YES_NO_KEY, P2_YES_NO_KEY)
          } else {
            List(DB_PREFIX+year, DC_PREFIX+year)
          }
        } else {
          List()
        }
      }
      val data = keysToReset.map((k)=>(None,k)).toList
      keystore.save[String](data, "")
    }
  }

  def onYearSelected = withSession { implicit request =>
    val data: Map[String, Seq[String]] = request.body.asFormUrlEncoded.getOrElse(Map[String, Seq[String]]())
    val kkey = data.filterKeys(_.contains("TaxYear")).map { case (k,v) => k.drop(7) }.mkString (",")

    if (kkey.length() > 0) {
      resetData(kkey,data("previous")(0))
      val year: String = ""
      keystore.save[String](List((kkey, SELECTED_INPUT_YEARS_KEY), (year, CURRENT_INPUT_YEAR_KEY))).flatMap{
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
