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

import scala.concurrent.Future
import play.api.mvc._
import service._
import service.KeystoreService._
import play.api.Logger
import controllers.action._
import config.{PaacContextImpl, Settings}

trait RedirectController extends BaseFrontendController with Settings {
  settings: Settings =>

  implicit val context: config.PaacContext = PaacContextImpl

  implicit class RichPageLocation(location:PageLocation) {
    def go(e:Event)(implicit request: Request[Any]): Future[Result] = {
      implicit val newSessionData = request.session.data
      goto(e)
    }

    def go(pair: Tuple2[Event, Map[String,String]])(implicit request: Request[Any]): Future[Result] = {
      implicit val newSessionData = pair._2
      goto(pair._1)
    }

    protected def goto(e:Event)(implicit newSessionData: Map[String,String], request: Request[Any]) = {
      location match {
        case Start(_) => Future.successful(Redirect((location move e).action).addingToSession(newSessionData.toSeq: _*))
        case _ => move(e, newSessionData)
      }
    }

    protected def move(e:Event, newSessionData: Map[String,String])(implicit request: Request[Any]): Future[Result] = {
      val currentYear = if (location.state.year == PageLocation.END) location.lastYear(newSessionData(SELECTED_INPUT_YEARS_KEY))
                        else if (newSessionData(CURRENT_INPUT_YEAR_KEY).isEmpty) location.firstYear(newSessionData(SELECTED_INPUT_YEARS_KEY))
                        else newSessionData(CURRENT_INPUT_YEAR_KEY).toInt
      val updatedLocation = location.updateForYear(location, currentYear, newSessionData)
      val page = e match {
        case Backward => {
          val nextYear = (updatedLocation move e).state.year
          if (nextYear != currentYear && !isCheckYourAnswersPage(location)) {
            val p = updateForYear(location, nextYear, newSessionData)
            p.update(p.state.copy(year=updatedLocation.state.year))
          } else {
            updatedLocation
          }
        }
        case _ => updatedLocation
      }

      val next = page move e
      Logger.info(s"${page} -> ${next}, saving next year as ${next.state.year}")

      val data = newSessionData ++ Map(CURRENT_INPUT_YEAR_KEY->next.state.year.toString)
      Future.successful(updateSession(data, Redirect(next.action)))
    }

    protected def updateSession(newSessionData: Map[String,String], results: Result)(implicit request: Request[Any]): Result = {
      val r = results.addingToSession(newSessionData.toSeq: _*)
      val keysToRemove = r.session.data.filterKeys(!newSessionData.isDefinedAt(_)).keys.toList
      r.removingFromSession(keysToRemove: _*)
    }

    protected def isCheckYourAnswersPage(location:PageLocation):Boolean = location match {
      case CheckYourAnswers(_) => true
      case _                   => false
    }

    protected def updateYear(): Boolean = location match {
      case TaxYearSelection(_) => false
      case Start(_) => false
      case CheckYourAnswers(_) => false
      case _ => true
    }

    protected def updateForYear(page: PageLocation, year: Int, newSessionData: Map[String,String]): PageLocation = {
      page.update(PageState(isDC = newSessionData bool s"${DC_FLAG_PREFIX}${year}",
                            isTE = newSessionData yesNo TE_YES_NO_KEY,
                            isTI = newSessionData yesNo s"${TI_YES_NO_KEY_PREFIX}${year}",
                            year = if (updateYear) year else page.state.year,
                            selectedYears = newSessionData(SELECTED_INPUT_YEARS_KEY),
                            isEdit = (newSessionData bool IS_EDIT_KEY),
                            firstDCYear = (newSessionData int FIRST_DC_YEAR_KEY)))
    }
  }

  protected def convert(data: Map[String,String]): Map[String,String] = {
    data.map{
      (entry)=>
      if (List(P1_DB_KEY, P2_DB_KEY, P1_DC_KEY, P2_DC_KEY, P1_TRIGGER_DC_KEY, P2_TRIGGER_DC_KEY, TRIGGER_DC_KEY).contains(entry._1) ||
          entry._1.startsWith(DB_PREFIX) ||
          entry._1.startsWith(DC_PREFIX) ||
          entry._1.startsWith(TH_PREFIX) ||
          entry._1.startsWith(AI_PREFIX) ||
          entry._1.startsWith(TA_PREFIX)) {
        if (settings.POUNDS_AND_PENCE) (entry._1, toDecimal(entry._2)) else (entry._1, toInt(entry._2))
      } else {
        entry
      }
    }
  }

  protected def toDecimal(v: String):String = if (v.trim.isEmpty || !v.matches("\\d+")) "" else f"${(v.toInt / 100.00)}%2.2f".trim
  protected def toInt(v: String):String = if (v.trim.isEmpty || !v.matches("\\d+")) "" else f"${(v.toInt / 100.00)}%2.0f".trim
}