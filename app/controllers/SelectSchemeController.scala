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


import service.KeystoreService
import play.api.mvc._
import scala.concurrent.Future
import form.SelectSchemeForm
import form.SelectSchemeKeys

object SelectSchemeController extends SelectSchemeController {
  override val keystore: KeystoreService = KeystoreService
}

trait SelectSchemeController  extends BaseFrontendController {
  val keystore: KeystoreService

  private val onSubmitRedirect: Call = routes.StaticPageController.onPipTaxYearPageLoad()

  val onPageLoad = withSession { implicit request =>

    val schemeType: List[Future[(String, String)]] = List(SelectSchemeForm.definedBenefit, SelectSchemeForm.definedContribution).map {
      (key) =>
        keystore.read[String](key).map {
          (value) => key match {
              case SelectSchemeForm.definedBenefit => (key, value.getOrElse("false"))
              case SelectSchemeForm.definedContribution => (key, value.getOrElse("false"))
              case _ => (key, "")
            }
        }
    }
    Future.sequence(schemeType).map {
      (fields) =>
        val fieldsMap = Map[String, String](fields: _*)
        Ok(views.html.selectScheme(SelectSchemeForm.form.bind(fieldsMap).discardingErrors))
    }
  }

  val onSubmit = withSession { implicit request =>
    SelectSchemeForm.form.bindFromRequest().fold(
      formWithErrors => Future.successful(Ok(views.html.selectScheme(SelectSchemeForm.form))),
      input => {

        keystore.store[String](input.definedBenefit.toString, SelectSchemeForm.definedBenefit)
        keystore.store[String](input.definedContribution.toString, SelectSchemeForm.definedContribution)
        Future.successful(Redirect(onSubmitRedirect))
      }
    )
  }
}



