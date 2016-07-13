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

import java.util.UUID
import scala.concurrent.Future
import play.api.mvc._

import service._
import service.KeystoreService._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import scala.util.{Try, Success, Failure}
import play.api.libs.json._
import play.api.Logger

trait SessionProvider {
  val NOSESSION = "NOSESSION"
  def createSessionId()(implicit request: Request[AnyContent]) : (String, String) = SessionKeys.sessionId -> s"session-${UUID.randomUUID}"

  def createKeystoreSession()(implicit request: Request[AnyContent]) : Session = {
    Session(request.session.data + createSessionId())
  }
}

trait RedirectController extends BaseFrontendController {
  def keystore: KeystoreService

  protected def isEdit()(implicit hc: HeaderCarrier, format: Format[String], request: Request[Any]): Future[Boolean] = {
    implicit val marshall = KeystoreService.toStringPair _
    keystore.read(List(IS_EDIT_KEY)).map((fieldsMap)=>fieldsMap bool IS_EDIT_KEY)
  }

  implicit class RichPageLocation(location:PageLocation) {
    protected implicit val marshall = KeystoreService.toStringPair _

    private def updateYear(): Boolean = location match {
      case TaxYearSelection(_) => false
      case Start(_) => false
      case CheckYourAnswers(_) => false
      case _ => true
    }

    protected def updateForYear(page: PageLocation, year: Int)(implicit hc: HeaderCarrier, format: Format[String], request: Request[Any]): Future[PageLocation] = {
      val toRead = List(DC_FLAG_PREFIX,TI_YES_NO_KEY_PREFIX).map(_+year) ++ List(IS_EDIT_KEY, TE_YES_NO_KEY, SELECTED_INPUT_YEARS_KEY, FIRST_DC_YEAR_KEY)
      keystore.read(toRead).map {
        (fieldsMap)=>
        page.update(PageState(isDC = fieldsMap bool toRead(0),
                              isTE = fieldsMap yesNo TE_YES_NO_KEY,
                              isTI = fieldsMap yesNo toRead(1),
                              year = if (updateYear) year else page.state.year,
                              selectedYears = fieldsMap(SELECTED_INPUT_YEARS_KEY),
                              isEdit = (fieldsMap bool IS_EDIT_KEY),
                              firstDCYear = (fieldsMap int FIRST_DC_YEAR_KEY)))
      }
    }

    def go(e:Event)(implicit hc: HeaderCarrier, format: Format[String], request: Request[Any]): Future[Result] = {
      keystore.read(List(CURRENT_INPUT_YEAR_KEY,SELECTED_INPUT_YEARS_KEY)).flatMap {
        (fieldsMap) =>
        val currentYear = if (location.state.year == PageLocation.END) location.lastYear(fieldsMap(SELECTED_INPUT_YEARS_KEY))
                          else if (fieldsMap(CURRENT_INPUT_YEAR_KEY).isEmpty) location.firstYear(fieldsMap(SELECTED_INPUT_YEARS_KEY))
                          else fieldsMap(CURRENT_INPUT_YEAR_KEY).toInt
        location.updateForYear(location, currentYear).flatMap {
          (updatedLocation)=>
          e match {
            case Backward => {
              val nextYear = (updatedLocation move e).state.year
              if (nextYear != currentYear && !location.isInstanceOf[CheckYourAnswers])
                updateForYear(location, nextYear).map((p)=>p.update(p.state.copy(year=updatedLocation.state.year)))
              else
                Future.successful(updatedLocation)
            }
            case _ => Future.successful(updatedLocation)
          }
        }
      } andThen {
        case Failure(t) => location
        case Success(page) => page
      } flatMap {
        (page) =>
        val next = page move e
        Logger.info(s"${page} -> ${next}, saving next year as ${next.state.year}")
        keystore.store(next.state.year.toString, CURRENT_INPUT_YEAR_KEY).flatMap((values) => Future.successful(Redirect(next.action)))
      }
    }
  }
}

trait BaseFrontendController extends SessionProvider with FrontendController {
  this: SessionProvider =>

  val keys = List(KeystoreService.SCHEME_TYPE_KEY, 
                  KeystoreService.DB_FLAG_PREFIX, 
                  KeystoreService.DC_FLAG_PREFIX, 
                  KeystoreService.TRIGGER_DATE_KEY,
                  KeystoreService.CURRENT_INPUT_YEAR_KEY,
                  KeystoreService.SELECTED_INPUT_YEARS_KEY,
                  KeystoreService.TE_YES_NO_KEY,
                  KeystoreService.IS_EDIT_KEY
                  )
  def isValue(key: String): Boolean = {
    !(keys.contains(key) || key.startsWith(KeystoreService.DB_FLAG_PREFIX) || key.startsWith(KeystoreService.DC_FLAG_PREFIX))
  }

  implicit val marshall = {
    (key: String, value: Option[String]) =>
      if (!isValue(key)) {
        value match {
          case None => (key, "")
          case Some(v) => (key, v)
        }
      } else {
        value match {
          case None => (key, "")
          case Some("") => (key, "0")
          case Some("0") => (key, "0")
          case Some(v) => (key, f"${(v.toInt / 100.00)}%2.0f".trim)
        }
      }
  }

  def getSessionId()(implicit request : Request[AnyContent]) : Option[String] = request.session.get(SessionKeys.sessionId)

  /**
   * every session should have an ID: required by key-store
   * If no session Id is found or session was deleted (NOSESSION), a new session id will be issued
    *
    * @return redirect to start page if no session else action
   * 
   * Example usage:
   * def onPageLoad(...) = withSession { implicit request => etc. }
   */
  def withSession(f: => Request[AnyContent]=> Future[Result]) : Action[AnyContent] = Action.async {
    implicit request : Request[AnyContent] =>
      getSessionId match {
        case Some(NOSESSION) => Future.successful(Redirect(routes.StartPageController.startPage()).withNewSession.withSession(createSessionId()))
        case None => Future.successful(Redirect(routes.StartPageController.startPage()).withNewSession.withSession(createSessionId()))
        case _ => f(request)
      }
  }

  def formRequestData(implicit request : Request[AnyContent]): Map[String, String] = request.body.asFormUrlEncoded.getOrElse(Map[String, Seq[String]]()).mapValues(_.head)
}
