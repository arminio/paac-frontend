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
import service.KeystoreService._

class ContributionsSpec extends test.BaseSpec {
  "Contributions" must {
    "return this year's and 8 previous years contributions converting big decimal to pence" in {
      // set up
      val data = Map(s"${DB_PREFIX}2016"->"12300",s"${DC_PREFIX}2016"->"45600",s"${AI_PREFIX}2016"->"78900")

      // test
      val contributions = Contributions(data)

      // check
      contributions.length shouldBe 1
      contributions(0).definedBenefit shouldBe 12300
      contributions(0).moneyPurchase shouldBe 45600
      contributions(0).income shouldBe 78900
      contributions(0).isTriggered shouldBe false
    }

    "return contributions for post trigger period 1 and 2 moneyPurchase amounts" in {
      // set up
      val data = Map(s"${DB_PREFIX}2016"->"12300",
                     s"${DC_PREFIX}2016"->"45600",
                     s"${AI_PREFIX}2016"->"78900",
                     TRIGGER_DATE_KEY->"2015-4-12",
                     P1_DC_KEY->"44400",
                     P2_DC_KEY->"55500",
                     P1_TRIGGER_DC_KEY->"88800"
                     )

      // test
      val contributions = Contributions(data)

      // check
      contributions.length shouldBe 4
      contributions(0).moneyPurchase shouldBe -44400
      contributions(0).isTriggered shouldBe false
      contributions(1).moneyPurchase shouldBe 88800
      contributions(1).isTriggered shouldBe true
      contributions(2).moneyPurchase shouldBe 55500
      contributions(2).isTriggered shouldBe true
      contributions(3).definedBenefit shouldBe 12300
      contributions(3).moneyPurchase shouldBe 45600
      contributions(3).income shouldBe 78900
      contributions(3).isTriggered shouldBe true
    }

    "return contributions for post trigger period 2 moneyPurchase amounts" in {
      // set up
      val data = Map(s"${DB_PREFIX}2016"->"12300",
                     s"${DC_PREFIX}2016"->"45600",
                     s"${AI_PREFIX}2016"->"78900",
                     TRIGGER_DATE_KEY->"2015-8-12",
                     P1_DC_KEY->"44400",
                     P2_DC_KEY->"55500",
                     P2_TRIGGER_DC_KEY->"88800"
                     )

      // test
      val contributions = Contributions(data)

      // check
      contributions.length shouldBe 4
      contributions(0).moneyPurchase shouldBe 44400
      contributions(0).isTriggered shouldBe false
      contributions(1).moneyPurchase shouldBe -33300
      contributions(1).isTriggered shouldBe false
      contributions(2).moneyPurchase shouldBe 88800
      contributions(2).isTriggered shouldBe true
      contributions(3).definedBenefit shouldBe 12300
      contributions(3).moneyPurchase shouldBe 45600
      contributions(3).income shouldBe 78900
      contributions(3).isTriggered shouldBe true
    }

    "return simple 2015 contributions if trigger date is after period 2" in {
      // set up
      val data = Map(s"${DB_PREFIX}2016"->"12300",
                     s"${DC_PREFIX}2016"->"45600",
                     s"${AI_PREFIX}2016"->"78900",
                     TRIGGER_DATE_KEY->"2020-11-02",
                     P1_DB_KEY->"44400",
                     P2_DB_KEY->"55500",
                     TRIGGER_DC_KEY->"88800"
                     )

      // test
      val contributions = Contributions(data)

      // check
      contributions.length shouldBe 3
      contributions(0).definedBenefit shouldBe 44400
      contributions(0).isTriggered shouldBe false
      contributions(1).definedBenefit shouldBe 55500
      contributions(1).isTriggered shouldBe false
      contributions(2).definedBenefit shouldBe 12300
      contributions(2).moneyPurchase shouldBe 45600
      contributions(2).income shouldBe 78900
      contributions(2).isTriggered shouldBe false
    }

    "return contributions when trigger date is 2017" in {
      // set up
      val data = Map(s"${DB_PREFIX}2016"->"12300",
                     s"${DC_PREFIX}2016"->"45600",
                     s"${AI_PREFIX}2016"->"78900",
                     s"${DB_PREFIX}2017"->"11100",
                     s"${DC_PREFIX}2017"->"22200",
                     s"${AI_PREFIX}2017"->"33300",
                     TRIGGER_DATE_KEY->"2017-7-9",
                     P1_DB_KEY->"44400",
                     P2_DB_KEY->"55500",
                     TRIGGER_DC_KEY->"88800"
                     )

      // test
      val contributions = Contributions(data)

      // check

      contributions.length shouldBe 5
      contributions(0).definedBenefit shouldBe 44400
      contributions(0).isTriggered shouldBe false
      contributions(1).definedBenefit shouldBe 55500
      contributions(1).isTriggered shouldBe false
      contributions(2).definedBenefit shouldBe 12300
      contributions(2).moneyPurchase shouldBe 45600
      contributions(2).income shouldBe 78900
      contributions(2).isTriggered shouldBe false
      contributions(3).definedBenefit shouldBe 11100
      contributions(3).moneyPurchase shouldBe -66600
      contributions(3).income shouldBe 33300
      contributions(3).isTriggered shouldBe false
      contributions(4).definedBenefit shouldBe 0
      contributions(4).moneyPurchase shouldBe 88800
      contributions(4).income shouldBe 0
      contributions(4).isTriggered shouldBe true
    }

    "return contributions when trigger date is 2017 but trigger amount not defined" in {
      // set up
      val data = Map(s"${DB_PREFIX}2016"->"12300",
                     s"${DC_PREFIX}2016"->"45600",
                     s"${AI_PREFIX}2016"->"78900",
                     s"${DB_PREFIX}2017"->"11100",
                     s"${DC_PREFIX}2017"->"22200",
                     s"${AI_PREFIX}2017"->"33300",
                     TRIGGER_DATE_KEY->"2017-7-9",
                     P1_DB_KEY->"44400",
                     P2_DB_KEY->"55500"
                     )

      // test
      val contributions = Contributions(data)

      // check

      contributions.length shouldBe 4
      contributions(0).definedBenefit shouldBe 44400
      contributions(0).isTriggered shouldBe false
      contributions(1).definedBenefit shouldBe 55500
      contributions(1).isTriggered shouldBe false
      contributions(2).definedBenefit shouldBe 12300
      contributions(2).moneyPurchase shouldBe 45600
      contributions(2).income shouldBe 78900
      contributions(2).isTriggered shouldBe false
      contributions(3).definedBenefit shouldBe 11100
      contributions(3).moneyPurchase shouldBe 22200
      contributions(3).income shouldBe 33300
      contributions(3).isTriggered shouldBe false
    }

    "be found in list of page values" in {
      // set up
      val data = Map(s"${DB_PREFIX}2016"->"12300",
                     s"${DC_PREFIX}2016"->"45600",
                     s"${AI_PREFIX}2016"->"78900",
                     s"${DB_PREFIX}2017"->"11100",
                     s"${DC_PREFIX}2017"->"22200",
                     s"${AI_PREFIX}2017"->"33300",
                     TRIGGER_DATE_KEY->"2015-8-9",
                     P1_DB_KEY->"44400",
                     P2_DB_KEY->"55500",
                     P1_DC_KEY->"66600",
                     P2_DC_KEY->"77700",
                     P2_TRIGGER_DC_KEY->"88800"
                     )
      val contributions = Contributions(data)
      val triggered = contributions.find(_.isTriggered).get

      // test
      val result = contributions.find((c)=>c.isPeriod2 && c != triggered)

      // check
      triggered shouldBe Contribution(PensionPeriod(2015,8,9),PensionPeriod(2016,4,5),Some(InputAmounts(Some(0),Some(88800),None,Some(true))))
      result shouldBe Some(Contribution(PensionPeriod(2015,7,9),PensionPeriod(2015,8,9),Some(InputAmounts(Some(55500),Some(-11100),None,Some(false)))))
    }
  }
}
