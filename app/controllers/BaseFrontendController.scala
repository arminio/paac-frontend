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

import service.KeystoreService
import service.KeystoreService._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}


trait SessionProvider {
  val NOSESSION = "NOSESSION"
  def createSessionId()(implicit request: Request[AnyContent]) : (String, String) = SessionKeys.sessionId -> s"session-${UUID.randomUUID}"

  def createKeystoreSession()(implicit request: Request[AnyContent]) : Session = {
    Session(request.session.data + createSessionId())
  }
}

trait RedirectController extends BaseFrontendController {
  def keystore: KeystoreService

  def wheretoNext[T](defaultRoute: Result)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]) : Future[Result] = {
    implicit val marshall = KeystoreService.toStringPair _

    def next(currentYear: String, selectedYears: String): Int = {
      val syears = selectedYears.split(",")
      if (currentYear == "") {
        if (syears.size > 0 && selectedYears.length > 0){
          syears(0).toInt
        } else {
          -1
        }
      } else {
        val i = syears.indexOf(currentYear) + 1
        if (i < syears.length) {
          syears(i).toInt
        } else {
          -1
        }
      }
    }

    keystore.read(List(CURRENT_INPUT_YEAR_KEY, SELECTED_INPUT_YEARS_KEY)).flatMap {
      (fieldsMap) =>
      val currentYear = fieldsMap(KeystoreService.CURRENT_INPUT_YEAR_KEY)
      val selectedYears = fieldsMap(KeystoreService.SELECTED_INPUT_YEARS_KEY)
      val nextYear = next(currentYear, selectedYears)

      // Save next year value to keystore with CurrentYear
      keystore.store(nextYear.toString(), KeystoreService.CURRENT_INPUT_YEAR_KEY).map {
        (values) =>
        //redirect to nextYear Controller
        if (nextYear == -1) {
          defaultRoute
        } else if (nextYear > 2015) {
          //2016
          Redirect(routes.StartPageController.startPage())
        } else if (nextYear == 2015) {
          Redirect(routes.StaticPageController.onPipPageLoad())
        } else {
          Redirect(routes.PensionInputsController.onPageLoad())
        }
      }
    }
  }
}

trait BaseFrontendController extends SessionProvider with FrontendController {
  this: SessionProvider =>

  val keys = List(KeystoreService.SCHEME_TYPE_KEY, KeystoreService.DB_FLAG, KeystoreService.DC_FLAG, KeystoreService.TRIGGER_DATE_KEY)

  implicit val marshall = {
    (key: String, value: Option[String]) =>
      if (keys.contains(key)) {
        value match {
          case None => (key, "")
          case Some(v) => (key, v)
        }
      } else {
        value match {
          case None => (key, "")
          case Some("0") => (key, "0.00")
          case Some(v) => (key, f"${(v.toInt / 100.00)}%2.2f")
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
}
