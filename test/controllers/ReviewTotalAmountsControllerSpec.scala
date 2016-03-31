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
import scala.concurrent.Future
import service._
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.{Future}
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
      val map = Map("definedBenefit_2014" -> "500000",
                    SessionKeys.sessionId -> SESSION_ID)
      override def store[T](data: T, key: String)
                  (implicit hc: HeaderCarrier, 
                   format: play.api.libs.json.Format[T], 
                   request: Request[Any])
                  : Future[Option[T]] = {
        map + (key -> data.toString)
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

  "ReviewTotalAmountsController" should {
    /*"display list of amounts" in new MockKeystoreFixture {
      // set up
      val endpoint = "/paac/review"

      // test
      val result : Option[Future[Result]] = route(FakeRequest(GET, endpoint).withSession {(SessionKeys.sessionId,SESSION_ID)})

      // check
      result.isDefined shouldBe true
      status(result.get) shouldBe 200
    }

    "display list of amounts previously provided by user" in {
      // set up
      val endpoint = "/paac/review"

      // test
      val result : Option[Future[Result]] = route(FakeRequest(GET, endpoint).withSession {(SessionKeys.sessionId,SESSION_ID)})

      // check
      result.isDefined shouldBe true
      status(result.get) shouldBe 200
    }*/

    "should return calculation results from amounts stored in keystore" in new MockCalculatorConnectorFixture with MockKeystoreFixture {
      // set up
      object MockedReviewTotalAmountsController extends ReviewTotalAmountsController { 
        override val connector: CalculatorConnector = MockCalculatorConnector 
        override val keystore: KeystoreService = MockKeystore
      }
      val request = FakeRequest(GET, "/paac/calculate").withSession {(SessionKeys.sessionId,SESSION_ID)}

      // test
      val result: Future[Result] = MockedReviewTotalAmountsController.onSubmit()(request)

      // check
      val htmlSummaryPage = contentAsString(await(result))
      htmlSummaryPage should include ("Tax Year Results")
    }
  }
}
