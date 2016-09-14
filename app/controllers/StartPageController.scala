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
import play.api.Play.current
import scala.concurrent.Future
import service._
import service.KeystoreService._
import config.AppSettings
import play.api.Logger
import play.api.i18n.Lang
import uk.gov.hmrc.play.http.BadGatewayException

object StartPageController extends StartPageController with AppSettings {
  def keystore: KeystoreService = KeystoreService
}

trait StartPageController extends RedirectController {
  val startPage = withWriteSession { implicit request =>
    val sessionData = request.data ++ Map((IS_EDIT_KEY, "false"))
    Start() go Forward.using(sessionData)
  }

  val newSession = withSession { implicit request =>
    keystore.clear.map {
      (_)=>
      Redirect(routes.StartPageController.startPage()).withNewSession
    } recover {
      case e: BadGatewayException =>
        Logger.error(s"[StartPageController] ${e.message}", e)
        throw e
    }
  }

  def setLanguage:Action[AnyContent] = withSession { implicit request =>
        val lang = request.getQueryString("lang").getOrElse("en")
        Logger.info("Language from request query is " + lang)
        implicit val newLang = Lang(lang)
        Logger.info("New language set to " + newLang.code)
        Future.successful(Redirect(routes.StartPageController.startPage).withLang(newLang))
  }
}
