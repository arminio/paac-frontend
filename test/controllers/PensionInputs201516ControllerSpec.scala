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

class PensionInputs201516ControllerSpec extends test.BaseSpec {
  val endPointURL = "/paac/pensionInputs201516"
  val IS_DB = s"${DB_FLAG_PREFIX}2015"
  val IS_DC = s"${DC_FLAG_PREFIX}2015"

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object ControllerWithMockKeystore extends PensionInputs201516Controller with AppTestSettings {
      val kesystoreKey = "definedBenefit_2015_p1"
      def keystore: KeystoreService = MockKeystore
    }
  }

  "PensionInputs201516Controller" when {
    "GET with routes" should {
      "not return result NOT_FOUND"  in {
        val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "return 303 for valid GET request"  in {
        val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL))
        status(result.get) shouldBe 303
      }

      "not return 200 for valid GET request"  in {
        val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL))
        status(result.get) should not be 200
      }
    }

    "onPageLoad" should {
      "have keystore with definedContribution flag = true value, should have DC input field" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "false")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include (s"""<input type="text" name="${P1_DC_KEY}" """)
        htmlPage should include (s"""<input type="text" name="${P2_DC_KEY}" """)
      }

      "have keystore with defined benefit flag = true value, should have DB input fields" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include (s"""<input type="text" name="${P1_DB_KEY}" """)
        htmlPage should include (s"""<input type="text" name="${P2_DB_KEY}" """)
      }

      "have keystore values when we revisit the same page with db is true" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (P1_DB_KEY -> "100")
        MockKeystore.map = MockKeystore.map + (P2_DB_KEY -> "200")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include (s"""<input type="text" name="${P1_DB_KEY}" id="${P1_DB_KEY}" min="0" class="input--no-spinner" value='1' """)
        htmlPage should include (s"""<input type="text" name="${P2_DB_KEY}" id="${P2_DB_KEY}" min="0" class="input--no-spinner" value='2' """)
      }

      "have keystore with definedContribution flag = true value, should have DC input fields" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "false")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include (s"""<input type="text" name="${P1_DC_KEY}" """)
        htmlPage should include (s"""<input type="text" name="${P2_DC_KEY}" """)
      }

      "have keystore values when we revisit the same page with dc is true" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "false")
        MockKeystore.map = MockKeystore.map + (P1_DC_KEY -> "100")
        MockKeystore.map = MockKeystore.map + (P2_DC_KEY -> "200")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include (s"""<input type="text" name="${P1_DC_KEY}" id="${P1_DC_KEY}" min="0" class="input--no-spinner" value='1' """)
        htmlPage should include (s"""<input type="text" name="${P2_DC_KEY}" id="${P2_DC_KEY}" min="0" class="input--no-spinner" value='2' """)
      }
    }

    "onSubmit with POST request" should {
      "not return result NOT_FOUND"  in {
        val result: Option[Future[Result]] = route(FakeRequest(POST, endPointURL))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "return 303 for valid GET request"  in {
        val result: Option[Future[Result]] = route(FakeRequest(POST, endPointURL))
        status(result.get) shouldBe 303
      }

      "with dc flag false should not go to trigger question page" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List(("isEdit" -> "false"),
                              (IS_DB -> "true"),
                              (IS_DC -> "false"),
                              (SELECTED_INPUT_YEARS_KEY -> "2015,2014"),
                              (CURRENT_INPUT_YEAR_KEY -> "2015"),
                              (SessionKeys.sessionId -> SESSION_ID))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                             .withFormUrlEncodedBody((P1_DB_KEY -> "40000"),
                                                                                     (P2_DB_KEY -> "0"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs/2014")
      }

      "with invalid request redisplay page with errors" in new ControllerWithMockKeystore {
        val sessionData = List(("isEdit" -> "false"),
                              (IS_DB -> "true"),
                              (IS_DC -> "false"),
                              (SELECTED_INPUT_YEARS_KEY -> "2015,2014"),
                              (CURRENT_INPUT_YEAR_KEY -> "2015"),
                              (SessionKeys.sessionId -> SESSION_ID))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                             .withFormUrlEncodedBody((P1_DB_KEY -> "-40000"),
                                                                                     (P2_DB_KEY -> "0"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter an amount that is £5,000,000 or less.")
      }

      "with empty db amount redisplay page with errors" in new ControllerWithMockKeystore {
        val sessionData = List(("isEdit" -> "false"),
                              (IS_DB -> "true"),
                              (IS_DC -> "false"),
                              (SELECTED_INPUT_YEARS_KEY -> "2015,2014"),
                              (CURRENT_INPUT_YEAR_KEY -> "2015"),
                              (SessionKeys.sessionId -> SESSION_ID))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                             .withFormUrlEncodedBody((P1_DB_KEY -> ""),
                                                                                     (P2_DB_KEY -> ""))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter your total defined benefit pension savings for period 2 even if it is 0.")
        htmlPage should include ("Enter your total defined benefit pension savings for period 2 even if it is 0.")
      }

      "with empty dc amount redisplay page with errors" in new ControllerWithMockKeystore {
        val sessionData = List(("isEdit" -> "false"),
                              (IS_DB -> "false"),
                              (IS_DC -> "true"),
                              (SELECTED_INPUT_YEARS_KEY -> "2015,2014"),
                              (CURRENT_INPUT_YEAR_KEY -> "2015"),
                              (SessionKeys.sessionId -> SESSION_ID))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                             .withFormUrlEncodedBody((P1_DC_KEY -> ""),
                                                                                     (P2_DC_KEY -> ""))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter your total defined contribution pension savings for period 1 even if it is 0.")
        htmlPage should include ("Enter your total defined contribution pension savings for period 2 even if it is 0.")
      }

     "with valid defined benefit values should save to keystore" in new ControllerWithMockKeystore {
       // set up
        val sessionData = List((CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"),
                               (IS_EDIT_KEY -> "false"),
                               (IS_DB -> "true"),
                               (IS_DC -> "false"),
                               (SessionKeys.sessionId,SESSION_ID))
       implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
      .withFormUrlEncodedBody((P2_DB_KEY -> "1"),(P1_DB_KEY -> "2"))

       // test
       val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

       // check
       status(result) shouldBe 303
       MockKeystore.map(P1_DB_KEY) shouldBe "200"
       MockKeystore.map(P2_DB_KEY) shouldBe "100"
     }

     "with valid defined contribution values should save to keystore" in new ControllerWithMockKeystore {
       // set up
        val sessionData = List((CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"),
                               (IS_EDIT_KEY -> "false"),
                               (IS_DB -> "false"),
                               (IS_DC -> "true"),
                               (SessionKeys.sessionId,SESSION_ID))
       implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
      .withFormUrlEncodedBody((P2_DC_KEY -> "1"),(P1_DC_KEY -> "2"))

       // test
       val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

       // check
       status(result) shouldBe 303
       MockKeystore.map(P1_DC_KEY) shouldBe "200"
       MockKeystore.map(P2_DC_KEY) shouldBe "100"
     }

      "with invalid request redisplay page with errors 2" in new ControllerWithMockKeystore {
        val sessionData = List((CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"),
                               (IS_EDIT_KEY -> "false"),
                               (IS_DB -> "true"),
                               (IS_DC -> "false"),
                               (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                             .withFormUrlEncodedBody((P2_DB_KEY -> "-49999"),(P1_DB_KEY -> "1"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter an amount that is £5,000,000 or less.")
      }

      "with empty db amount redisplay page with errors 2" in new ControllerWithMockKeystore {
        val sessionData = List((CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"),
                               (IS_EDIT_KEY -> "false"),
                               (IS_DB -> "true"),
                               (IS_DC -> "false"),
                               (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                             .withFormUrlEncodedBody((P2_DB_KEY -> ""),(P1_DB_KEY -> "1"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter your total defined benefit pension savings for period 2 even if it is 0.")
      }

      "with empty dc amount redisplay page with errors 2" in new ControllerWithMockKeystore {
        val sessionData = List((CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"),
                               (IS_EDIT_KEY -> "false"),
                               (IS_DB -> "false"),
                               (IS_DC -> "true"),
                               (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                             .withFormUrlEncodedBody((P2_DC_KEY -> ""),(P1_DC_KEY -> "1"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter your total defined contribution pension savings for period 2 even if it is 0.")
      }
    }
  }

  "onBack" should {
    "redirect to scheme selection page" in new ControllerWithMockKeystore {
      // set up
      val sessionData = List(("isEdit" -> "false"),
                      (SELECTED_INPUT_YEARS_KEY -> "2015,2014"),
                      (CURRENT_INPUT_YEAR_KEY -> "2015"),
                      (SessionKeys.sessionId -> SESSION_ID))
      implicit val request = FakeRequest(GET,"/paac/back").withSession(sessionData: _*)
      MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "2015")
      MockKeystore.map = MockKeystore.map + (SELECTED_INPUT_YEARS_KEY -> "2015")

      // test
      val result : Future[Result] = ControllerWithMockKeystore.onBack()(request)

      // check
      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some("/paac/scheme/2015")
    }
  }
}