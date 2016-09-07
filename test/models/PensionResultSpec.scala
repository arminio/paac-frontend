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

import play.api.test.Helpers._
import play.api.test._
import play.api.libs.json._

import org.scalatest._
import org.scalatest.Matchers._

class PensionResultSpec extends ModelSpec {
  "SummaryResult" can {
    "have default value of 0 for all fields" in {
      // do it
      val summaryResult = SummaryResult()

      // check
      summaryResult.chargableAmount shouldBe 0
      summaryResult.exceedingAAAmount shouldBe 0
      summaryResult.availableAllowance shouldBe 0
      summaryResult.unusedAllowance shouldBe 0
    }

    "have chargable amount in pounds" in {
      // setup
      val chargableAmount = 13492

      // do it
      val summaryResult = SummaryResult(chargableAmount=chargableAmount)

      // check
      summaryResult.chargableAmount shouldBe chargableAmount
    }

    "have exceeding Annual Allowance Amount" in {
      // setup
      val exceedingAAAmount = 13492

      // do it
      val summaryResult = SummaryResult(exceedingAAAmount=exceedingAAAmount)

      // check
      summaryResult.exceedingAAAmount shouldBe exceedingAAAmount
    }

    "have available Allowance Amount" in {
      // setup
      val availableAllowanceAmount = 13492

      // do it
      val summaryResult = SummaryResult(availableAllowance=availableAllowanceAmount)

      // check
      summaryResult.availableAllowance shouldBe availableAllowanceAmount
    }

    "have unused Allowance Amount" in {
      // setup
      val unusedAllowanceAmount = 13492

      // do it
      val summaryResult = SummaryResult(unusedAllowance=unusedAllowanceAmount)

      // check
      summaryResult.unusedAllowance shouldBe unusedAllowanceAmount
    }

    "have available Allowance with Carry Forward Amount" in {
      // setup
      val availableAllowanceWithCFAmount = 13492

      // do it
      val summaryResult = SummaryResult(availableAAWithCF=availableAllowanceWithCFAmount)

      // check
      summaryResult.availableAAWithCF shouldBe availableAllowanceWithCFAmount
    }

    "have available Allowance with Cumulative Carry Forward Amount" in {
      // setup
      val availableAllowanceWithCCFAmount = 13492

      // do it
      val summaryResult = SummaryResult(availableAAWithCCF=availableAllowanceWithCCFAmount)

      // check
      summaryResult.availableAAWithCCF shouldBe availableAllowanceWithCCFAmount
    }

    "have available Unused AA" in {
      // setup
      val value = 13492

      // do it
      val summaryResult = SummaryResult(unusedAAA=value)

      // check
      summaryResult.unusedAAA shouldBe value
    }

    "have available Unused MPAA" in {
      // setup
      val value = 13492

      // do it
      val summaryResult = SummaryResult(unusedMPAA=value)

      // check
      summaryResult.unusedMPAA shouldBe value
    }

    "have isACA" in {
      // set up
      val value = true

      // test
      val summaryResult = SummaryResult(isACA=value)

      // check
      summaryResult.isACA shouldBe value
    }

    "marshall to JSON" in {
      // setup
      val chargableAmount : Long = 2468
      val exceedingAAAmount : Long = 13579
      val summaryResult = SummaryResult(chargableAmount,
                                        exceedingAAAmount,
                                        isACA = true,
                                        availableAAAWithCF = 1,
                                        availableAAAWithCCF = 2)

      // do it
      val json = Json.toJson(summaryResult)

      // check
      val jsonChargableAmount = json \ "chargableAmount"
      jsonChargableAmount.as[Long] shouldBe chargableAmount
      val jsonExceedingAAAmount = json \ "exceedingAAAmount"
      jsonExceedingAAAmount.as[Long] shouldBe exceedingAAAmount
      val jsonAvailableAllowance = json \ "availableAllowance"
      jsonAvailableAllowance.as[Long] shouldBe 0
      val jsonUnusedAllowance = json \ "unusedAllowance"
      jsonUnusedAllowance.as[Long] shouldBe 0
      val jsonAvailableAllowanceWithCF = json \ "availableAAWithCF"
      jsonAvailableAllowanceWithCF.as[Long] shouldBe 0
      val jsonAvailableAllowanceWithCCF = json \ "availableAAWithCCF"
      jsonAvailableAllowanceWithCCF.as[Long] shouldBe 0
      val jsonIsACA = json \ "isACA"
      jsonIsACA.as[Boolean] shouldBe true
      val jsonAvailableAAAWithCF = json \ "availableAAAWithCF"
      jsonAvailableAAAWithCF.as[Long] shouldBe 1
      val jsonAvailableAAAWithCCF = json \ "availableAAAWithCCF"
      jsonAvailableAAAWithCCF.as[Long] shouldBe 2
    }

    "unmarshall from JSON" in {
      // setup
      val json = Json.parse("""{"chargableAmount": 12345, "exceedingAAAmount": 67890, "availableAllowance":0, "unusedAllowance": 0, "availableAAWithCF": 0, "availableAAWithCCF":0, "unusedAAA":0, "unusedMPAA": 0, "exceedingMPAA": 0, "exceedingAAA": 0, "isMPA": true, "moneyPurchaseAA": 12, "alternativeAA": 15, "isACA": true, "availableAAAWithCF": 1, "availableAAAWithCCF": 2}""")

      // do it
      val summaryResultOption : Option[Summary] = json.validate[Summary].fold(invalid = { _ => None }, valid = { obj => Some(obj)})

      summaryResultOption shouldBe Some(SummaryResult(12345, 67890, isMPA=true, moneyPurchaseAA=12, alternativeAA=15, isACA=true, availableAAAWithCF=1, availableAAAWithCCF=2))
    }

    "round" must {
      trait TestResult {
        object Test {
          def roundInt(value: Int): Int = SummaryResult(value*100).taxBucket
        }
      }

      trait TestRounding {
        object Test {
          def taxBucket(value: Int): Int = SummaryResult(value*100).taxBucket
          def aaBucket(value: Int): Int = SummaryResult(availableAAWithCF=value*100).aaBucket
          def aaaBucket(value: Int): Int = SummaryResult(availableAAAWithCF=value*100).aaaBucket
          def unusedAABucket(value: Int): Int = SummaryResult(availableAAWithCCF=value*100).unusedAABucket
          def unusedAAABucket(value: Int): Int = SummaryResult(availableAAAWithCCF=value*100).unusedAAABucket
        }
      }

      "round tax" in new TestRounding {
        // set up
        val values = Map(323 -> 300, 126 -> 100, 21 -> 0, 51 -> 100, 89 -> 100, 49 -> 0, 678 -> 700)

        // test
        values.foreach {
          (pair) =>
          val (v, expected) = pair
          withClue(s"Expected ${v} to be ${expected}, ") { Test.taxBucket(v) shouldBe expected }
        }
      }

      "round aa" in new TestRounding {
        // set up
        val values = Map(323 -> 300, 126 -> 100, 21 -> 0, 51 -> 100, 89 -> 100, 49 -> 0, 678 -> 700)

        // test
        values.foreach {
          (pair) =>
          val (v, expected) = pair
          withClue(s"Expected ${v} to be ${expected}, ") { Test.aaBucket(v) shouldBe expected }
        }
      }

      "round aaa" in new TestRounding {
        // set up
        val values = Map(323 -> 300, 126 -> 100, 21 -> 0, 51 -> 100, 89 -> 100, 49 -> 0, 678 -> 700)

        // test
        values.foreach {
          (pair) =>
          val (v, expected) = pair
          withClue(s"Expected ${v} to be ${expected}, ") { Test.aaaBucket(v) shouldBe expected }
        }
      }

      "round unused aa" in new TestRounding {
        // set up
        val values = Map(323 -> 300, 126 -> 100, 21 -> 0, 51 -> 100, 89 -> 100, 49 -> 0, 678 -> 700)

        // test
        values.foreach {
          (pair) =>
          val (v, expected) = pair
          withClue(s"Expected ${v} to be ${expected}, ") { Test.unusedAABucket(v) shouldBe expected }
        }
      }

      "round unused aaa" in new TestRounding {
        // set up
        val values = Map(323 -> 300, 126 -> 100, 21 -> 0, 51 -> 100, 89 -> 100, 49 -> 0, 678 -> 700)

        // test
        values.foreach {
          (pair) =>
          val (v, expected) = pair
          withClue(s"Expected ${v} to be ${expected}, ") { Test.unusedAAABucket(v) shouldBe expected }
        }
      }

      "round to nearest hundred when value < 1000" in new TestResult {
        // set up
        val values = Map(323 -> 300, 126 -> 100, 21 -> 0, 51 -> 100, 89 -> 100, 49 -> 0, 678 -> 700)

        // test
        values.foreach {
          (pair) =>
          val (v, expected) = pair
          withClue(s"Expected ${v} to be ${expected}, ") { Test.roundInt(v) shouldBe expected }
        }
      }

      "round to nearest thousand when value < 10000 and > 1000" in new TestResult {
        // set up
        val values = Map(3534 -> 4000, 1266 -> 1000, 2143 -> 2000, 5064 -> 5000, 8946 -> 9000, 4412 -> 4000, 4678 -> 5000)

        // test
        values.foreach {
          (pair) =>
          val (v, expected) = pair
          withClue(s"Expected ${v} to be ${expected}, ") { Test.roundInt(v) shouldBe expected }
        }
      }

      "round to nearest ten thousand when value < 100000 and > 10000" in new TestResult {
        // set up
        val values = Map(24334 -> 20000, 64126 -> 60000, 12214 -> 10000, 75063 -> 80000, 89462 -> 90000, 4412 -> 4000, 4678 -> 5000)

        // test
        values.foreach {
          (pair) =>
          val (v, expected) = pair
          withClue(s"Expected ${v} to be ${expected}, ") { Test.roundInt(v) shouldBe expected }
        }
      }

      "round to nearest 50 thousand when value < 1000000 and > 100000" in new TestResult {
        // set up
        val values = Map(243343 -> 250000, 641261 -> 650000, 122147 -> 100000, 7506357 -> 7500000, 8946245 -> 8950000, 424412 -> 400000, 465378 -> 450000)

        // test
        values.foreach {
          (pair) =>
          val (v, expected) = pair
          withClue(s"Expected ${v} to be ${expected}, ") { Test.roundInt(v) shouldBe expected }
        }
      }
    }
  }

  "TaxYearResults" can {
    "have tax year and input amounts as a contibution" in {
      // setup
      val contribution = Contribution(PensionPeriod(2011, 0, 1), PensionPeriod(2011, 4, 31), Some(InputAmounts(1, 2)))

      // do it
      val results = TaxYearResults(contribution, SummaryResult())

      // check
      results.input shouldBe contribution
    }

    "have summary results as a SummaryResult" in {
      // setup
      val summary = SummaryResult(12345, 67890)

      // do it
      val results = TaxYearResults(Contribution(PensionPeriod(2011, 0, 1), PensionPeriod(2011, 4, 31), Some(InputAmounts())), summary)

      // check
      results.summaryResult shouldBe summary
    }

    "marshall to JSON" in {
      // setup
      val taxYear:Short = 2013
      val dbAmountInPounds = 39342
      val mpAmountInPounds = 6789234
      val contribution = Contribution(PensionPeriod(taxYear, 0, 1), PensionPeriod(taxYear, 4, 31), Some(InputAmounts(dbAmountInPounds,mpAmountInPounds)))

      val chargableAmount : Long = 2468
      val exceedingAAAmount : Long = 13579
      val summaryResult = SummaryResult(chargableAmount, exceedingAAAmount)

      // do it
      val json = Json.toJson(TaxYearResults(contribution, summaryResult))

      // check
      val jsonTaxYear = json \ "input" \ "taxPeriodStart" \ "year"
      jsonTaxYear.as[Short] shouldBe taxYear
      val jsonDefinedBenfitInPounds = json \ "input" \ "amounts" \ "definedBenefit"
      jsonDefinedBenfitInPounds.as[Long] shouldBe dbAmountInPounds
      val jsonMoneyPurchaseInPounds = json \ "input" \ "amounts" \ "moneyPurchase"
      jsonMoneyPurchaseInPounds.as[Long] shouldBe mpAmountInPounds
      val jsonChargableAmount = json \ "summaryResult" \ "chargableAmount"
      jsonChargableAmount.as[Long] shouldBe chargableAmount
      val jsonExceedingAAAmount = json \ "summaryResult" \ "exceedingAAAmount"
      jsonExceedingAAAmount.as[Long] shouldBe exceedingAAAmount
      val jsonUnusedAAA = json \ "summaryResult" \ "unusedAAA"
      jsonUnusedAAA.as[Long] shouldBe 0L
      val jsonUnusedMPAA = json \ "summaryResult" \ "unusedMPAA"
      jsonUnusedMPAA.as[Long] shouldBe 0L
      val jsonExceedingMPAA = json \ "summaryResult" \ "exceedingMPAA"
      jsonExceedingMPAA.as[Long] shouldBe 0L
      val jsonExceedingAAA = json \ "summaryResult" \ "exceedingAAA"
      jsonExceedingAAA.as[Long] shouldBe 0L
    }

    "unmarshall from JSON" in {
      // setup
      val json = Json.parse("""{"input": {"taxPeriodStart": {"year":2008, "month" : 2, "day" : 11},
                                          "taxPeriodEnd": {"year":2008, "month" : 8, "day" : 12},
                                          "amounts": {"definedBenefit": 12345, "moneyPurchase": 67890}},
                                          "summaryResult": {"chargableAmount": 12345, "exceedingAAAmount": 67890, "availableAllowance":1, "unusedAllowance": 2,
                                          "availableAAWithCF": 3, "availableAAWithCCF":4, "unusedAAA":5, "unusedMPAA": 6,
                                          "exceedingMPAA": 0, "exceedingAAA": 0, "isMPA": true, "moneyPurchaseAA": 12, "alternativeAA": 15, "isACA": true, "availableAAAWithCF": 123, "availableAAAWithCCF": 345}}""")

      // do it
      val taxYearResultsOption : Option[TaxYearResults] = json.validate[TaxYearResults].fold(invalid = { _ => None }, valid = { obj => Some(obj)})

      taxYearResultsOption.get.input.taxPeriodStart.year shouldBe 2008
      taxYearResultsOption.get.summaryResult.chargableAmount shouldBe 12345
      taxYearResultsOption.get.summaryResult.exceedingAAAmount shouldBe 67890
      taxYearResultsOption.get.summaryResult.availableAllowance shouldBe 1
      taxYearResultsOption.get.summaryResult.unusedAllowance shouldBe 2
      taxYearResultsOption.get.summaryResult.availableAAWithCF shouldBe 3
      taxYearResultsOption.get.summaryResult.availableAAWithCCF shouldBe 4
      taxYearResultsOption.get.summaryResult.unusedAAA shouldBe 5
      taxYearResultsOption.get.summaryResult.unusedMPAA shouldBe 6
      taxYearResultsOption.get.summaryResult.isMPA shouldBe true
      taxYearResultsOption.get.summaryResult.moneyPurchaseAA shouldBe 12
      taxYearResultsOption.get.summaryResult.alternativeAA shouldBe 15
      taxYearResultsOption.get.summaryResult.availableAAAWithCF shouldBe 123
      taxYearResultsOption.get.summaryResult.availableAAAWithCCF shouldBe 345
    }
  }

  "toTuple" should {
    "convert summary to tuple" in {
      // set up
      val summary = SummaryResult(1,2,3,4,5,6,7,8,9,10,true,12,13,true,15,16)

      // test
      val tuple = Summary.toTuple(summary)

      // check
      tuple._1 shouldBe 1
      tuple._2 shouldBe 2
      tuple._3 shouldBe 3
      tuple._4 shouldBe 4
      tuple._5 shouldBe 5
      tuple._6 shouldBe 6
      tuple._7 shouldBe 7
      tuple._8 shouldBe 8
      tuple._9 shouldBe 9
      tuple._10 shouldBe 10
      tuple._11 shouldBe true
      tuple._12 shouldBe 12
      tuple._13 shouldBe 13
      tuple._14 shouldBe true
      tuple._15 shouldBe 15
      tuple._16 shouldBe 16
    }
  }

  "toSummary" should {
    "convert create summary instance" in {
      // set up
      val chargableAmount = 1
      val exceedingAAAmount = 2
      val availableAllowance = 3
      val unusedAllowance = 4
      val availableAAWithCF = 5
      val availableAAWithCCF = 6
      val unusedAAA = 7
      val unusedMPAA = 8
      val exceedingMPAA = 9
      val exceedingAAA = 10
      val isMPA = true
      val moneyPurchaseAA = 12
      val alternativeAA = 13
      val isACA = true
      val availableAAAWithCF = 15
      val availableAAAWithCCF = 16

      // test
      val summaryResult = Summary.toSummary(chargableAmount,exceedingAAAmount,availableAllowance,unusedAllowance,availableAAWithCF,availableAAWithCCF,unusedAAA,unusedMPAA,exceedingMPAA,exceedingAAA,isMPA,moneyPurchaseAA,alternativeAA,isACA,availableAAAWithCF,availableAAAWithCCF)

      // check
      summaryResult.chargableAmount shouldBe 1
      summaryResult.exceedingAAAmount shouldBe 2
      summaryResult.availableAllowance shouldBe 3
      summaryResult.unusedAllowance shouldBe 4
      summaryResult.availableAAWithCF shouldBe 5
      summaryResult.availableAAWithCCF shouldBe 6
      summaryResult.unusedAAA shouldBe 7
      summaryResult.unusedMPAA shouldBe 8
      summaryResult.exceedingMPAA shouldBe 9
      summaryResult.exceedingAAA shouldBe 10
      summaryResult.isMPA shouldBe true
      summaryResult.moneyPurchaseAA shouldBe 12
      summaryResult.alternativeAA shouldBe 13
      summaryResult.isACA shouldBe true
      summaryResult.availableAAAWithCF shouldBe 15
      summaryResult.availableAAAWithCCF shouldBe 16
    }
  }
}