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

import play.api.mvc._
import service.PageLocation._
import play.api.test._
import play.api.test.Helpers._
import scala.concurrent.Future
import service._

class UserJourneyServiceSpec extends test.BaseSpec with Results {
  def toInt(year: String): Int = if (year == "") PRESTART else year.toInt

  def fulltest(year: String, selectedYears: String, t: Any, isDC: Boolean, isTI: Boolean, isTE: Boolean, isEdit: Boolean = false, firstDCYear: Int = 2015): Option[String] = {
    val location = PageLocation(t, PageState(toInt(year), selectedYears, isDC, isTE, isTI, isEdit, firstDCYear))
    info(location.toString)
    redirectLocation(Redirect(location.action))
  }

  def testNoDC(year: String, selectedYears: String, t: Any): Option[String] = {
    fulltest(year, selectedYears, t, false, false, false)
  }

  "Going Forward" should {
    def fullforward(year: String,
                    selectedYears: String,
                    t: Any,
                    isDC: Boolean,
                    isTI: Boolean,
                    isTE: Boolean,
                    isEdit: Boolean = false,
                    firstDCYear: Int = 2015): Option[String] = {
      val location = PageLocation(t, PageState(toInt(year), selectedYears, isDC, isTE, isTI, isEdit, firstDCYear))
      val next = (location move Forward)
      info(s"${location} -> ${next}")
      redirectLocation(Redirect(next.action))
    }

    def forward(year: String, selectedYears: String, t: Any, isDC: Boolean, isTI: Boolean): Option[String] = {
      fullforward(year, selectedYears, t, isDC, isTI, true)
    }

    def forwardNoDC(year: String, selectedYears: String, t: Any): Option[String] = {
      forward(year, selectedYears, t, false, false)
    }

    "when no year gives prestart" in {
      testNoDC("", "2012", Start) shouldBe Some("/paac")
    }

    "when no year on forward goes to tax selection" in {
      forwardNoDC("", "2012", Start) shouldBe Some("/paac/taxyearselection")
    }

    "when on simple pre-2015 journey" should {
      "when TaxYearSelection on forward goes to first year inputs" in {
        forwardNoDC("-2", "2012", TaxYearSelection) shouldBe Some("/paac/pensionInputs")
      }

      "when on one year on forward goes to end" in {
        forwardNoDC("2012", "2012", PensionInput) shouldBe Some("/paac/review")
      }

      "when on last year on forward goes to end" in {
        forwardNoDC("2013", "2012,2013", PensionInput) shouldBe Some("/paac/review")
      }
    }

    "when on 2015 journey" should {
      "on 1st step goto scheme selection" in {
        forward("-2", "2015", TaxYearSelection, false, false) shouldBe Some("/paac/scheme/2015")
        forward("-2", "2015", TaxYearSelection, true, false) shouldBe Some("/paac/scheme/2015")
      }
      "on 2nd step goto inputs" in {
        forward("2015", "2015", SelectScheme, false, false) shouldBe Some("/paac/pensionInputs201516")
        forward("2015", "2015", SelectScheme, true, false) shouldBe Some("/paac/pensionInputs201516")
      }
      "on 3rd step goto end if dc is false" in {
        fullforward("2015", "2015", PensionInput, false, false, false, false, 0) shouldBe Some("/paac/review")
      }
      "on 3rd step goto trigger yes/no if dc is true" in {
        forward("2015", "2015", PensionInput, true, false) shouldBe Some("/paac/yesnompaate")
      }
      "on 4th step goto trigger date if dc and te is true" in {
        fullforward("2015", "2015", YesNoTrigger, true, false, true) shouldBe Some("/paac/dateofmpaate")
      }
      "on 4th step next year if te is false" in {
        fullforward("2015", "2015", YesNoTrigger, true, false, false) shouldBe Some("/paac/review")
        fullforward("2015", "2015,2014", YesNoTrigger, true, false, false) shouldBe Some("/paac/pensionInputs")
      }
      "on 5th step goto trigger amount if dc is true" in {
        fullforward("2015", "2015", TriggerDate, true, false, true) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
      }
      "on 6th step goto review if dc is true and only one year selected" in {
        fullforward("2015", "2015", TriggerAmount, true, false,true) shouldBe Some("/paac/review")
      }
      "on 6th step goto review if dc is true if more than one year selected" in {
        fullforward("2015", "2015,2014", TriggerAmount, true, false,true) shouldBe Some("/paac/pensionInputs")
      }
    }

    "when on 2016 journey" should {
      "on 1st step goto scheme selection" in {
        forward("-2", "2016", TaxYearSelection, false, false) shouldBe Some("/paac/scheme/2016")
        forward("-2", "2016", TaxYearSelection, true, false) shouldBe Some("/paac/scheme/2016")
      }
      "on 2nd step goto post 2015 inputs" in {
        forward("2016", "2016", SelectScheme, false, false) shouldBe Some("/paac/pensionInputsPost2015")
        forward("2016", "2016", SelectScheme, true, false) shouldBe Some("/paac/pensionInputsPost2015")
      }
      "on 3rd step goto post 2015 inputs" in {
        forward("2016", "2016", PensionInput, false, false) shouldBe Some("/paac/yesnothresholdincome")
        forward("2016", "2016", PensionInput, true, false) shouldBe Some("/paac/yesnothresholdincome")
      }
      "on 4th step goto threshold income if ti is true" in {
        forward("2016", "2016", YesNoIncome, false, true) shouldBe Some("/paac/adjustedincome")
      }
      "on 4th step goto trigger yes/no if dc is true, ti is false, and 2015 not selected" in {
        fullforward("2016", "2016", YesNoIncome, true, false, false, false, 2016) shouldBe Some("/paac/yesnompaate")
        fullforward("2016", "2016,2014", YesNoIncome, true, false, false, false, 2016) shouldBe Some("/paac/yesnompaate")
      }
      "on 4th step goto next year if ti is false and only one year" in {
        forward("2016", "2016,2015", YesNoIncome, false, false) shouldBe Some("/paac/scheme/2015")
        forward("2016", "2016,2014", YesNoIncome, false, false) shouldBe Some("/paac/pensionInputs")
      }
      "on 5th step goto trigger yes/no if dc and te is true (only when 2015 not selected)" in {
        fullforward("2016", "2016", AdjustedIncome, true, false, true, false, 2016) shouldBe Some("/paac/yesnompaate")
      }
      "on 5th step goto next year if dc is false" in {
        forward("2016", "2016,2014", AdjustedIncome, false, true) shouldBe Some("/paac/pensionInputs")
      }
      "on 5th step goto next year if dc is false and 2015 selected" in {
        forward("2016", "2016,2015", AdjustedIncome, false, true) shouldBe Some("/paac/scheme/2015")
      }
      "on 6th step goto trigger date if dc is true" in {
        fullforward("2016", "2016", YesNoTrigger, true, true, true) shouldBe Some("/paac/dateofmpaate")
      }
      "on 7th step goto trigger date" in {
        fullforward("2016", "2016", TriggerDate, true, true, true) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
      }
      "on 8th step goto next year" in {
        fullforward("2016", "2016,2015", TriggerAmount, true, true,true) shouldBe Some("/paac/scheme/2015")
        fullforward("2016", "2016,2014", TriggerAmount, true, true,true) shouldBe Some("/paac/pensionInputs")
      }
      "on 8th step goto end if only one year" in {
        fullforward("2016", "2016", TriggerAmount, true, true,true) shouldBe Some("/paac/review")
      }
    }
  }

  "Going Backwards" should {
    def backwards(year: Int, selectedYears: String, t: Any, isDC: Boolean = false, isTI: Boolean = false, isTE: Boolean = false, isEdit: Boolean = false, firstDCYear: Int = 2015): Option[String] = {
      val location = PageLocation(t, PageState(year, selectedYears, isDC, isTE, isTI, isEdit, firstDCYear))
      val next = (location move Backward)
      info(s"${next} <- ${location}")
      redirectLocation(Redirect(next.action))
    }

    "when on simple pre-2015 journey" should {
      "when showing review page on backwards goes to last year inputs" in {
        backwards(END, "2014,2013,2012", CheckYourAnswers) shouldBe Some("/paac/pensionInputs")
      }
      "when showing pension input page on backwards goes to pension inputs" in {
        backwards(2012, "2014,2013,2012", PensionInput) shouldBe Some("/paac/pensionInputs")
      }
      "when showing first pension input page on backwards goes to taxyearselection" in {
        backwards(2014, "2014,2013,2012", PensionInput) shouldBe Some("/paac/taxyearselection")
      }
    }

    "when on 2015 journey" should {
      "when showing review page backwards goes to trigger amount page if triggered is true" in {
        backwards(END, "2016,2015", CheckYourAnswers, true, false, true) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
      }
      "when showing review page backwards goes to did trigger page if triggered is false" in {
        backwards(END, "2016,2015", CheckYourAnswers, true, false, false) shouldBe Some("/paac/yesnompaate")
      }
      "when dc is false" should {
        "when te is false" should {
          "when showing pension savings page backwards goes to threshold income yes/no" in {
            backwards(2014, "2015,2014", PensionInput, false, false, false, false, 0) shouldBe Some("/paac/pensionInputs201516")
          }
          "when showing pension savings page backwards goes to threshold input when ti is true" in {
            backwards(2014, "2015,2014", PensionInput, false, true, false, false, 0) shouldBe Some("/paac/pensionInputs201516")
          }
        }
        "when te is true" should {
          "when showing pension savings page backwards goes to trigger amount when ti is false" in {
            backwards(2014, "2015,2014", PensionInput, false, false, true) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
          }
          "when showing pension savings page backwards goes to trigger amount when ti is true" in {
            backwards(2014, "2015,2014", PensionInput, false, true, true) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
          }
        }
      }
      "when dc is true" should {
        "when te is false" should {
          "when showing pension savings page backwards goes to threshold income yes/no" in {
            backwards(2014, "2015,2014", PensionInput, true, false, false) shouldBe Some("/paac/yesnompaate")
          }
          "when showing pension savings page backwards goes to threshold input when ti is true" in {
            backwards(2014, "2015,2014", PensionInput, true, true, false) shouldBe Some("/paac/yesnompaate")
          }
        }
        "when te is true" should {
          "when showing pension savings page backwards goes to trigger amount when ti is false" in {
            backwards(2014, "2015,2014", PensionInput, true, false, true) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
          }
          "when showing pension savings page backwards goes to trigger amount when ti is true" in {
            backwards(2014, "2015,2014", PensionInput, true, true, true) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
          }
        }
      }
    }

    "when on 2016 journey" should {
      "when on 2014/2015" should {
        "when dc is false" should {
          "when te is false" should {
            "when showing pension savings page backwards goes to threshold income yes/no" in {
              backwards(2014, "2016,2014", PensionInput, false, false, false) shouldBe Some("/paac/yesnothresholdincome")
              backwards(2015, "2016,2015", SelectScheme, false, false, false) shouldBe Some("/paac/yesnothresholdincome")
            }
            "when showing pension savings page backwards goes to threshold input when ti is true" in {
              backwards(2014, "2016,2014", PensionInput, false, true, false) shouldBe Some("/paac/adjustedincome")
              backwards(2015, "2016,2015", SelectScheme, false, true, false) shouldBe Some("/paac/adjustedincome")
            }
          }
          "when te is true" should {
            "when showing pension savings page backwards goes to trigger amount when ti is false (only when 2015 not selected)" in {
              backwards(2014, "2016,2014", PensionInput, false, false, true, false, 2016) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
              backwards(2015, "2016,2015", PensionInput, false, false, true, false, 2016) shouldBe Some("/paac/scheme/2015")
            }
            "when showing pension savings page backwards goes to trigger amount when ti is true (only when 2015 not selected)" in {
              backwards(2014, "2016,2014", PensionInput, false, true, true, false, 2016) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
              backwards(2015, "2016,2015", SelectScheme, false, true, true, false, 2016) shouldBe Some("/paac/adjustedincome")
            }
          }
        }
        "when dc is true" should {
          "when te is false" should {
            "when showing pension savings page backwards goes to threshold income yes/no (only when 2015 not selected)" in {
              backwards(2014, "2016,2014", PensionInput, true, false, false, false, 2016) shouldBe Some("/paac/yesnompaate")
              backwards(2014, "2016,2014", PensionInput, true, true, false, false, 2016) shouldBe Some("/paac/yesnompaate")
            }
            "when showing pension savings page backwards when 2015 selected" in {
              backwards(2015, "2016,2015", SelectScheme, true, false, false, false, 2016) shouldBe Some("/paac/yesnothresholdincome")
              backwards(2015, "2016,2015", SelectScheme, true, true, false,false,  2016) shouldBe Some("/paac/adjustedincome")
            }
          }
          "when te is true" should {
            "when showing pension savings page backwards goes to trigger date when ti is false and 2015 not selected" in {
              backwards(2014, "2016,2014", PensionInput, true, false, true, false, 2016) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
              //backwards(2015, "2016,2015", SelectScheme, true, true, true, false, 2016) shouldBe Some("/paac/adjustedincome")
            }
            "when showing pension savings page backwards goes to trigger amount when ti is false" in {
              backwards(2014, "2016,2014", PensionInput, true, false, true, false, 2016) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
              //backwards(2015, "2016,2015", SelectScheme, true, true, true, false, 2016) shouldBe Some("/paac/adjustedincome")
            }
            "when showing pension savings page backwards goes to trigger amount when ti is true" in {
              backwards(2014, "2016,2014", PensionInput, true, true, true, false, 2016) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
              //backwards(2015, "2016,2015", SelectScheme, true, true, true, false, 2016) shouldBe Some("/paac/adjustedincome")
            }
          }
        }
      }
    }
  }

  "toType" should {
    "return AnyRef when given an unknown page location type" in {
      PageLocation.toType(new Object()) shouldBe AnyRef
    }
  }

  "apply" should {
    "return Start when given an unknown page location type" in {
      PageLocation(AnyRef, PageState()) shouldBe Start(PageState())
    }
  }

  "first year" should {
    "return first selected year as int" in {
      PageLocation(Start(), PageState(selectedYears="2008,2010")).firstYear shouldBe 2008
    }
    "return start when no selected years" in {
      PageLocation(Start(), PageState(selectedYears="2014")).firstYear shouldBe 2014
      PageLocation(Start(), PageState(selectedYears="2014")).firstYear("") shouldBe PageLocation.START
      PageLocation(Start(), PageState(selectedYears="2014")).firstYear("2013,2015") shouldBe 2013
    }
  }

  "backward" should {
    "when landing on unsupported page location go to first supported page location" in {
      (TriggerAmount(PageState(2016, "2016,2015", firstDCYear=2015)) move Backward) shouldBe YesNoIncome(PageState(2016,"2016,2015",false,false,false,false,2015))
    }
  }

  "when selected years is empty" should {
    "on forward" in {
      (TaxYearSelection(PageState(2015, "")) move Forward) shouldBe SelectScheme(PageState(2015,"",false,false,false,false,-1))
    }
    "on backward" in {
      (Start(PageState(2015, "")) move Backward) shouldBe Start(PageState(2015,"",false,false,false,false,-1))
    }
    "nextYear should return START" should {
      (TriggerAmount(PageState(2015,"")) move Forward).state.year shouldBe PageLocation.START
    }
    "previousYear should return END" should {
      (PensionInput(PageState(2015,"")) move Backward).state.year shouldBe 2015
    }
  }
}
