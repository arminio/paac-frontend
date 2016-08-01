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
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future

class BaseFrontendControllerSpec extends test.BaseSpec {

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object Controller extends BaseFrontendController {
      def keystore: KeystoreService = MockKeystore
    }
  }

  "BaseFrontendController" should {
    "withSession" must {
      "redirect if no session" in new ControllerWithMockKeystore {
        // set up
        val request = FakeRequest().withSession()

        // test
        val noSessionAction = Controller.withSession { implicit request =>
          Future.successful(Ok("Done"))
        }
        val result = await(noSessionAction(request))

        // check
        status(result) shouldBe 303
      }

      "not redirect if session id is present" in new ControllerWithMockKeystore {
        // set up
        val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val noSessionAction = Controller.withSession { implicit request =>
          Future.successful(Ok("Done"))
        }
        val result = await(noSessionAction(request))

        // check
        status(result) shouldBe 200
      }
    }

    "withReadSession" must {
      "redirect if no session" in new ControllerWithMockKeystore {
        // set up
        val request = FakeRequest().withSession()

        // test
        val noSessionAction = Controller.withReadSession { implicit request =>
          Future.successful(Ok("Done"))
        }
        val result = await(noSessionAction(request))

        // check
        status(result) shouldBe 303
      }

      "not redirect if session id is present" in new ControllerWithMockKeystore {
        // set up
        val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val noSessionAction = Controller.withReadSession { implicit request =>
          Future.successful(Ok("Done"))
        }
        val result = await(noSessionAction(request))

        // check
        status(result) shouldBe 200
      }

      "read data from keystore and decorate request" in new ControllerWithMockKeystore {
        // set up
        MockKeystore.map = MockKeystore.map + ("msg"->"Hello world!")
        val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val noSessionAction = Controller.withReadSession { implicit request =>
          Future.successful(Ok(request.data("msg")))
        }
        val result = await(noSessionAction(request))

        // check
        status(result) shouldBe 200
        contentAsString(result) shouldBe "Hello world!"
      }
    }

    "withWriteSession" must {
      "redirect if no session" in new ControllerWithMockKeystore {
        // set up
        val request = FakeRequest().withSession()

        // test
        val noSessionAction = Controller.withReadSession { implicit request =>
          Future.successful(Ok("Done"))
        }
        val result = await(noSessionAction(request))

        // check
        status(result) shouldBe 303
      }

      "not redirect if session id is present" in new ControllerWithMockKeystore {
        // set up
        val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val noSessionAction = Controller.withReadSession { implicit request =>
          Future.successful(Ok("Done"))
        }
        val result = await(noSessionAction(request))

        // check
        status(result) shouldBe 200
      }


      "save data to keystore from session" in new ControllerWithMockKeystore {
        // set up
        val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val noSessionAction = Controller.withWriteSession { implicit request =>
          Future.successful(Ok("hi").addingToSession("msg"->"A value to be persisted"))
        }
        val result = await(noSessionAction(request))

        // check
        status(result) shouldBe 200
        MockKeystore.map("msg") shouldBe "A value to be persisted"
      }
    }
  }
}
