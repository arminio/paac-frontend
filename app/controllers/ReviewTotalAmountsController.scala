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
import form._
import play.api.mvc._
import service._
import service.KeystoreService._
import uk.gov.hmrc.play.http.HeaderCarrier
import models._
import scala.concurrent.Future
import config.Settings
import config.AppSettings

object ReviewTotalAmountsController extends ReviewTotalAmountsController with AppSettings {
  def keystore: KeystoreService = KeystoreService
  override val connector: CalculatorConnector = CalculatorConnector
}

trait ReviewTotalAmountsController extends RedirectController with Settings {
  settings: Settings =>

  val EDIT_TRIGGER_AMOUNT = -4
  val EDIT_TRIGGER_DATE = -5
  val connector: CalculatorConnector

  val onPageLoad = withReadSession { implicit request =>
    val values = Map((TRIGGER_DATE_KEY, request.data.get(TRIGGER_DATE_KEY).getOrElse(""))) ++ request.data
    Future.successful(Ok(views.html.review_amounts(Contributions(values))))
  }

  def onEditAmount(year:Int) = withWriteSession { implicit request =>
    val sessionData = request.data ++ Map((IS_EDIT_KEY->"true"),(CURRENT_INPUT_YEAR_KEY->year.toString))
    val location = year match {
      case EDIT_TRIGGER_AMOUNT => TriggerAmount()
      case EDIT_TRIGGER_DATE => TriggerDate()
      case _ => PensionInput(PageState())
    }

    location go Edit.using(sessionData)
  }

  def onEditIncome(year:Int) = withWriteSession { implicit request =>
    val sessionData = request.data ++ Map((IS_EDIT_KEY->"true"),(CURRENT_INPUT_YEAR_KEY->year.toString))
    AdjustedIncome() go Edit.using(sessionData)
  }

  val onSubmit = withReadSession { implicit request =>
    val values = Map((TRIGGER_DATE_KEY, request.data.get(TRIGGER_DATE_KEY).getOrElse(""))) ++ request.data
    val triggerDate = request.data.get(TRIGGER_DATE_KEY).map(PensionPeriod.toPensionPeriod(_))
    val maybeTriggerAmount = triggerDate.flatMap((pp)=>if (pp.isPeriod1) request.data.get(P1_TRIGGER_DC_KEY)
                                                       else if (pp.isPeriod2) request.data.get(P2_TRIGGER_DC_KEY)
                                                       else request.data.get(TRIGGER_DC_KEY))
    val triggerAmount = if(!maybeTriggerAmount.forall(_.isEmpty)) maybeTriggerAmount.map(_.toLong).getOrElse(0L) else 0L
    val selectedYears = values(SELECTED_INPUT_YEARS_KEY).split(",").map(_.toInt)

    val contributions = Contributions(values)
    connector.connectToPAACService(contributions).flatMap{
      response =>
      Future.successful(Ok(views.html.results(response, selectedYears, triggerDate, triggerAmount)))
    }
  }

  val onBack = withWriteSession { implicit request =>
    val sessionData = request.data ++ Map((IS_EDIT_KEY->"false"),(CURRENT_INPUT_YEAR_KEY->PageLocation.END.toString))
    CheckYourAnswers() go Backward.using(sessionData)
  }
}
