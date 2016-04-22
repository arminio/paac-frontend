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

import form.YesNo1516Period1Form
import play.api.mvc._
import service.KeystoreService
import scala.concurrent.Future

object YesNo1516Period1Controller extends YesNo1516Period1Controller {
  override val keystore: KeystoreService = KeystoreService
}

trait YesNo1516Period1Controller extends BaseFrontendController {
  val keystore: KeystoreService

  private   val yesNoKesystoreKey = "yesnoFor1516P1"
  private   val yesNoFormKey = "yesNo"
  private val onSubmitRedirectForYes: Call = routes.PensionInputs1516Period1Controller.onPageLoad()
  private val onSubmitRedirectForNo: Call = routes.YesNo1516Period2Controller.onPageLoad()

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](yesNoKesystoreKey).map {
      (yesNo) =>
        val fields = Map(yesNo match {
          case Some(value) => (yesNoFormKey, value)
          case None => (yesNoFormKey, "Yes")
        })
        Ok(views.html.yesno_1516_period1(YesNo1516Period1Form.form.bind(fields).discardingErrors))
    }
  }

  val onSubmit = withSession { implicit request =>

    YesNo1516Period1Form.form.bindFromRequest().fold(
      formWithErrors => { Future.successful(Ok(views.html.yesno_1516_period1(YesNo1516Period1Form.form))) },
      input => {
        keystore.store[String](input, YesNo1516Period1Form.yesNo)
        if (input == "Yes") {
          Future.successful(Redirect(onSubmitRedirectForYes))
        } else {
          Future.successful(Redirect(onSubmitRedirectForNo))
        }
      }
    )
  }
}