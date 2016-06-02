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

import form.YesNo1516Period2Form
import play.api.mvc._
import service.KeystoreService
import scala.concurrent.Future

object YesNo1516Period2Controller extends YesNo1516Period2Controller {
  override val keystore: KeystoreService = KeystoreService
}

trait YesNo1516Period2Controller extends RedirectController {
  val keystore: KeystoreService

  private val yesNoFormKey = "yesNo"
  private val onSubmitRedirectForYes: Call = routes.PensionInputs1516Period2Controller.onPageLoad()
  private val onSubmitRedirectForNo: Call = routes.PensionInputsController.onPageLoad()

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](KeystoreService.P2_YES_NO_KEY).map {
      (yesNo) =>
        val fields = Map(yesNo match {
          case Some(value) => (yesNoFormKey, value)
          case None => (yesNoFormKey, "Yes")
        })
        Ok(views.html.yesno_1516_period2(YesNo1516Period2Form.form.bind(fields).discardingErrors))
    }
  }

  val onSubmit = withSession { implicit request =>
    YesNo1516Period2Form.form.bindFromRequest().fold(
      formWithErrors => { Future.successful(Ok(views.html.yesno_1516_period2(YesNo1516Period2Form.form))) },
      input => {
        keystore.store(input, KeystoreService.P2_YES_NO_KEY)
        if (input == "Yes") {
          Future.successful(Redirect(onSubmitRedirectForYes))
        } else {
          keystore.read[String](KeystoreService.P1_YES_NO_KEY).flatMap {
            (yesNoP1) =>
            val yesOrNoP1 = yesNoP1.getOrElse("No")
            if (yesOrNoP1 == "No" && input == "No") {
              wheretoNext(Redirect(routes.StaticPageController.onPipTaxYearPageLoad()))
            } else {
              if (input == "No") {
                keystore.save(List(Some(("0",KeystoreService.P2_DB_KEY)), Some(("0",KeystoreService.P2_DC_KEY))), "")
              }
              keystore.read[String](KeystoreService.DC_FLAG).flatMap {
                (isDefinedContribution) =>
                if (isDefinedContribution.getOrElse("false").toBoolean){
                  Future.successful(Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))
                } else {
                  wheretoNext(Redirect(onSubmitRedirectForNo))
                }
              }
            }
          }
        }
      }
    )
  }
}
