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
import service._
import service.KeystoreService._
import scala.concurrent.Future
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

object TaxYearSelectionController extends TaxYearSelectionController {
  def keystore: KeystoreService = KeystoreService
}

trait TaxYearSelectionController extends RedirectController {
  def onYearSelected = withWriteSession { implicit request =>
    val kkey = request.form.filterKeys(_.contains("TaxYear")).map { case (k,v) => k.drop(7) }.mkString (",")
    if (kkey.nonEmpty) {
      val data = request.data ++ sessionData(kkey,request.form("previous"))
      TaxYearSelection() go Forward.using(data)
    } else {
      Future.successful(Ok(views.html.taxyearselection(Array[String](), true)))
    }
  }

  val onPageLoad = withReadSession { implicit request =>
    Future.successful(Ok(views.html.taxyearselection(request.data.getOrElse(SELECTED_INPUT_YEARS_KEY, "").split(","), false)))
  }

  protected def sessionData(kkey: String, previous: String): Map[String,String] = {
    val m = if (!previous.isEmpty) {
      val deletedYears = previous.split(",").diff(kkey.split(","))
      deletedYears.flatMap {
        (year)=>
        if (year.size > 0)
          if (year == "2015") // to do 2016+
            List(P1_DB_KEY, P1_DC_KEY, P2_DB_KEY, P2_DC_KEY, P1_TRIGGER_DC_KEY, P2_TRIGGER_DC_KEY, TRIGGER_DATE_KEY, TE_YES_NO_KEY)
          else
            List(DB_PREFIX+year, DC_PREFIX+year)
        else List()
      }.map((k)=>(k,"")).toList.toMap
    } else {
      Map[String,String]()
    }
    m ++ Map((SELECTED_INPUT_YEARS_KEY->kkey), (CURRENT_INPUT_YEAR_KEY->PageLocation.START.toString))
  }
}
