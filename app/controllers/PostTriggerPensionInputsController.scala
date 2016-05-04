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
import models._

object PostTriggerPensionInputsController extends PostTriggerPensionInputsController {
  override val keystore: KeystoreService = KeystoreService
}

trait PostTriggerPensionInputsController extends RedirectController {
  val keystore: KeystoreService

  def toContribution(jodaDateString: String): Contribution = {
    val parts = jodaDateString.split("-").map(_.toInt)
    val taxPeriodDate = TaxPeriod(parts(0), parts(1)-1, parts(2))
    val c = Contribution(taxPeriodDate, taxPeriodDate, None)
    if (c.isPeriod1()) {
      Contribution(TaxPeriod.PERIOD_1_2015_START, TaxPeriod.PERIOD_1_2015_END, None)
    } else if (c.isPeriod2()) {
      Contribution(TaxPeriod.PERIOD_2_2015_START, TaxPeriod.PERIOD_2_2015_END, None)
    } else {
      Contribution(parts(0), 0L)
    }
  }

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](DateOfMPAATriggerEventController.dateOfMPAATEKey).map {
      (date) =>
        val dateAsStr = date.getOrElse("")
        if (dateAsStr == "") {
          Redirect(routes.DateOfMPAATriggerEventController.onPageLoad)
        } else {
          Ok(views.html.postTriggerPensionInputs(CalculatorForm.form, toContribution(dateAsStr)))
        }
    }
  }

  val onSubmit = withSession { implicit request =>
    Future.successful(Redirect(routes.ReviewTotalAmountsController.onPageLoad))
  }
}
