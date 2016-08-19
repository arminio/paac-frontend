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

import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.KeystoreService
import service.KeystoreService._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

class PostTriggerPensionInputsControllerSpec extends test.BaseSpec {
  val endPointURL = "/paac/moneyPurchasePostTriggerValue"
  val DC_2016_FLAG = s"${DC_FLAG_PREFIX}2016"

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2015")
    MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2015")
    object ControllerWithMockKeystore extends PostTriggerPensionInputsController with AppTestSettings {
      def keystore: KeystoreService = MockKeystore
    }
  }

  "onPageLoad" should {
    "redirect if trigger date not present in keystore" in {
      val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL))
      result.isDefined shouldBe true
      status(result.get) shouldBe 303
    }

    "redirect if trigger date is blank in keystore" in new ControllerWithMockKeystore {
      MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "")
      val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                             (KeystoreService.IS_EDIT_KEY -> "true"),
                             (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2015"),
                             (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2015"))
      val request = FakeRequest(GET,"").withSession(sessionData: _*)

      val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

      status(result) shouldBe 303
    }

    "display p1 input amount page with previous value if trigger date was period 1" in new ControllerWithMockKeystore {
      // setup
      val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
      MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-6-15")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P1_TRIGGER_DC_KEY -> "1200")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P2_TRIGGER_DC_KEY -> "5600")

      // test
      val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include (s"""<input type="text" name="${P1_TRIGGER_DC_KEY}" id="${P1_TRIGGER_DC_KEY}" """)
    }

    "display p2 input amount page with previous value if trigger date was period 2" in new ControllerWithMockKeystore {
      // setup
      val sessionData = List((SessionKeys.sessionId,SESSION_ID))
      MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-11-15")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P1_TRIGGER_DC_KEY -> "1200")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P2_TRIGGER_DC_KEY -> "5600")
      val request = FakeRequest(GET,"").withSession(sessionData: _*)

      // test
      val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include (s"""<input type="text" name="${P2_TRIGGER_DC_KEY}" id="${P2_TRIGGER_DC_KEY}" """)
    }
  }

  "onSubmit" should {
    "display errors if amount is negative" in new ControllerWithMockKeystore {
      // set up
      val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                             (TRIGGER_DATE_KEY -> "2015-11-15"),
                             (IS_EDIT_KEY -> "false"),
                             (P1_TRIGGER_DC_KEY -> "1234"),
                             (P2_TRIGGER_DC_KEY -> "5678"),
                             (CURRENT_INPUT_YEAR_KEY, "2015"),
                             (SELECTED_INPUT_YEARS_KEY, "2015"))
      implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody((P2_TRIGGER_DC_KEY -> "-1"))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include ("Enter an amount that is £5,000,000 or less.")
    }

    "display errors if Period-2 DC amount is blank" in new ControllerWithMockKeystore {
      // set up
      val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                             (TRIGGER_DATE_KEY -> "2015-11-15"),
                             (IS_EDIT_KEY -> "false"),
                             (P1_TRIGGER_DC_KEY -> "1234"),
                             (P2_TRIGGER_DC_KEY -> "5678"),
                             (CURRENT_INPUT_YEAR_KEY, "2015"),
                             (SELECTED_INPUT_YEARS_KEY, "2015"))
      implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody((P2_TRIGGER_DC_KEY -> ""))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include ("Enter your defined contribution pension savings for rest of period 2 even if it is 0.")
    }

    "display errors if Period-1 DC amount is blank" in new ControllerWithMockKeystore {
      // set up
      val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                             (TRIGGER_DATE_KEY -> "2015-4-15"),
                             (IS_EDIT_KEY -> "false"),
                             (P1_TRIGGER_DC_KEY -> "1234"),
                             (P2_TRIGGER_DC_KEY -> "5678"),
                             (CURRENT_INPUT_YEAR_KEY, "2015"),
                             (SELECTED_INPUT_YEARS_KEY, "2015"))
      implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody((P1_TRIGGER_DC_KEY -> ""))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include ("Enter your defined contribution pension savings for rest of period 1 even if it is 0.")
    }

    "display errors if amount is blank when trigger is 2016" in new ControllerWithMockKeystore {
      // set up
      val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                             (TRIGGER_DATE_KEY -> "2016-11-15"),
                             (IS_EDIT_KEY -> "false"),
                             (DC_2016_FLAG -> "true"),
                             (CURRENT_INPUT_YEAR_KEY, "2016"),
                             (SELECTED_INPUT_YEARS_KEY, "2016"))
      implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody((TRIGGER_DC_KEY -> ""))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include ("Enter your defined contribution pension savings for rest of 2016 to 2017 even if it is 0.")
    }

    "saves p2 amount in keystore if valid form" in new ControllerWithMockKeystore {
      // set up
      val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                             (TRIGGER_DATE_KEY -> "2015-11-15"),
                             (IS_EDIT_KEY -> "false"),
                             (CURRENT_INPUT_YEAR_KEY, "2015"),
                             (SELECTED_INPUT_YEARS_KEY, "2015"))
      implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody((P2_TRIGGER_DC_KEY -> "40000"))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 303
      MockKeystore.map should contain key (KeystoreService.P2_TRIGGER_DC_KEY)
      MockKeystore.map should contain value ("4000000")
    }

    "saves p1 amount in keystore if valid form" in new ControllerWithMockKeystore {
      // set up
      val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                             (TRIGGER_DATE_KEY -> "2015-4-15"),
                             (IS_EDIT_KEY -> "false"),
                             (CURRENT_INPUT_YEAR_KEY, "2015"),
                             (SELECTED_INPUT_YEARS_KEY, "2015"))
      implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody((P1_TRIGGER_DC_KEY -> "40000"))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 303
      MockKeystore.map should contain key (KeystoreService.P1_TRIGGER_DC_KEY)
      MockKeystore.map should contain value ("4000000")
    }
  }

    "onBack" should {
      "during edit return to review page" in new ControllerWithMockKeystore {
        // set up
        val request = FakeRequest(GET,"").withSession((SessionKeys.sessionId,SESSION_ID),
                                                      (KeystoreService.IS_EDIT_KEY -> "true"),
                                                      (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2015"),
                                                      (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2015"))

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onBack()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/review")
      }

      "during edit return to date page" in new ControllerWithMockKeystore {
        // set up
        val request = FakeRequest(GET,"").withSession((SessionKeys.sessionId,SESSION_ID),
                                                      (KeystoreService.IS_EDIT_KEY -> "false"),
                                                      (KeystoreService.FIRST_DC_YEAR_KEY -> "2015"),
                                                      (KeystoreService.TE_YES_NO_KEY -> "Yes"),
                                                      (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2015"),
                                                      (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2015"))

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onBack()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/dateofmpaate")
      }
    }
}
