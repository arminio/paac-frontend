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

package controllers.action

import uk.gov.hmrc.play.http.SessionKeys
import scala.concurrent.Future
import play.api.mvc._
import java.util.UUID

trait SessionProvider {
  def createSessionId()(implicit request: Request[_]): (String, String) =
    SessionKeys.sessionId -> s"session-${UUID.randomUUID}"

  def createKeystoreSession()(implicit request: Request[_]): Session =
    Session(request.session.data + createSessionId())
}

trait CheckSessionAction extends ActionBuilder[Request] with ActionFilter[Request] with SessionProvider {
  sessionProvider: SessionProvider =>
  val NOSESSION = "NOSESSION"

  def filter[A](r: Request[A]): Future[Option[Result]] = {
    implicit val request = r
    val result = request.session.get(SessionKeys.sessionId) match {
      case Some(NOSESSION) => Some(redirectTo.withNewSession.withSession(sessionProvider.createSessionId()))
      case None => Some(redirectTo.withNewSession.withSession(sessionProvider.createSessionId()))
      case _ => None
    }
    Future.successful(result)
  }

  protected lazy val redirectTo = Results.Redirect(service.PageLocation.start.action)
}

object CheckSessionAction extends CheckSessionAction