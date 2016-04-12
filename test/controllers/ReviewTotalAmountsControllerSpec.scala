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

import org.scalatest.BeforeAndAfterAll
import org.scalatest.mock._
import connector._
import models._
import play.api.libs.json._
import play.api.Play
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, FakeApplication}
import uk.gov.hmrc.play.test.UnitSpec
import service._
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import uk.gov.hmrc.play.http.SessionKeys
import java.util.UUID

class ReviewTotalAmountsControllerSpec extends UnitSpec with BeforeAndAfterAll {
  val app = FakeApplication()
  val contribution0 = Contribution(2008, 500000)
  val contribution1 = Contribution(2009, 600000)
  val contribution2 = Contribution(2010, 700000)
  val contribution3 = Contribution(2011, 800000)
  val contribution4 = Contribution(2012, 900000)
  val contribution5 = Contribution(2013, 1000000)
  val SESSION_ID = s"session-${UUID.randomUUID}"

  override def beforeAll() {
    Play.start(app)
    super.beforeAll() // To be stackable, must call super.beforeEach
  }

  override def afterAll() {
    try {
      super.afterAll()
    } finally Play.stop()
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

  trait MockKeystoreFixture {
    object MockKeystore extends KeystoreService {
      var map = Map("definedBenefit_2006" -> "500000",
                    "definedBenefit_2007" -> "600000",
                    "definedBenefit_2008" -> "700000",
                    "definedBenefit_2009" -> "800000",
                    "definedBenefit_2010" -> "900000",
                    "definedBenefit_2011" -> "1000000",
                    "definedBenefit_2012" -> "1100000",
                    "definedBenefit_2013" -> "1200000",
                    "definedBenefit_2014" -> "1300000",
                    SessionKeys.sessionId -> SESSION_ID)
      override def store[T](data: T, key: String)
                  (implicit hc: HeaderCarrier,
                   format: play.api.libs.json.Format[T],
                   request: Request[Any])
                  : Future[Option[T]] = {
        map = map + (key -> data.toString)
        Future.successful(Some(data))
      }
      override def read[T](key: String)
                 (implicit hc: HeaderCarrier,
                  format: play.api.libs.json.Format[T],
                  request: Request[Any])
                 : Future[Option[T]] = {
        Future.successful((map get key).map(_.asInstanceOf[T]))
      }
    }
  }

  trait MockControllerFixture extends MockKeystoreFixture with MockCalculatorConnectorFixture {
    object MockedReviewTotalAmountsController extends ReviewTotalAmountsController {
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
        val result: Future[Map[String,String]] = MockedReviewTotalAmountsController.fetchAmounts()

        // check
        val values: Map[String,String] = Await.result(result, Duration(1000,MILLISECONDS))
        values should contain key ("definedBenefit_2011") 
        values should contain value ("12000.00")
      }

      "should return 0.00 when keystore has 0 as an amount" in new MockControllerFixture {
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
      }

      "should return money purchase when keystore has money purchase amount for 2015" in new MockControllerFixture {
        // set up
        MockKeystore.map = MockKeystore.map + ("moneyPurchase_2015"->"123450")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession((SessionKeys.sessionId,SESSION_ID))

        // test
        val result: Future[Map[String,String]] = MockedReviewTotalAmountsController.fetchAmounts()

        // check
        val values: Map[String,String] = Await.result(result, Duration(1000,MILLISECONDS))
        values should contain key ("moneyPurchase_2015") 
        values should contain value ("1234.50")
      }

      "should return defined benefit when keystore has money purchase amount for 2015" in new MockControllerFixture {
        // set up
        MockKeystore.map = MockKeystore.map + ("definedBenefit_2015_p1"->"9123450")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession((SessionKeys.sessionId,SESSION_ID))

        // test
        val result: Future[Map[String,String]] = MockedReviewTotalAmountsController.fetchAmounts()

        // check
        val values: Map[String,String] = Await.result(result, Duration(1000,MILLISECONDS))
        values should contain key ("definedBenefit_2015_p1")
        values should contain value ("91234.50")
      }

      "should return values when keystore has amounts for 2016" in new MockControllerFixture {
        // set up
        MockKeystore.map = MockKeystore.map ++ Map("definedBenefit_2016"->"100",
                                                   "moneyPurchase_2016"->"200",
                                                   "thresholdIncome_2016"->"300",
                                                   "adjustedIncome_2016"->"400",
                                                   "taperedAllowance_2016"->"500")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession((SessionKeys.sessionId,SESSION_ID))

        // test
        val result: Future[Map[String,String]] = MockedReviewTotalAmountsController.fetchAmounts()

        // check
        val values: Map[String,String] = Await.result(result, Duration(1000,MILLISECONDS))
        values should contain key ("definedBenefit_2016") 
        values should contain value ("1.00")
        values should contain key ("moneyPurchase_2016") 
        values should contain value ("2.00")
        values should contain key ("thresholdIncome_2016") 
        values should contain value ("3.00")
        values should contain key ("adjustedIncome_2016") 
        values should contain value ("4.00")
        values should contain key ("taperedAllowance_2016") 
        values should contain value ("5.00")
      }

    }

    "onSubmit" can {
      "display list of amounts previously provided by user" in new MockControllerFixture {
        // set up
        val endpoint = "/paac/review"

        // test
        val result : Future[Result] = MockedReviewTotalAmountsController.onSubmit()(FakeRequest(GET, endpoint).withSession {(SessionKeys.sessionId,SESSION_ID)})

        // check
        status(result) shouldBe 200
      }

      "should return calculation results from amounts stored in keystore" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = MockedReviewTotalAmountsController.onSubmit()(request)

        // check
        val htmlSummaryPage = contentAsString(await(result))
        htmlSummaryPage should include ("Tax Year Results")
      }

      "should display errors if values in keystore are incorrect" in new MockControllerFixture {
        // set up
        MockKeystore.map = (MockKeystore.map - "definedBenefit_2006") ++ Map("definedBenefit_2006"->"-100")
        val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = MockedReviewTotalAmountsController.onSubmit()(request)

        // check
        val htmlSummaryPage = contentAsString(await(result))
        htmlSummaryPage should include ("2006/07 amount was empty. Please provide an amount between £0.00 and £99999999.99.")
      }
    }

    "onPageLoad" can {
      "display errors if values in keystore are incorrect" in new MockControllerFixture {
        // set up
        MockKeystore.map = (MockKeystore.map - "definedBenefit_2006") ++ Map("definedBenefit_2006"->"-100")
        val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = MockedReviewTotalAmountsController.onPageLoad()(request)

        // check
        val htmlSummaryPage = contentAsString(await(result))
        htmlSummaryPage should include ("2006/07 amount was empty. Please provide an amount between £0.00 and £99999999.99.")
      }

      "display table of values that are present in keystore" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = MockedReviewTotalAmountsController.onPageLoad()(request)

        // check
        val htmlSummaryPage = contentAsString(await(result))
        htmlSummaryPage should include ("£5,000.00")
        htmlSummaryPage should include ("£6,000.00")
        htmlSummaryPage should include ("£7,000.00")
        htmlSummaryPage should include ("£8,000.00")
        htmlSummaryPage should include ("£9,000.00")
        htmlSummaryPage should include ("£10,000.00")
        htmlSummaryPage should include ("£11,000.00")
        htmlSummaryPage should include ("£12,000.00")
        htmlSummaryPage should include ("£13,000.00")
      }
    }

    "onEditAmount" can {
      "redirect to pension inputs controller when year is less than 2015" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = MockedReviewTotalAmountsController.onEditAmount(2014)(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs")
      }

      // TODO: Need to update this scenario with 2015 tax year
      "redirect to on page load when year is 2015" in new MockControllerFixture {
        // set up
        val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = MockedReviewTotalAmountsController.onEditAmount(2015)(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs1516p1")
      }
    }
  }
}
