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

package service

import controllers._
import play.api.mvc._

sealed trait Event
object Forward extends Event
object Backward extends Event
object Edit extends Event

case class PageState(year: Int = PageLocation.PRESTART, selectedYears: String = "", isDC: Boolean = false, isTE: Boolean = false, isTI: Boolean = false, isEdit: Boolean = false, firstDCYear: Int = -1)

object PageLocation {
  val PRESTART = -1
  val START = -2
  val END = Integer.MAX_VALUE

  def toType(page: Any): Any = page match {
    case TaxYearSelection(_) => TaxYearSelection
    case SelectScheme(_) => SelectScheme
    case TriggerDate(_) => TriggerDate
    case TriggerAmount(_) => TriggerAmount
    case Start(_) => Start
    case CheckYourAnswers(_) => CheckYourAnswers
    case PensionInput(_) => PensionInput
    case YesNoTrigger(_) =>YesNoTrigger
    case YesNoIncome(_) => YesNoIncome
    case AdjustedIncome(_) => AdjustedIncome
    case _ => AnyRef
  }

  def apply(page: Any, pageState: PageState): PageLocation = page match {
    case TaxYearSelection => TaxYearSelection(pageState)
    case SelectScheme => SelectScheme(pageState)
    case TriggerDate => TriggerDate(pageState)
    case TriggerAmount => TriggerAmount(pageState)
    case Start => Start(pageState)
    case CheckYourAnswers => CheckYourAnswers(pageState)
    case PensionInput => PensionInput(pageState)
    case YesNoTrigger =>YesNoTrigger(pageState)
    case YesNoIncome => YesNoIncome(pageState)
    case AdjustedIncome => AdjustedIncome(pageState)
    case _ => Start(pageState)
  }
}

sealed trait PageLocation {
  def state(): PageState
  def action(): Call
  def firstYear(path: String): Int = path.split(",")(0).toInt
  def lastYear(path: String): Int = path.split(",").reverse(0).toInt
  
  def move(e: Event): PageLocation = {
    e match {
      case Forward => forward
      case Backward => backward
      case Edit => this
    }
  }

  protected def isSupported(): Boolean = true
  
  protected def backward(): PageLocation = {
    if (state.isEdit)
      CheckYourAnswers(state)
    else {
      val subJourney = pageOrder(state.year)
      val index = subJourney.indexOf(PageLocation.toType(this))
      if (index <= 0) previousSubJourney()
      else {
        val t = subJourney(index-1)
        val previousStep = PageLocation(t, state)
        if (!previousStep.isSupported)
          previousStep.backward
        else
          previousStep
      }
    }
  }
  
  protected def forward(): PageLocation = {
    if (state.isEdit)
      CheckYourAnswers(state())
    else {
      val subJourney = pageOrder(state.year)
      val index = subJourney.indexOf(PageLocation.toType(this)) + 1
      if (index >= subJourney.length) {
        nextSubJourney()
      }
      else { 
        val t = subJourney(index)
        PageLocation(t, state())
      }
    }
  }
  
  protected lazy val years: Array[Int] = state.selectedYears.split(",").map(_.toInt)

  protected def nextSubJourney() = {
    val t = pageOrder(nextYear)(0)
    PageLocation(t, state().copy(year=nextYear))
  }

  protected def previousSubJourney() = {
    val s = state().copy(year=previousYear)
    val t = pageOrder(previousYear).reverse.find(PageLocation(_,s).isSupported).getOrElse(preJourney(0))
    PageLocation(t, s)
  }

  private val preJourney = Vector(Start,TaxYearSelection)
  
  private val postJourney = Vector(CheckYourAnswers)
  
  private val subJournies = Map(("Pre2015"->Vector(PensionInput)),
                             ("2015"->Vector(SelectScheme,PensionInput,YesNoTrigger,TriggerDate,TriggerAmount)),
                             ("Post2015"->Vector(SelectScheme,PensionInput,YesNoIncome,AdjustedIncome,YesNoTrigger,TriggerDate,TriggerAmount)))

  
  private def pageOrder(year: Int) = if (year <= 0) preJourney 
                                     else if (year >= PageLocation.END) postJourney 
                                     else {
                                      val index = (if (year>2015) "Post2015" else if (year < 2015) "Pre2015" else "2015")
                                      subJournies(index)
                                    }

  private def nextYear(): Int =
    if (state.selectedYears.isEmpty)
      PageLocation.START
    else if ((years.indexOf(state.year) + 1) < years.length)
      years(years.indexOf(state.year) + 1)
    else PageLocation.END

  private def previousYear(): Int =
    if (state.selectedYears.isEmpty)
      PageLocation.END
    else if (state.year >= PageLocation.END)
      lastYear(state.selectedYears)
    else if ((years.indexOf(state.year) - 1) < 0)
      PageLocation.START
    else
      years((years.indexOf(state.year) - 1))

  def update(newState: PageState): PageLocation = PageLocation(PageLocation.toType(this), newState)
}

case class TaxYearSelection(state: PageState=PageState(year=PageLocation.START)) extends PageLocation {
  def action(): Call = controllers.routes.TaxYearSelectionController.onPageLoad()
}

case class SelectScheme(state: PageState=PageState()) extends PageLocation {
  def action(): Call = controllers.routes.SelectSchemeController.onPageLoad(state.year)
}

case class TriggerDate(state: PageState=PageState()) extends PageLocation {
  def action(): Call = controllers.routes.DateOfMPAATriggerEventController.onPageLoad()
  override def isSupported(): Boolean = state.isTE
}

case class TriggerAmount(state: PageState=PageState()) extends PageLocation {
  def action(): Call = controllers.routes.PostTriggerPensionInputsController.onPageLoad()
  override def isSupported(): Boolean = state.isTE
}

case class Start(state: PageState=PageState(year=PageLocation.PRESTART)) extends PageLocation {
  def action(): Call = controllers.routes.StartPageController.startPage()
  override protected def backward(): PageLocation = this
}

case class CheckYourAnswers(state: PageState=PageState(year=PageLocation.END)) extends PageLocation {
  def action(): Call = controllers.routes.ReviewTotalAmountsController.onPageLoad()
  override protected def forward(): PageLocation = this
}

case class PensionInput(state: PageState=PageState()) extends PageLocation {
  def action(): Call = if (state.year == 2015) controllers.routes.PensionInputs201516Controller.onPageLoad() 
                       else if (state.year > 2015) controllers.routes.PensionInputs201617Controller.onPageLoad()
                       else controllers.routes.PensionInputsController.onPageLoad()
  override protected def forward(): PageLocation = if (state.year >= 2016) super.forward
                                                   else if (YesNoTrigger(state).isSupported) YesNoTrigger(state)
                                                   else nextSubJourney
}

case class YesNoTrigger(state: PageState=PageState()) extends PageLocation {
  override def isSupported(): Boolean = state.year == state.firstDCYear
  def action(): Call = controllers.routes.YesNoMPAATriggerEventAmountController.onPageLoad()
  override protected def forward(): PageLocation = if (state.isTE) super.forward else nextSubJourney
}

case class YesNoIncome(state: PageState=PageState()) extends PageLocation {
  def action(): Call = controllers.routes.YesNoThresholdIncomeController.onPageLoad()
  override protected def forward(): PageLocation = if (state.isTI) super.forward 
                                                   else if (YesNoTrigger(state).isSupported()) YesNoTrigger(state) 
                                                   else nextSubJourney
}

case class AdjustedIncome(state: PageState=PageState()) extends PageLocation {
  override def isSupported(): Boolean = state.isTI
  def action(): Call = controllers.routes.AdjustedIncome1617InputController.onPageLoad()
  override protected def forward(): PageLocation = if (state.isDC) YesNoTrigger(state) else nextSubJourney
}