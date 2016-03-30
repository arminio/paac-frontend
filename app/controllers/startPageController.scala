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
import form.EligibilityForm
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import scala.concurrent.Future

object startPageController extends startPageController{
    override val connector: CalculatorConnector = CalculatorConnector
  }

  trait startPageController  extends FrontendController {
    val connector: CalculatorConnector

    val startPage = Action.async { implicit request =>
      Future.successful(Ok(views.html.startPage("")))
    }

    val onPageLoad:Action[AnyContent] = Action.async { implicit request =>
      Future.successful(Ok(views.html.startPage("")))
    }

    val onSubmit:Action[AnyContent] = Action.async { implicit request =>
      Future.successful(Ok(views.html.eligibility(EligibilityForm.form)))
    }
  }
