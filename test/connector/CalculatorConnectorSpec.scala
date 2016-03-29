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

import config.WSHttp
import models._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class CalculatorConnectorSpec extends UnitSpec with MockitoSugar {
  "Calculator connector" should {
    "convert contributions to json" in {
      // set up
      implicit val hc = uk.gov.hmrc.play.http.HeaderCarrier()
      val mockHttpPost = mock[HttpPost]
      object MockCalculatorConnector extends CalculatorConnector {
        override def httpPostRequest: HttpPost = mockHttpPost
        override def serviceUrl: String = ""
      }
      stub(mockHttpPost.POST[JsValue, HttpResponse]("/paac/calculate", Json.toJson(List(Contribution(2008,5000))), null)).toReturn(HttpResponse(200))

      // test
      MockCalculatorConnector.connectToPAACService(List(Contribution(2008,5000)))

      // check
      verify(mockHttpPost).POST[JsValue, HttpResponse]("/paac/calculate", Json.toJson(List(Contribution(2008,5000))), null)
    }
  }
}
