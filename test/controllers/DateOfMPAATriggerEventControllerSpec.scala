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
import play.api.Play
import play.api.mvc.{Result, Request}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, FakeApplication}
import service.KeystoreService
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class DateOfMPAATriggerEventControllerSpec extends test.BaseSpec {
  val endPointURL = "/paac/dateofmpaate"

  trait ControllerWithMockKeystore extends MockKeystoreFixture{
    object MockDateOfMPAATriggerEventControllerWithMockKeystore extends YesNoMPAATriggerEventAmountController {
      val yesNoKeystoreKey = "TRIGGER_DATE_KEY"
      override val keystore: KeystoreService = MockKeystore
    }
  }

  "DateOfMPAATriggerEventController" when {
    "GET with routes" should {
      "not return result NOT_FOUND" in {
        val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "return 303 for valid GET request" in {
        val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL))
        status(result.get) shouldBe 303
      }

      "not return 200 for valid GET request" in {
        val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL))
        status(result.get) should not be 200
      }
    }

//    "onPageLoad with GET request" should {
//      "have keystore with no values and display trigger event date" in new ControllerWithMockKeystore {
//        // setup
//        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
//
//        // test
//        val result : Future[Result] = MockDateOfMPAATriggerEventControllerWithMockKeystore.onPageLoad()(request)
//
//        // check
//        status(result) shouldBe 200
//        val htmlPage = contentAsString(await(result))
//      }
//      "have keystore with date option for Trigger Event date when we revisit the same page" in new ControllerWithMockKeystore {
//        // setup
//        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
//        MockKeystore.map = MockKeystore.map + ("DateOfMPAATriggerEvent" -> "18-05-2014")
//
//        // test
//        val result : Future[Result] = MockDateOfMPAATriggerEventControllerWithMockKeystore.onPageLoad()(request)
//
//        // check
//        status(result) shouldBe 200
//        MockKeystore.map should contain key "DateOfMPAATriggerEvent"
//        MockKeystore.map should contain value ("18-05-2014")
//      }
//    }

    "onSubmit with POST request" should {
//      "with invalid date key name should show same form with errors and response code 200" in new ControllerWithMockKeystore{
//        // set up
//        implicit val hc = HeaderCarrier()
//        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
//          .withFormUrlEncodedBody("DateOfMPAATriggerEvent" -> "15-08-2015")
//
//        // test
//        val result: Future[Result] = MockDateOfMPAATriggerEventControllerWithMockKeystore.onSubmit()(request)
//
//        // check
//        status(result) shouldBe 200
//      }
    }
  }
}
