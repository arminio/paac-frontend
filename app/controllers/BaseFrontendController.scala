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
  val CurrentYear : String = "Current"
  val SelectedYears : String = "SelectedYears"

  def wheretoNext[T](defaultRoute: Result)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]) : Future[Result] = {
    val reads: List[Future[(String, String)]] = List(CurrentYear, SelectedYears).map {
      (key) =>
        keystore.read[String](key).map {
          case None => (key, "")
          case Some(v) => (key, v)
        }
    }

    Future.sequence(reads).map {
      (fields) =>
        val fieldsMap = Map[String, String](fields: _*)
        val currentYear = fieldsMap(CurrentYear)
        val selectedYears = fieldsMap(SelectedYears)
        val syears = selectedYears.split(",")
        val nextYear = if (currentYear == "") {
          syears(0).toInt
        } else {
          val i = syears.indexOf(currentYear) + 1
          if (i < syears.length) {
            syears(i).toInt
          } else {
            -1
          }
        }


        // Save next year value to keystore with CurrentYear
        keystore.store(nextYear, CurrentYear)
        //redirect to nextYear Controller
        if (nextYear == -1) {
          defaultRoute
        }
        else if (nextYear < 2015) {
          Redirect(routes.StartPageController.startPage())
        } else if (nextYear == 2015) {
          Redirect(routes.PensionInputs1516Period1Controller.onPageLoad())
        } else {
            // 2016
          Redirect(routes.PensionInputsController.onPageLoad())
          }
    }
  }
}

trait BaseFrontendController extends SessionProvider with FrontendController {
  this: SessionProvider =>

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
