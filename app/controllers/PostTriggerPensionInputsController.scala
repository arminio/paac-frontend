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
import form._

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

  def id(c: Contribution): String = {
    if (c.isPeriod1()) {
      "year2015.postTriggerDcAmount2015P1"
    } else if (c.isPeriod2()) {
      "year2015.postTriggerDcAmount2015P2"
    } else {
      "triggerAmounts.amount_"+c.taxPeriodStart.year
    }
  }

  def key(c: Contribution): String = {
    if (c.isPeriod1()) {
      "postTriggerDefinedContribution_2015_p1"
    } else if (c.isPeriod2()) {
      "postTriggerDefinedContribution_2015_p2"
    } else {
      "postTriggerDefinedContribution_"+c.taxPeriodStart.year
    }
  }

  def amount(c: Contribution, form: CalculatorFormFields): Long = {
    if (c.isPeriod1()) {
      form.year2015.postTriggerDcAmount2015P1.map((_ * 100L).toLong).getOrElse(0L)
    } else if (c.isPeriod2()) {
      form.year2015.postTriggerDcAmount2015P2.map((_ * 100L).toLong).getOrElse(0L)
    } else {
      0L
    }
  }

  val onPageLoad = withSession { implicit request =>
    keystore.read[String](DateOfMPAATriggerEventController.dateOfMPAATEKey).flatMap {
      (date) =>
        val dateAsStr = date.getOrElse("")
        if (dateAsStr == "") {
          Future.successful(Redirect(routes.DateOfMPAATriggerEventController.onPageLoad))
        } else {
          val c = toContribution(dateAsStr)
          keystore.read[String](key(c)).flatMap {
            (value) =>
            val v = BigDecimal(value.toInt/100L)
            Future.successful(Ok(views.html.postTriggerPensionInputs(CalculatorForm.form, c, id(c), v)))
          }
        }
    }
  }

  val onSubmit = withSession { implicit request =>
    keystore.read[String](DateOfMPAATriggerEventController.dateOfMPAATEKey).flatMap {
      (date) =>
      val dateAsStr = date.getOrElse("")
      if (dateAsStr == "") {
        Future.successful(Redirect(routes.DateOfMPAATriggerEventController.onPageLoad))
      } else {
        val c = toContribution(dateAsStr)
        val a = amount(c, input)
        CalculatorForm.form.bindFromRequest().fold(
          formWithErrors => { Future.successful( Ok(views.html.postTriggerPensionInputs(CalculatorForm.form, c, id(c), a)) ) },
          input => {
            keystore.store[String](a.toString, key(c)).flatMap {
              (_) => 
              Future.successful(Redirect(routes.ReviewTotalAmountsController.onPageLoad))
            }
          }
        )
      }
    }
  }
}
