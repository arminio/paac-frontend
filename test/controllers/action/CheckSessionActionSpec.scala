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
import scala.concurrent.Future

class CheckSessionActionSpec extends test.BaseSpec {

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object RedirectController extends BaseFrontendController {
      val keystorekey = "TaxYearSelection"
      val keystore: KeystoreService = MockKeystore
    }
  }

  "CheckSessionAction" should {
    /*"get session id should return None if keystore session id is not present" in {
      // set up
      val request = FakeRequest()
      object BaseFrontendController extends BaseFrontendController with SessionProvider {}

      // test
      val maybeSessionId = BaseFrontendController.getSessionId()(request)

      // check
      maybeSessionId shouldBe empty
    }

    "get session id should return Some if keystore session id is present" in {
      // set up
      val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
      object BaseFrontendController extends BaseFrontendController with SessionProvider {}

      // test
      val maybeSessionId = BaseFrontendController.getSessionId()(request)

      // check
      maybeSessionId should not be None
    }

    "withSession should redirect if no session" in {
      // set up
      val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
      object BaseFrontendController extends BaseFrontendController with SessionProvider {
        override def getSessionId()(implicit request : Request[AnyContent]) : Option[String] = Some("NOSESSION")
      }

      // test
      val noSessionAction = BaseFrontendController.withSession { implicit request =>
        Future.successful(Ok("Done"))
      }
      val result = await(noSessionAction(request))

      // check
      status(result) shouldBe 303
    }

    "withSession should redirect if no session id" in {
      // set up
      val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
      object BaseFrontendController extends BaseFrontendController with SessionProvider {
        override def getSessionId()(implicit request : Request[AnyContent]) : Option[String] = None
      }

      // test
      val noSessionAction = BaseFrontendController.withSession { implicit request =>
        Future.successful(Ok("Done"))
      }
      val result = await(noSessionAction(request))

      // check
      status(result) shouldBe 303
    }

    "withSession should not redirect if session id is present" in {
      // set up
      val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
      object BaseFrontendController extends BaseFrontendController with SessionProvider {
        override def getSessionId()(implicit request : Request[AnyContent]) : Option[String] = Some("session-test")
      }

      // test
      val noSessionAction = BaseFrontendController.withSession { implicit request =>
        Future.successful(Ok("Done"))
      }
      val result = await(noSessionAction(request))

      // check
      status(result) shouldBe 200
    }*/
  }
}
