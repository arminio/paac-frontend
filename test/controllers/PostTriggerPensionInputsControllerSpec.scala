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
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.{FakeApplication, FakeRequest}
import service.KeystoreService
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import scala.concurrent.Future
import play.api.Play
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._

import java.util.UUID

class PostTriggerPensionInputsControllerSpec extends test.BaseSpec {
  val endPointURL = "/paac/moneyPurchasePostTriggerValue"

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2015")
    MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2015")
    object ControllerWithMockKeystore extends PostTriggerPensionInputsController {
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
      htmlPage should include ("""<input type="number" name="year2015.postTriggerDcAmount2015P1" id="year2015.postTriggerDcAmount2015P1" """)
    }

    "display p2 input amount page with previous value if trigger date was period 2" in new ControllerWithMockKeystore {
      // setup
      val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
      MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-11-15")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P1_TRIGGER_DC_KEY -> "1200")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P2_TRIGGER_DC_KEY -> "5600")

      // test
      val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include ("""<input type="number" name="year2015.postTriggerDcAmount2015P2" id="year2015.postTriggerDcAmount2015P2" """)
    }
  }

  "onSubmit" should {
    "display errors if amount is negative" in new ControllerWithMockKeystore {
      // set up
      MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-11-15")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P1_TRIGGER_DC_KEY -> "1234")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P2_TRIGGER_DC_KEY -> "5678")
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("year2015.postTriggerDcAmount2015P2" -> "-1"),
                               ("triggerDate", "2015-11-15"))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include ("""2015 amount was too small and must be either £0 or greater.""")
    }

    "display errors if amount is blank" in new ControllerWithMockKeystore {
      // set up
      val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                             (KeystoreService.TRIGGER_DATE_KEY -> "2015-11-15"),
                             ("isEdit" -> "false"),
                             (KeystoreService.P1_TRIGGER_DC_KEY -> "1234"),
                             (KeystoreService.P2_TRIGGER_DC_KEY -> "5678"),
                             (KeystoreService.CURRENT_INPUT_YEAR_KEY, "2015"),
                             (KeystoreService.SELECTED_INPUT_YEARS_KEY, "2015"))
      implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody(("year2015.postTriggerDcAmount2015P2" -> ""),
                               ("triggerDate", "2015-11-15"))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include ("""2015 amount was empty or negative. Please provide an amount between £0 and £5,000,000.""")
    }

    "display errors if amount is blank when trigger is 2016" in new ControllerWithMockKeystore {
      // set up
      val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                             (KeystoreService.TRIGGER_DATE_KEY -> "2016-11-15"),
                             ("isEdit" -> "false"),
                             (KeystoreService.CURRENT_INPUT_YEAR_KEY, "2016"),
                             (KeystoreService.SELECTED_INPUT_YEARS_KEY, "2016"))
      implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody(("triggerAmount" -> ""),
                               ("triggerDate", "2016-11-15"))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include ("""2016 amount was empty or negative. Please provide an amount between £0 and £5,000,000.""")
    }

    "saves p2 amount in keystore if valid form" in new ControllerWithMockKeystore {
      // set up
      val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                             (KeystoreService.TRIGGER_DATE_KEY -> "2015-11-15"),
                             ("isEdit" -> "false"),
                             (KeystoreService.CURRENT_INPUT_YEAR_KEY, "2015"),
                             (KeystoreService.SELECTED_INPUT_YEARS_KEY, "2015"))
      implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody(("year2015.postTriggerDcAmount2015P2" -> "40000"),
                               ("triggerDate", "2015-11-15"))

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
                             (KeystoreService.TRIGGER_DATE_KEY -> "2015-4-15"),
                             ("isEdit" -> "false"),
                             (KeystoreService.CURRENT_INPUT_YEAR_KEY, "2015"),
                             (KeystoreService.SELECTED_INPUT_YEARS_KEY, "2015"))
      implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody(("year2015.postTriggerDcAmount2015P1" -> "40000"),
                               ("triggerDate", "2015-4-15"))

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
