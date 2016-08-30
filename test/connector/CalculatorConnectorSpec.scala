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

package connector

import org.scalatest.BeforeAndAfterAll
import org.scalatest.mock._
import connector._
import models._
import play.api.libs.json._
import play.api.Play
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.test.{FakeRequest, FakeApplication}

import config.WSHttp
import models._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class CalculatorConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterAll {
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

  "Calculator connector" should {
    "convert contributions to json" in {
      // set up
      implicit val hc = uk.gov.hmrc.play.http.HeaderCarrier()
      val mockHttpPost = mock[HttpPost]
      object MockCalculatorConnector extends CalculatorConnector with test.NullMetrics {
        override def httpPostRequest: HttpPost = mockHttpPost
        override def serviceUrl: String = ""
      }
      val body = Json.toJson(CalculationRequest(List(Contribution(2008,5000)),Some(2008),Some(false)))
      stub(mockHttpPost.POST[JsValue, HttpResponse]("/paac/calculate", body, null)).toReturn(Future.successful(HttpResponse(200)))

      // test
      MockCalculatorConnector.connectToPAACService(List(Contribution(2008,5000)))

      // check
      verify(mockHttpPost).POST[JsValue, HttpResponse]("/paac/calculate", body, null)
    }
    "convert results to TaxYearResults" in {
      // set up
      implicit val hc = uk.gov.hmrc.play.http.HeaderCarrier()
      val mockHttpPost = mock[HttpPost]
      object MockCalculatorConnector extends CalculatorConnector with test.NullMetrics {
        override def httpPostRequest: HttpPost = mockHttpPost
        override def serviceUrl: String = ""
      }
      val body = Json.toJson(CalculationRequest(List(Contribution(2008,5000)),Some(2008),Some(false)))
      val resultsBody = Json.obj("status" -> JsNumber(OK),
                                 "message" -> JsString("Valid pension calculation request received."),
                                 "results" -> Json.toJson(List(
                                              TaxYearResults(Contribution(2008,5000),SummaryResult(availableAAWithCCF=5000)),
                                              TaxYearResults(Contribution(2009,15000),SummaryResult(availableAAWithCCF=15000))
                                                               )))
      stub(mockHttpPost.POST[JsValue, HttpResponse]("/paac/calculate", body, null)).toReturn(Future.successful(HttpResponse(200,responseJson=Some(resultsBody))))

      // test
      val results = await(MockCalculatorConnector.connectToPAACService(List(Contribution(2008,5000))))

      // check
      results.size shouldBe 2
      results.map(_.summaryResult.availableAAWithCCF) shouldBe List(5000,15000)
    }

    "httpPostRequest hooks is default" in {
      CalculatorConnector.httpPostRequest.hooks.size shouldBe 1
    }
  }
}
