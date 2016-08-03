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
import service.KeystoreService

import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}
import uk.gov.hmrc.play.http.SessionKeys
import scala.concurrent.Future

class CheckSessionActionSpec extends test.BaseSpec {
  "CheckSessionAction" should {
    "withSession should redirect if no session" in {
      // set up
      val request = FakeRequest()
      val action = new CheckSessionAction(){}

      // test
      val result = await(action.filter(request))

      // check
      status(result.get) shouldBe 303
    }

    "filter" should {
      "redirect if no session id" in {
        // set up
        val request = FakeRequest().withSession()
        val action = new CheckSessionAction(){}

        // test
        val result = await(action.filter(request))

        // check
        status(result.get) shouldBe 303
      }

      "redirect if session id not set" in {
        // set up
        val request = FakeRequest().withSession((SessionKeys.sessionId, "NOSESSION"))
        val action = new CheckSessionAction(){}

        // test
        val result = await(action.filter(request))

        // check
        status(result.get) shouldBe 303
      }

      "return None if session id is present" in {
        // set up
        val request = FakeRequest().withSession((SessionKeys.sessionId, "session-id"))
        val action = new CheckSessionAction(){}

        // test
        val result = await(action.filter(request))

        // check
        result shouldBe None
      }
    }
  }
}
