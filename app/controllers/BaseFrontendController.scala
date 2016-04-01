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
import play.api.libs.concurrent.Execution.Implicits._

import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.SessionKeys

// See https://github.tools.tax.service.gov.uk/DDCN/cc-frontend/blob/master/app/controllers/keystore/Session.scala

trait SessionProvider {
  val NOSESSION = "NOSESSION"
  def createSessionId()(implicit request: Request[AnyContent]) : (String, String) = SessionKeys.sessionId -> s"session-${UUID.randomUUID}"

  def createKeystoreSession()(implicit request: Request[AnyContent]) : Session = {
    Session(request.session.data + createSessionId())
  }
}

trait BaseFrontendController extends SessionProvider with FrontendController {
  this: SessionProvider =>

  def getSessionId()(implicit request : Request[AnyContent]) : Option[String] = request.session.get(SessionKeys.sessionId)

  /**
   * every session should have an ID: required by key-store
   * If no session Id is found or session was deleted (NOSESSION), a new session id will be issued
   * @return redirect to start page if no session else action
   * 
   * Example usage:
   * def onPageLoad(...) = withSession { implicit request => etc. }
   */
  def withSession(f: => Request[AnyContent]=> Future[Result]) : Action[AnyContent] = Action.async {
    implicit request : Request[AnyContent] =>
      getSessionId match {
        case Some(NOSESSION) => Future.successful(Results.Redirect(routes.CalculatorController.onPageLoad()).withSession(createKeystoreSession()))
        case None => Future.successful(Results.Redirect(routes.CalculatorController.onPageLoad()).withSession(createKeystoreSession()))
        case _ => f(request)
      }
  }
}
