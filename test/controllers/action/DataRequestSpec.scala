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

package controllers.action

import java.util.UUID

import org.scalatest.BeforeAndAfterAll
import play.api.test.Helpers._
import play.api.test.{FakeRequest}
import play.api.Play
import play.api.test._
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.Future

class DataRequestSpec extends test.BaseSpec {
  "form" should {
    "return form data" in {
      // set up
      val request = FakeRequest("GET", "/abc").withFormUrlEncodedBody("adjustedIncome.amount_2016" -> "20000",
                                                                      "year" -> "2016",
                                                                      "isEdit" -> "false")
      // test
      val result = new DataRequest(Map[String,String](), request).form

      // check
      result("year") shouldBe "2016"
      result("isEdit") shouldBe "false"
      result("adjustedIncome.amount_2016") shouldBe "20000"
    }
  }
}
