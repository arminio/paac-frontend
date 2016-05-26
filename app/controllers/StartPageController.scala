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

import connector.CalculatorConnector
import play.api.mvc._
import scala.concurrent.Future
import service._

object StartPageController extends StartPageController{
    override val connector: CalculatorConnector = CalculatorConnector
    override val keystore: KeystoreService = KeystoreService
}

trait StartPageController extends BaseFrontendController {
    val connector: CalculatorConnector
    val keystore: KeystoreService

    private val onSubmitRedirect: Call = routes.SelectSchemeController.onPageLoad()

    val startPage = withSession { implicit request =>
      keystore.store[String](false.toString(), KeystoreService.IS_EDIT_KEY).map {
        (_) =>
        Ok(views.html.startPage(""))
      }
    }

    val newSession = withSession { implicit request =>
      Future.successful(Redirect(routes.StartPageController.startPage()).withNewSession.withSession(createSessionId()))
    }

    val onSubmit = withSession { implicit request =>
      Future.successful(Redirect(onSubmitRedirect))
    }
}
