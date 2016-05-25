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

package models

import org.scalatest.BeforeAndAfterAll
import play.api.Play
import play.api.test._

trait Year2016 extends models.ThisYear {
  override def THIS_YEAR = 2016
}

class CalculatorFormFieldSpec extends ModelSpec with BeforeAndAfterAll {
  val app = FakeApplication()

  override def beforeAll() {
    Play.start(app)
    super.beforeAll() // To be stackable, must call super.beforeEach
  }

  override def afterAll() {
    try {
      super.afterAll()
    } finally Play.stop()
  }

  "Amounts" should {
    "isEmpty" should {
      "return false if any of the values are not None" in {
        // set up
        val a1 = Amounts(Some(BigDecimal(123)))
        val a2 = Amounts(None, Some(BigDecimal(123)))
        val a3 = Amounts(None, None, Some(BigDecimal(123)))
        val a4 = Amounts(None, None, None, Some(BigDecimal(123)))
        val a5 = Amounts(None, None, None, None, Some(BigDecimal(123)))
        val a6 = Amounts(None, None, None, None, None, Some(BigDecimal(123)))
        val a7 = Amounts(None, None, None, None, None, None, Some(BigDecimal(123)))
        val a8 = Amounts(None, None, None, None, None, None, None, Some(BigDecimal(123)))

        // test
        val result1 = a1.isEmpty
        val result2 = a2.isEmpty
        val result3 = a3.isEmpty
        val result4 = a4.isEmpty
        val result5 = a5.isEmpty
        val result6 = a6.isEmpty
        val result7 = a7.isEmpty
        val result8 = a8.isEmpty

        // check
        result1 shouldBe false
        result2 shouldBe false
        result3 shouldBe false
        result4 shouldBe false
        result5 shouldBe false
        result6 shouldBe false
        result7 shouldBe false
        result8 shouldBe false
      }

      "return true when all values are None" in {
        // set up
        val a = Amounts(None, None, None, None, None, None, None, None)

        // test
        val result = a.isEmpty

        // check
        result shouldBe true
      }

      "return true when using default values" in {
        // set up
        val a = Amounts()

        // test
        val result = a.isEmpty

        // check
        result shouldBe true
      }
    }
  }

  "Year2015Amounts" should {
    "isEmpty" should {
      "return false if any of the values are not None" in {
        // set up
        val a1 = Year2015Amounts(Some(BigDecimal(123)))
        val a2 = Year2015Amounts(None, Some(BigDecimal(123)))
        val a3 = Year2015Amounts(None, None, Some(BigDecimal(123)))
        val a4 = Year2015Amounts(None, None, None, Some(BigDecimal(123)))
        val a5 = Year2015Amounts(None, None, None, None, Some(BigDecimal(123)))
        val a6 = Year2015Amounts(None, None, None, None, None, Some(BigDecimal(123)))

        // test
        val result1 = a1.isEmpty
        val result2 = a2.isEmpty
        val result3 = a3.isEmpty
        val result4 = a4.isEmpty
        val result5 = a5.isEmpty
        val result6 = a6.isEmpty

        // check
        result1 shouldBe false
        result2 shouldBe false
        result3 shouldBe false
        result4 shouldBe false
        result5 shouldBe false
        result6 shouldBe false
      }

      "return true when all values are None" in {
        // set up
        val a = Amounts(None, None, None, None, None, None)

        // test
        val result = a.isEmpty

        // check
        result shouldBe true
      }

      "return true when using default values" in {
        // set up
        val a = Amounts()

        // test
        val result = a.isEmpty

        // check
        result shouldBe true
      }
    }

    "hasDefinedContributions" should {
      "be true when any dc field is not None" in {
        // set up
        val a2 = Year2015Amounts(None, Some(BigDecimal(123)))
        val a4 = Year2015Amounts(None, None, None, Some(BigDecimal(123)))
        val a5 = Year2015Amounts(None, None, None, None, Some(BigDecimal(123)))
        val a6 = Year2015Amounts(None, None, None, None, None, Some(BigDecimal(123)))

        // test
        val result2 = a2.hasDefinedContributions
        val result4 = a4.hasDefinedContributions
        val result5 = a5.hasDefinedContributions
        val result6 = a6.hasDefinedContributions

        // check
        result2 shouldBe true
        result4 shouldBe true
        result5 shouldBe true
        result6 shouldBe true
      }

      "be false when all dc fields is None" in {
        // set up
        val year2015 = Year2015Amounts()

        // test
        val result = year2015.hasDefinedContributions

        // check
        result shouldBe false
      }
    }

    "hasDefinedBenefits" should {
      "be true when any db field is not None" in {
        // set up
        val a1 = Year2015Amounts(Some(BigDecimal(123)))
        val a3 = Year2015Amounts(None, None, Some(BigDecimal(123)))

        // test
        val result1 = a1.hasDefinedBenefits
        val result3 = a3.hasDefinedBenefits

        // check
        result1 shouldBe true
        result3 shouldBe true
      }

      "be false when all dc fields is None" in {
        // set up
        val year2015 = Year2015Amounts()

        // test
        val result = year2015.hasDefinedBenefits

        // check
        result shouldBe false
      }
    }
  }

  "CalculatorFormFields" should {
    "convert values to pence amounts" in {
      // set up
      val input = CalculatorFormFields(Amounts(Some(50.50), Some(90.50), Some(100.50), Some(200.50), Some(300.50),
        Some(400.50), Some(500.50), Some(600.50)),
        Amounts(Some(700.50), Some(800.50), Some(900.50), Some(1000.50), Some(1100.50),
          Some(1200.50), Some(1300.50), Some(1400.50)),
        Year2015Amounts(Some(1700.50), Some(1600.50), Some(1500.50), Some(1800.50), None, None), None)

      // test
      val maybeTuple = input.toDefinedBenefit(config.PaacConfiguration.year())

      // check
      maybeTuple should not be None
      maybeTuple.get._1 shouldBe 150050L
      maybeTuple.get._2 shouldBe "definedBenefit_" + config.PaacConfiguration.year()
    }
  }

  "toPageValues" should {
    "return this year's and 8 previous years contributions converting big decimal to pence" in {
      // set up
      val definedBenefits = Amounts(Some(BigDecimal(123)))
      val definedContributions = Amounts(Some(BigDecimal(456)))
      val year2015 = Year2015Amounts()
      val triggerDate = None
      val formFields = new CalculatorFormFields(definedBenefits,
                                                definedContributions,
                                                year2015,
                                                triggerDate) with Year2016

      // test
      val contributions = formFields.toPageValues

      // check
      contributions.length shouldBe 10
      contributions(0).taxPeriodStart.year shouldBe 2008
      contributions(1).taxPeriodStart.year shouldBe 2009
      contributions(2).taxPeriodStart.year shouldBe 2010
      contributions(3).taxPeriodStart.year shouldBe 2011
      contributions(4).taxPeriodStart.year shouldBe 2012
      contributions(5).taxPeriodStart.year shouldBe 2013
      contributions(6).taxPeriodStart.year shouldBe 2014
      contributions(7).taxPeriodStart.year shouldBe 2015
      contributions(8).taxPeriodStart.year shouldBe 2015
      contributions(9).taxPeriodStart.year shouldBe 2016
      contributions(9).amounts.get.definedBenefit.get shouldBe 12300
      contributions(9).amounts.get.moneyPurchase.get shouldBe 45600
    }

    "return contributions for post trigger period 1 and 2 moneyPurchase amounts" in {
      // set up
      val definedBenefits = Amounts(Some(BigDecimal(123)))
      val definedContributions = Amounts(Some(BigDecimal(456)))
      val year2015 = Year2015Amounts(None, Some(444), None, Some(555), Some(888), None)
      val triggerDate = Some("2015-4-12")
      val formFields = new CalculatorFormFields(definedBenefits,
                                                definedContributions,
                                                year2015,
                                                triggerDate) with Year2016

      // test
      val contributions = formFields.toPageValues

      // check
      contributions.length shouldBe 11
      contributions(0).taxPeriodStart.year shouldBe 2008
      contributions(1).taxPeriodStart.year shouldBe 2009
      contributions(2).taxPeriodStart.year shouldBe 2010
      contributions(3).taxPeriodStart.year shouldBe 2011
      contributions(4).taxPeriodStart.year shouldBe 2012
      contributions(5).taxPeriodStart.year shouldBe 2013
      contributions(6).taxPeriodStart.year shouldBe 2014
      contributions(7).taxPeriodStart.year shouldBe 2015
      contributions(8).taxPeriodStart.year shouldBe 2015
      contributions(9).taxPeriodStart.year shouldBe 2015

      contributions(7).amounts.get.moneyPurchase.get shouldBe 88800
      contributions(7).amounts.get.triggered.get shouldBe true
      contributions(8).amounts.get.moneyPurchase.get shouldBe 55500
      contributions(8).amounts.get.triggered.get shouldBe true
      contributions(9).amounts.get.moneyPurchase.get shouldBe 44400
      contributions(9).amounts.get.triggered.get shouldBe false
    }

    "return contributions for post trigger period 2 moneyPurchase amounts" in {
      // set up
      val definedBenefits = Amounts(Some(BigDecimal(123)))
      val definedContributions = Amounts(Some(BigDecimal(456)))
      val year2015 = Year2015Amounts(None, Some(444), None, Some(888), None, Some(555))
      val triggerDate = Some("2015-11-12")
      val formFields = new CalculatorFormFields(definedBenefits,
                                                definedContributions,
                                                year2015,
                                                triggerDate) with Year2016

      // test
      val contributions = formFields.toPageValues

      // check
      contributions.length shouldBe 11
      contributions(0).taxPeriodStart.year shouldBe 2008
      contributions(1).taxPeriodStart.year shouldBe 2009
      contributions(2).taxPeriodStart.year shouldBe 2010
      contributions(3).taxPeriodStart.year shouldBe 2011
      contributions(4).taxPeriodStart.year shouldBe 2012
      contributions(5).taxPeriodStart.year shouldBe 2013
      contributions(6).taxPeriodStart.year shouldBe 2014
      contributions(7).taxPeriodStart.year shouldBe 2015
      contributions(8).taxPeriodStart.year shouldBe 2015
      contributions(9).taxPeriodStart.year shouldBe 2015

      contributions(7).amounts.get.moneyPurchase.get shouldBe 88800
      contributions(7).amounts.get.triggered.get shouldBe false
      contributions(8).amounts.get.moneyPurchase.get shouldBe 55500
      contributions(8).amounts.get.triggered.get shouldBe true
      contributions(9).amounts.get.moneyPurchase.get shouldBe 44400
      contributions(9).amounts.get.triggered.get shouldBe false
    }

    "return contributions for post trigger period 1 and 2 moneyPurchase amounts when trigger date is the start of period 1" in {
      // set up
      val definedBenefits = Amounts(Some(BigDecimal(123)))
      val definedContributions = Amounts(Some(BigDecimal(456)))
      val year2015 = Year2015Amounts(None, Some(444), None, Some(555), Some(888), None)
      val triggerDate = Some("2015-4-6")
      val formFields = new CalculatorFormFields(definedBenefits,
                                                definedContributions,
                                                year2015,
                                                triggerDate) with Year2016

      // test
      val contributions = formFields.toPageValues

      // check
      contributions.length shouldBe 10
      contributions(0).taxPeriodStart.year shouldBe 2008
      contributions(1).taxPeriodStart.year shouldBe 2009
      contributions(2).taxPeriodStart.year shouldBe 2010
      contributions(3).taxPeriodStart.year shouldBe 2011
      contributions(4).taxPeriodStart.year shouldBe 2012
      contributions(5).taxPeriodStart.year shouldBe 2013
      contributions(6).taxPeriodStart.year shouldBe 2014
      contributions(7).taxPeriodStart.year shouldBe 2015
      contributions(8).taxPeriodStart.year shouldBe 2015
      contributions(9).taxPeriodStart.year shouldBe 2016

      contributions(7).amounts.get.moneyPurchase.get shouldBe 88800
      contributions(7).amounts.get.triggered.get shouldBe true
      contributions(8).amounts.get.moneyPurchase.get shouldBe 55500
      contributions(8).amounts.get.triggered.get shouldBe true
    }

    "return contributions for post trigger period 2 moneyPurchase amounts when trigger date is start of period 2" in {
      // set up
      val definedBenefits = Amounts(Some(BigDecimal(123)))
      val definedContributions = Amounts(Some(BigDecimal(456)))
      val year2015 = Year2015Amounts(None, Some(444), None, Some(555), None, Some(888))
      val triggerDate = Some("2015-7-9")
      val formFields = new CalculatorFormFields(definedBenefits,
                                                definedContributions,
                                                year2015,
                                                triggerDate) with Year2016

      // test
      val contributions = formFields.toPageValues

      // check
      contributions.length shouldBe 11
      contributions(0).taxPeriodStart.year shouldBe 2008
      contributions(1).taxPeriodStart.year shouldBe 2009
      contributions(2).taxPeriodStart.year shouldBe 2010
      contributions(3).taxPeriodStart.year shouldBe 2011
      contributions(4).taxPeriodStart.year shouldBe 2012
      contributions(5).taxPeriodStart.year shouldBe 2013
      contributions(6).taxPeriodStart.year shouldBe 2014
      contributions(7).taxPeriodStart.year shouldBe 2015
      contributions(8).taxPeriodStart.year shouldBe 2015
      //contributions(9).taxPeriodStart.year shouldBe 2016

      contributions(7).amounts.get.moneyPurchase.get shouldBe 88800
      contributions(7).amounts.get.triggered.get shouldBe true
      contributions(8).amounts.get.moneyPurchase.get shouldBe 55500
      contributions(8).amounts.get.triggered.get shouldBe true
    }


    "return contributions for periods 1 and 2 moneyPurchase amounts" in {
      // set up
      val definedBenefits = Amounts(Some(BigDecimal(123)))
      val definedContributions = Amounts(Some(BigDecimal(456)))
      val year2015 = Year2015Amounts(None, Some(444), None, Some(555), None, None)
      val triggerDate = None
      val formFields = new CalculatorFormFields(definedBenefits,
                                                definedContributions,
                                                year2015,
                                                triggerDate) with Year2016

      // test
      val contributions = formFields.toPageValues

      // check
      contributions.length shouldBe 10
      contributions(0).taxPeriodStart.year shouldBe 2008
      contributions(1).taxPeriodStart.year shouldBe 2009
      contributions(2).taxPeriodStart.year shouldBe 2010
      contributions(3).taxPeriodStart.year shouldBe 2011
      contributions(4).taxPeriodStart.year shouldBe 2012
      contributions(5).taxPeriodStart.year shouldBe 2013
      contributions(6).taxPeriodStart.year shouldBe 2014
      contributions(7).taxPeriodStart.year shouldBe 2015
      contributions(8).taxPeriodStart.year shouldBe 2015
      contributions(9).taxPeriodStart.year shouldBe 2016

      contributions(7).amounts.get.moneyPurchase.get shouldBe 55500
      contributions(7).amounts.get.triggered.get shouldBe false
      contributions(8).amounts.get.moneyPurchase.get shouldBe 44400
      contributions(8).amounts.get.triggered.get shouldBe false
    }

    "return contributions for periods 1 and 2 defined benefits amounts" in {
      // set up
      val definedBenefits = Amounts(Some(BigDecimal(123)))
      val definedContributions = Amounts(Some(BigDecimal(456)))
      val year2015 = Year2015Amounts(Some(444), None, Some(555), None, None, None)
      val triggerDate = None
      val formFields = new CalculatorFormFields(definedBenefits,
                                                definedContributions,
                                                year2015,
                                                triggerDate) with Year2016

      // test
      val contributions = formFields.toPageValues

      // check
      contributions.length shouldBe 10
      contributions(0).taxPeriodStart.year shouldBe 2008
      contributions(1).taxPeriodStart.year shouldBe 2009
      contributions(2).taxPeriodStart.year shouldBe 2010
      contributions(3).taxPeriodStart.year shouldBe 2011
      contributions(4).taxPeriodStart.year shouldBe 2012
      contributions(5).taxPeriodStart.year shouldBe 2013
      contributions(6).taxPeriodStart.year shouldBe 2014
      contributions(7).taxPeriodStart.year shouldBe 2015
      contributions(8).taxPeriodStart.year shouldBe 2015
      contributions(9).taxPeriodStart.year shouldBe 2016

      contributions(7).amounts.get.definedBenefit.get shouldBe 55500
      contributions(7).amounts.get.triggered.get shouldBe false
      contributions(8).amounts.get.definedBenefit.get shouldBe 44400
      contributions(8).amounts.get.triggered.get shouldBe false
    }

    "return simple 2015 contributions if trigger date is after period 2" in {
      // set up
      val definedBenefits = Amounts(Some(BigDecimal(123)))
      val definedContributions = Amounts(Some(BigDecimal(456)))
      val year2015 = Year2015Amounts(Some(444), None, Some(555), None, None, None)
      val triggerDate = Some("2020-11-02")
      val formFields = new CalculatorFormFields(definedBenefits,
                                                definedContributions,
                                                year2015,
                                                triggerDate) with Year2016

      // test
      val contributions = formFields.toPageValues

      // check
      contributions.length shouldBe 10
      contributions(0).taxPeriodStart.year shouldBe 2008
      contributions(1).taxPeriodStart.year shouldBe 2009
      contributions(2).taxPeriodStart.year shouldBe 2010
      contributions(3).taxPeriodStart.year shouldBe 2011
      contributions(4).taxPeriodStart.year shouldBe 2012
      contributions(5).taxPeriodStart.year shouldBe 2013
      contributions(6).taxPeriodStart.year shouldBe 2014
      contributions(7).taxPeriodStart.year shouldBe 2015
      contributions(8).taxPeriodStart.year shouldBe 2015
      contributions(9).taxPeriodStart.year shouldBe 2016
    }
  }

  "hasDefinedBenefits" should {
    "return true if any defined benefit values set" in {
      // set up
      val year2015a = Year2015Amounts(Some(BigDecimal(123)))
      val year2015b = Year2015Amounts(None, None, Some(BigDecimal(123)))
      val definedBenefits = Amounts()
      val definedContributions = Amounts()
      val triggerDate = None
      val formFields1 = new CalculatorFormFields(definedBenefits,
                                                definedContributions,
                                                year2015a,
                                                triggerDate) with Year2016
      val formFields2 = new CalculatorFormFields(definedBenefits,
                                                definedContributions,
                                                year2015b,
                                                triggerDate) with Year2016
      val formFields3 = new CalculatorFormFields(Amounts(Some(123)),
                                                definedContributions,
                                                Year2015Amounts(),
                                                triggerDate) with Year2016

      // test
      val result1 = formFields1.hasDefinedBenefits
      val result2 = formFields2.hasDefinedBenefits
      val result3 = formFields3.hasDefinedBenefits

      // check
      result1 shouldBe true
      result2 shouldBe true
      result3 shouldBe true
    }
  }

  "hasTriggerDate" should {
    "return true if any trigger date is set" in {
      // set up
      val triggerDate = Some("2015-1-3")
      val formFields1 = new CalculatorFormFields(Amounts(),
                                                Amounts(),
                                                Year2015Amounts(),
                                                triggerDate) with Year2016

      // test
      val result1 = formFields1.hasTriggerDate

      // check
      result1 shouldBe true
    }

    "return false if any trigger date is set but is empty" in {
      // set up
      val triggerDate = Some("")
      val formFields1 = new CalculatorFormFields(Amounts(),
                                                Amounts(),
                                                Year2015Amounts(),
                                                triggerDate) with Year2016

      // test
      val result1 = formFields1.hasTriggerDate

      // check
      result1 shouldBe false
    }

    "return false if any trigger date is not set" in {
      // set up
      val triggerDate = None
      val formFields1 = new CalculatorFormFields(Amounts(),
                                                Amounts(),
                                                Year2015Amounts(),
                                                triggerDate) with Year2016

      // test
      val result1 = formFields1.hasTriggerDate

      // check
      result1 shouldBe false
    }
  }

  "toP1TriggerDefinedContribution" should {
    "return None if no period 1 trigger amount" in {
      // set up
      val triggerDate = None
      val formFields1 = new CalculatorFormFields(Amounts(),
                                                Amounts(),
                                                Year2015Amounts(None, None, None, None, None, None),
                                                triggerDate) with Year2016

      // test
      val result1 = formFields1.toP1TriggerDefinedContribution

      // check
      result1 shouldBe None
    }

    "return Some value in pence if period 1 trigger amount given" in {
      // set up
      val triggerDate = None
      val formFields1 = new CalculatorFormFields(Amounts(),
                                                Amounts(),
                                                Year2015Amounts(None, None, None, None, Some(BigDecimal(123)), None),
                                                triggerDate) with Year2016

      // test
      val result1 = formFields1.toP1TriggerDefinedContribution

      // check
      result1 should not be None
      result1.get._1 shouldBe 12300
      result1.get._2 shouldBe "triggerDefinedContribution_2015_p1"
    }
  }

  "toP2TriggerDefinedContribution" should {
    "return None if no period 2 trigger amount" in {
      // set up
      val triggerDate = None
      val formFields1 = new CalculatorFormFields(Amounts(),
                                                Amounts(),
                                                Year2015Amounts(None, None, None, None, None, None),
                                                triggerDate) with Year2016

      // test
      val result1 = formFields1.toP2TriggerDefinedContribution

      // check
      result1 shouldBe None
    }

    "return Some value in pence if period 2 trigger amount given" in {
      // set up
      val triggerDate = None
      val formFields1 = new CalculatorFormFields(Amounts(),
                                                Amounts(),
                                                Year2015Amounts(None, None, None, None, None, Some(BigDecimal(123))),
                                                triggerDate) with Year2016

      // test
      val result1 = formFields1.toP2TriggerDefinedContribution

      // check
      result1 should not be None
      result1.get._1 shouldBe 12300
      result1.get._2 shouldBe "triggerDefinedContribution_2015_p2"
    }
  }

  "toDefinedContribution" should {
    "return some tuple" in {
      // set up
      val definedBenefits = Amounts(Some(BigDecimal(123)))
      val definedContributions = Amounts(Some(BigDecimal(456)))
      val year2015 = Year2015Amounts(Some(444), None, Some(555), None, None, None)
      val triggerDate = None
      val formFields = new CalculatorFormFields(definedBenefits,
                                                definedContributions,
                                                year2015,
                                                triggerDate) with Year2016
      def first = { (c:Contribution) => c.amounts.isDefined && c.amounts.get.moneyPurchase.isDefined && c.amounts.get.moneyPurchase.get == 45600 }

      // test
      val result = formFields.toDefinedContribution(first)("Hi")

      // check
      result should not be None
      result.get._1 shouldBe 45600
      result.get._2 shouldBe "Hi"
    }

    "return some tuple for 2016" in {
      // set up
      val definedBenefits = Amounts(Some(BigDecimal(123)))
      val definedContributions = Amounts(Some(BigDecimal(456)))
      val year2015 = Year2015Amounts()
      val triggerDate = None
      val formFields = new CalculatorFormFields(definedBenefits,
                                                definedContributions,
                                                year2015,
                                                triggerDate) with Year2016

      // test
      val result = formFields.toDefinedContribution(2016)

      // check
      result should not be None
      result.get._1 shouldBe 45600
      result.get._2 shouldBe "definedContribution_2016"
    }
  }

  "ThisYear trait" should {
    "have THIS_YEAR set to config year" in {
      class Test extends models.ThisYear {}
      new Test().THIS_YEAR shouldBe config.PaacConfiguration.year()
    }
  }

  "triggerDatePeriod" should {
    "return period 1 contribution" in {
      // set up
      val formFields = new CalculatorFormFields(Amounts(),
                                                Amounts(),
                                                Year2015Amounts(),
                                                Some("2015-4-15")) with Year2016

      // test
      val result = formFields.triggerDatePeriod

      // check
      result should not be None
      result.get.isPeriod1 shouldBe true
    }
  }
}
