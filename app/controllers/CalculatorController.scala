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
import models._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import scala.concurrent.Future
import form.CalculatorForm

object CalculatorController extends CalculatorController{
  override val connector: CalculatorConnector = CalculatorConnector

}

trait CalculatorController  extends FrontendController{
  val connector: CalculatorConnector

  val onPageLoad = Action.async { implicit request =>
    Future.successful(Ok(views.html.calculator(CalculatorForm.form)))
  }

  val onSubmit = Action.async { implicit request =>
    CalculatorForm.form.bindFromRequest().fold(
      formWithErrors => { Future.successful(BadRequest) },
      input => { connector.connectToPAACService(input.toContributions()).map(response => Ok(views.html.results(response))) }
    )
  }
}



