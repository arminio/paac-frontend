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
import form.CalculatorForm

/**
  * 2015/16 Period-1 : Pre-Alignment Tax Year
  */
object PensionInputs1516Period1Controller extends PensionInputs1516Period1Controller {
  override val keystore: KeystoreService = KeystoreService
}

trait PensionInputs1516Period1Controller extends BaseFrontendController {
  val keystore: KeystoreService

  private val onSubmitRedirect: Call = routes.YesNo1516Period2Controller.onPageLoad()

  val onPageLoad = withSession { implicit request =>
    val reads: List[Future[(String, String)]] = List(KeystoreService.P1_DB_KEY, KeystoreService.P1_DC_KEY, KeystoreService.SCHEME_TYPE_KEY).map {
      (key) =>
        keystore.read[String](key).map {
          (value) =>
            if (key == KeystoreService.SCHEME_TYPE_KEY) {
              value match {
                case None => (key, "")
                case Some(v) => (key, v)
              }
            } else {
              value match {
                case None => (key, "")
                case Some("0") => (key, "0.00")
                case Some(v) => (key, f"${(v.toInt / 100.00)}%2.2f")
              }
            }
        }
    }

    Future.sequence(reads).map {
      (fields) =>
        val fieldsMap = Map[String, String](fields: _*)
        Ok(views.html.pensionInputs_1516_period1(CalculatorForm.bind(fieldsMap).discardingErrors, fieldsMap(KeystoreService.SCHEME_TYPE_KEY)))
    }
  }

  val onSubmit = withSession { implicit request =>
    CalculatorForm.form.bindFromRequest().fold(
      // TODO: When we do validation story, please forward this to onPageLoad method with selectedSchemeType
      formWithErrors => { Future.successful(Ok(views.html.pensionInputs_1516_period1(formWithErrors))) },
      input => {
        val saves = List((input.to1516Period1DefinedBenefit, KeystoreService.P1_DB_KEY), (input.to1516Period1DefinedContribution,KeystoreService.P1_DC_KEY)).map {
          (pair)=>
          val (dbAmount:Long, dbKey:String) = pair._1.getOrElse((0L,pair._2))
          keystore.store[String](dbAmount.toString, dbKey)
        }
        Future.sequence(saves).map((_)=>Redirect(onSubmitRedirect))
      }
    )
  }
}