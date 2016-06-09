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

import java.util.UUID

import connector._
import models._
import org.scalatest.BeforeAndAfterAll
import play.api.Play
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}
import service._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ReviewTotalAmountsControllerSpec extends test.BaseSpec {
  val contribution0 = Contribution(2008, 500000)
  val contribution1 = Contribution(2009, 600000)
  val contribution2 = Contribution(2010, 700000)
  val contribution3 = Contribution(2011, 800000)
  val contribution4 = Contribution(2012, 900000)
  val contribution5 = Contribution(2013, 1000000)

  trait Year2016 extends models.ThisYear {
    override def THIS_YEAR = 2016
  }

  trait MockCalculatorConnectorFixture {
    object MockCalculatorConnector extends CalculatorConnector {
      val tyr0 = TaxYearResults(contribution0, SummaryResult(-1,0,5000000,4500000,5000000,4500000,4500000))
      val tyr1 = TaxYearResults(contribution1, SummaryResult(-1,0,5000000,4400000,9500000,8900000,8900000))
      val tyr2 = TaxYearResults(contribution2, SummaryResult(-1,0,5000000,4300000,13900000,13200000,13200000))
      val tyr3 = TaxYearResults(contribution3, SummaryResult(0,0,5000000,4200000,18200000,12900000,17400000))
      val tyr4 = TaxYearResults(contribution4, SummaryResult(0,0,5000000,4100000,17900000,12600000,17000000))
      val tyr5 = TaxYearResults(contribution5, SummaryResult(0,0,5000000,4000000,17600000,12300000,16600000))
      val expectedResults: List[TaxYearResults] = List[TaxYearResults](tyr0, tyr1, tyr2, tyr3, tyr4, tyr5)
      def httpPostRequest: uk.gov.hmrc.play.http.HttpPost = null
      def serviceUrl: String = ""
      override def connectToPAACService(contributions:List[Contribution])
                                       (implicit hc: uk.gov.hmrc.play.http.HeaderCarrier)
                                       : Future[List[TaxYearResults]] = Future.successful(expectedResults)
    }
  }

  trait AMockKeystoreFixture extends MockKeystoreFixture {
    MockKeystore.map = Map("definedBenefit_2008" -> "700000",
                    "definedBenefit_2009" -> "800000",
                    "definedBenefit_2010" -> "900000",
                    "definedBenefit_2011" -> "1000000",
                    "definedBenefit_2012" -> "1100000",
                    "definedBenefit_2013" -> "1200000",
                    "definedBenefit_2014" -> "1300000",
                    "definedBenefit_2016" -> "1300000",
                    "definedBenefit" -> "true",
                    "definedContribution" -> "true",
                    SessionKeys.sessionId -> SESSION_ID)
  }

  trait MockControllerFixture extends AMockKeystoreFixture with MockCalculatorConnectorFixture {
    object ControllerWithMocks extends ReviewTotalAmountsController {
      override val connector: CalculatorConnector = MockCalculatorConnector
      override val keystore: KeystoreService = MockKeystore
    }
  }

  trait Mock2016ControllerFixture extends AMockKeystoreFixture with MockCalculatorConnectorFixture {
    object ControllerWithMocks extends ReviewTotalAmountsController with Year2016 {
      override val connector: CalculatorConnector = MockCalculatorConnector
      override val keystore: KeystoreService = MockKeystore
    }
  }

  "ReviewTotalAmountsController" should {
    "companion object should have keystore" in {
      ReviewTotalAmountsController.keystore shouldBe KeystoreService
    }

    "companion object should have connector" in {
      ReviewTotalAmountsController.connector shouldBe CalculatorConnector
    }

    "fetch amounts" can {
      "should return values for all years in keystore" in new MockControllerFixture {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession((SessionKeys.sessionId,SESSION_ID))

        // test
        val result: Future[Map[String,String]] = ControllerWithMocks.fetchAmounts()

        // check
        val values: Map[String,String] = Await.result(result, Duration(1000,MILLISECONDS))
        values should contain key ("definedBenefit_2011")
        values should contain value ("12000")
      }

      "should return values for 2016 values in keystore" in new Mock2016ControllerFixture {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession((SessionKeys.sessionId,SESSION_ID))

        // test
        val result: Future[Map[String,String]] = ControllerWithMocks.fetchAmounts()

        // check
        val values: Map[String,String] = Await.result(result, Duration(1000,MILLISECONDS))
        values should contain key ("definedBenefit_2016")
        values("definedBenefit_2016") shouldBe "13000"
      }

      /*"should return 0.00 when keystore has 0 as an amount" in new MockControllerFixture {
        // set up
        MockKeystore.map = (MockKeystore.map - "definedBenefit_2006") + ("definedBenefit_2006"->"0")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession((SessionKeys.sessionId,SESSION_ID))

        // test
        val result: Future[Map[String,String]] = MockedReviewTotalAmountsController.fetchAmounts()

        // check
        val values: Map[String,String] = Await.result(result, Duration(1000,MILLISECONDS))
        values should contain key ("definedBenefit_2006") 
        values should contain value ("0.00")
      }*/

      "should return money purchase when keystore has money purchase amount for 2015" in new MockControllerFixture {
        // set up
        MockKeystore.map = MockKeystore.map + ("definedContribution_2015_p1"->"123400")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession((SessionKeys.sessionId,SESSION_ID))

        // test
        val result: Future[Map[String,String]] = ControllerWithMocks.fetchAmounts()

        // check
        val values: Map[String,String] = Await.result(result, Duration(1000,MILLISECONDS))
        values should contain key ("definedContribution_2015_p1")
        values should contain value ("1234")
      }

      "should return defined benefit when keystore has money purchase amount for 2015" in new MockControllerFixture {
        // set up
        MockKeystore.map = MockKeystore.map + ("definedBenefit_2015_p1"->"9123400")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession((SessionKeys.sessionId,SESSION_ID))

        // test
        val result: Future[Map[String,String]] = ControllerWithMocks.fetchAmounts()

        // check
        val values: Map[String,String] = Await.result(result, Duration(1000,MILLISECONDS))
        values should contain key ("definedBenefit_2015_p1")
        values should contain value ("91234")
      }
    }

    "onSubmit" can {
      "display list of amounts previously provided by user" in new MockControllerFixture {
        // set up
        val endpoint = "/paac/review"

        // test
        val result : Future[Result] = ControllerWithMocks.onSubmit()(FakeRequest(GET, endpoint).withSession {(SessionKeys.sessionId,SESSION_ID)})

        // check
        status(result) shouldBe 200
      }

      "return calculation results from amounts stored in keystore" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = ControllerWithMocks.onSubmit()(request)

        // check
        val htmlSummaryPage = contentAsString(await(result))
        htmlSummaryPage should include ("Tax Year Results")
      }

      "display errors if errors in keystore" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map - "definedBenefit_2009"
        MockKeystore.map = MockKeystore.map + ("definedBenefit_2009" -> "-800000")

        // test
        val result: Future[Result] = ControllerWithMocks.onSubmit()(request)

        // check
        val htmlSummaryPage = contentAsString(await(result))
        htmlSummaryPage should include ("definedBenefits.amount_2009 amount was too small and must be either £0 or greater.")
      }
    }

    "onPageLoad" can {

      "reset isEdit and CurrentYear in keystore" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = ControllerWithMocks.onPageLoad()(request)

        // check
        await(result)
        MockKeystore.map(KeystoreService.IS_EDIT_KEY) shouldBe "false"
        MockKeystore.map(KeystoreService.CURRENT_INPUT_YEAR_KEY) shouldBe "-1"
      }

      "display table of values that are present in keystore" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = ControllerWithMocks.onPageLoad()(request)

        // check
        val htmlSummaryPage = contentAsString(await(result))
        htmlSummaryPage should include ("&pound;7,000")
        htmlSummaryPage should include ("&pound;8,000")
        htmlSummaryPage should include ("&pound;9,000")
        htmlSummaryPage should include ("&pound;10,000")
        htmlSummaryPage should include ("&pound;11,000")
        htmlSummaryPage should include ("&pound;12,000")
        htmlSummaryPage should include ("&pound;13,000")
      }

      "display p1 trigger amount columns if in keystore" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (KeystoreService.P1_TRIGGER_DC_KEY -> "123400")
        MockKeystore.map = MockKeystore.map + (KeystoreService.P1_DB_KEY -> "9000")
        MockKeystore.map = MockKeystore.map + (KeystoreService.P1_DC_KEY -> "10000")
        MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-5-10")

        // test
        val result: Future[Result] = ControllerWithMocks.onPageLoad()(request)

        // check
        val htmlSummaryPage = contentAsString(await(result))
        htmlSummaryPage should include ("&pound;1,234")
        htmlSummaryPage should include ("10-5-2015")
      }

      "display p2 trigger amount columns if in keystore" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (KeystoreService.P2_TRIGGER_DC_KEY -> "123400")
        MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-11-5")
        MockKeystore.map = MockKeystore.map + (KeystoreService.P2_DB_KEY -> "9000")
        MockKeystore.map = MockKeystore.map + (KeystoreService.P2_DC_KEY -> "10000")

        // test
        val result: Future[Result] = ControllerWithMocks.onPageLoad()(request)

        // check
        val htmlSummaryPage = contentAsString(await(result))
        htmlSummaryPage should include ("&pound;1,234")
        htmlSummaryPage should include ("5-11-2015")
      }

      "display errors if errors in keystore" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map - "definedBenefit_2009"
        MockKeystore.map = MockKeystore.map + ("definedBenefit_2009" -> "-800000")

        // test
        val result: Future[Result] = ControllerWithMocks.onPageLoad()(request)

        // check
        val htmlSummaryPage = contentAsString(await(result))
        htmlSummaryPage should include ("definedBenefits.amount_2009 amount was too small and must be either £0 or greater.")
      }
    }

    "onEditAmount" can {
      "redirect to pension inputs controller when year is less than 2015" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/edit").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = ControllerWithMocks.onEditAmount(2014)(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs")
      }

      "redirect to pension inputs 1516 period 1 controller on page load when year is 2015 p1" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/edit").withSession {(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")

        // test
        val result: Future[Result] = ControllerWithMocks.onEditAmount(20151)(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs1516p1")
      }

      "redirect to pension inputs 1516 period 2 controller on page load when year is 2015 p2" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/edit").withSession {(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")

        // test
        val result: Future[Result] = ControllerWithMocks.onEditAmount(20152)(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs1516p2")
      }
    }

    "onBack" should {
      "redirect to pension input" in new MockControllerFixture {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(GET,"/paac/backreview").withSession{(SessionKeys.sessionId,SESSION_ID)}
  
          // test
          val result : Future[Result] = ControllerWithMocks.onBack()(request)
  
          // check
          status(result) shouldBe 303
          redirectLocation(result) shouldBe Some("/paac/pensionInputs")
      }
    }
  }
}
