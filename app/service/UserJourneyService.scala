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
object Forward extends Event {
  def using(data: Map[String,String]): Tuple2[Event, Map[String,String]] = {
    (this, data)
  }
}
object Backward extends Event {
  def using(data: Map[String,String]): Tuple2[Event, Map[String,String]] = {
    (this, data)
  }
}
object Edit extends Event {
  def using(data: Map[String,String]): Tuple2[Event, Map[String,String]] = {
    (this, data)
  }
}

/* Simple data holder for a page used to determine user journey flow. */
case class PageState(year: Int = PageLocation.PRESTART,
                     selectedYears: String = "",
                     isDC: Boolean = false,
                     isTE: Boolean = false,
                     isTI: Boolean = false,
                     isEdit: Boolean = false,
                     firstDCYear: Int = -1)

object PageLocation {
  val PRESTART = -1
  val START = -2
  val END = Integer.MAX_VALUE

  /* Shouldn't need this but .type is not available */
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

  /**
    Simple factory method to create a new PageLocation instance
    given it's type and state.*/
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

  def start(): PageLocation = Start()
}

/**
  PageLocation type used with concreate case classes below representing pages in the user journey.
 */
sealed trait PageLocation {
  /** Current state logic for the page. */
  def state(): PageState
  /** Controller action to display this page location. */
  def action(): Call

  /*
    Page order is generally determined by these lists where
    by default the journey moves forward through each list.
    Concrete PageLocation classes may override forward/backward
    to jump to new locations based on state.*/
  private val preJourney = Vector(Start,TaxYearSelection)
  private val postJourney = Vector(CheckYourAnswers)
  private val subJournies = Map(("Pre2015"->Vector(PensionInput)),
                                ("2015"->Vector(SelectScheme,PensionInput,YesNoTrigger,TriggerDate,TriggerAmount)),
                                ("Post2015"->Vector(SelectScheme,PensionInput,YesNoIncome,AdjustedIncome,YesNoTrigger,TriggerDate,TriggerAmount)))

  /**
    Direction to move: Forward, Backward or Edit (to jump to go to this page.=).
   */
  def move(e: Event): PageLocation = e match {
      case Forward => if (state.isEdit) CheckYourAnswers(state) else forward
      case Backward => if (state.isEdit) CheckYourAnswers(state) else backward
      case Edit => this
    }

  def firstYear(): Int = years(0)

  def lastYear(): Int = years.reverse(0)

  def firstYear(selectedYears: String): Int = if (selectedYears.isEmpty) PageLocation.START else selectedYears.split(",").map(_.toInt).head

  def lastYear(selectedYears: String): Int = if (selectedYears.isEmpty) PageLocation.END else selectedYears.split(",").map(_.toInt).reverse(0)

  def update(newState: PageState): PageLocation = PageLocation(PageLocation.toType(this), newState)

  /* Concrete classes may override these methods to control user journey flow. */
  /**
    isSupported returns true if the page should be displayed based on page
    state. At present only considered during backward movement. */
  protected def isSupported(): Boolean = true

  protected def backward(): PageLocation = if (pageOrder(state.year).indexOf(PageLocation.toType(this)) <= 0)
      previousSubJourney()
    else {
      val previousStep = PageLocation(pageOrder(state.year)(pageOrder(state.year).indexOf(PageLocation.toType(this))-1), state)
      if (!previousStep.isSupported)
        previousStep.backward
      else
        previousStep
    }

  protected def forward(): PageLocation = {
    val subJourney = pageOrder(state.year)
    val index = subJourney.indexOf(PageLocation.toType(this)) + 1
    if (index >= subJourney.length)
      nextSubJourney()
    else
      PageLocation(subJourney(index), state())
  }

  protected lazy val years: Array[Int] = state.selectedYears.split(",").map(_.toInt)

  protected def nextSubJourney() = PageLocation(pageOrder(nextYear)(0), state().copy(year=nextYear))

  protected def previousSubJourney() = {
    val s = state().copy(year=previousYear)
    // $COVERAGE-OFF$
    val t = pageOrder(previousYear).reverse.find(PageLocation(_,s).isSupported).getOrElse(preJourney(0))
    // $COVERAGE-ON$
    PageLocation(t, s)
  }

  private def pageOrder(year: Int) = if (year <= 0) preJourney
                                    else if (year >= PageLocation.END) postJourney
                                    else subJournies((if (year > 2015) "Post2015" else if (year < 2015) "Pre2015" else "2015"))

  private def nextYear(): Int = if (state.selectedYears.isEmpty)
      PageLocation.START
    else if ((years.indexOf(state.year) + 1) < years.length)
      years(years.indexOf(state.year) + 1)
    else PageLocation.END

  private def previousYear(): Int = if (state.selectedYears.isEmpty) {
      // $COVERAGE-OFF$
      PageLocation.END
      // $COVERAGE-ON$
    } else if (state.year >= PageLocation.END)
      lastYear
    else if ((years.indexOf(state.year) - 1) < 0)
      PageLocation.START
    else
      years((years.indexOf(state.year) - 1))
}

/* Page location classes */

case class TaxYearSelection(state: PageState=PageState(year=PageLocation.START)) extends PageLocation {
  def action(): Call = controllers.routes.TaxYearSelectionController.onPageLoad()
}

case class SelectScheme(state: PageState=PageState()) extends PageLocation {
  def action(): Call = controllers.routes.SelectSchemeController.onPageLoad(state.year)
}

case class TriggerDate(state: PageState=PageState()) extends PageLocation {
  def action(): Call = controllers.routes.DateOfMPAATriggerEventController.onPageLoad()
  override def isSupported(): Boolean = state.isTE && YesNoTrigger(state).isSupported
}

case class TriggerAmount(state: PageState=PageState()) extends PageLocation {
  def action(): Call = controllers.routes.PostTriggerPensionInputsController.onPageLoad()
  override def isSupported(): Boolean = state.isTE && YesNoTrigger(state).isSupported
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
                       else if (state.year > 2015) controllers.routes.PensionInputs201617Controller.onPageLoad(state.year)
                       else controllers.routes.PensionInputsController.onPageLoad()
  override protected def forward(): PageLocation = if (state.year >= 2016) super.forward
                                                   else if (YesNoTrigger(state).isSupported) YesNoTrigger(state)
                                                   else nextSubJourney
}

case class YesNoTrigger(state: PageState=PageState()) extends PageLocation {
  override def isSupported(): Boolean = ((!years.contains(2015) && state.year == state.firstDCYear) ||
                                          (years.contains(2015) && state.year == 2015 && state.firstDCYear > 0))
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
  override protected def forward(): PageLocation = if (YesNoTrigger(state).isSupported()) YesNoTrigger(state) else nextSubJourney
}