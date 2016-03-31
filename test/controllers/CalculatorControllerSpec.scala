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


class CalculatorControllerSpec extends UnitSpec with BeforeAndAfterAll {
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

  implicit val request = FakeRequest()

  "CalculatorController" should {
    "not return result NOT_FOUND" in {
      val result : Option[Future[Result]] = route(FakeRequest(GET, "/paac/calculate"))
      result.isDefined shouldBe true
      status(result.get) should not be NOT_FOUND
    }
    "return 200 for valid GET request" in {
      val result : Option[Future[Result]] = route(FakeRequest(GET, "/paac/calculate"))
      status(result.get) shouldBe 200
    }
    /*"return error if no JSON supplied for POST request" in {
      val result : Option[Future[Result]] = route(FakeRequest(POST, "/paac/calculate"))
      status(result.get) shouldBe 200
    }*/
    "should return calculation results" in {
      // set up
      val contribution0 = Contribution(2008, 500000)
      val contribution1 = Contribution(2009, 600000)
      val contribution2 = Contribution(2010, 700000)
      val contribution3 = Contribution(2011, 800000)
      val contribution4 = Contribution(2012, 900000)
      val contribution5 = Contribution(2013, 1000000)
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
        override def connectToPAACService(contributions:List[Contribution])(implicit hc: uk.gov.hmrc.play.http.HeaderCarrier): Future[List[TaxYearResults]] = Future.successful(expectedResults)
      }
      object MockedCalculatorController extends CalculatorController { 
        override val connector: CalculatorConnector = MockCalculatorConnector 
      }
      val request = FakeRequest(GET, "/paac/calculate").withFormUrlEncodedBody(("definedBenefit_2008","500000"),
                                                                               ("definedBenefit_2009","600000"),
                                                                               ("definedBenefit_2010","700000"),
                                                                               ("definedBenefit_2011","800000"),
                                                                               ("definedBenefit_2012","900000"),
                                                                               ("definedBenefit_2013","1000000"),
                                                                               ("definedBenefit_2014","0.0"))

      // test
      val result: Future[Result] = MockedCalculatorController.onSubmit()(request)

      // check
      val htmlSummaryPage = contentAsString(await(result))
      htmlSummaryPage should include ("Tax Year Results")
    }
  }
}
