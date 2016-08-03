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
    object ControllerWithMockKeystore extends PensionInputs201516Controller {
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

    "onPageLoad with GET request" should {
      "have keystore with definedContribution flag = true value, should have DC input field"  in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "false")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="year2015.definedContribution_2015_p1" """)
      }

      "have keystore with definedBenefit flag = true value, should have DB input field"  in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="year2015.definedBenefit_2015_p1" """)
      }

      "have keystore with definedBenefit_2015_p1 value when we revisit the same page"  in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("definedBenefit_2015_p1" -> "40000")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        MockKeystore.map should contain key ("definedBenefit_2015_p1")
        MockKeystore.map should contain value ("40000")
      }
      "have keystore with definedContribution flag = true value, should have DC input field for P2" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "false")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="year2015.definedContribution_2015_p2" """)
      }

      "have keystore with definedBenefit flag = true value, should have DB input field for Period 2" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="year2015.definedBenefit_2015_p2" """)
      }

      "have keystore with definedBenefit_2015_p2 value when we revisit the same page"  in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("definedBenefit_2015_p2" -> "40000")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        MockKeystore.map should contain key ("definedBenefit_2015_p2")
        MockKeystore.map should contain value ("40000")
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
                                                             .withFormUrlEncodedBody(("year2015.definedBenefit_2015_p1" -> "40000"),
                                                               ("year2015.definedBenefit_2015_p2" -> "0"),
                                                               ("isDefinedBenefit","true"),
                                                               ("isDefinedContribution","false"),
                                                               ("isEdit","false"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs")
      }

//      "with valid definedBenefit_2015_p1 and in edit mode should redirect to review" in new ControllerWithMockKeystore {
//        // set up
//        MockKeystore.map = MockKeystore.map + ("isEdit" -> "true")
//        MockKeystore.map = MockKeystore.map + (DB_FLAG -> "true")
//        MockKeystore.map = MockKeystore.map + (DC_FLAG -> "false")
//        implicit val hc = HeaderCarrier()
//        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
      // .withFormUrlEncodedBody(("year2015.definedBenefit_2015_p1" -> "40000"),("year2015.definedContribution_2015_p1" -> ""))
//
//        // test
//        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)
//
//        // check
//        status(result) shouldBe 303
//        redirectLocation(result) shouldBe Some("/paac/review")
//      }

//      "with valid definedBenefit_2015_p1 should save to keystore" in new ControllerWithMockKeystore {
//        // set up
//        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
//        MockKeystore.map = MockKeystore.map + (DB_FLAG -> "true")
//        MockKeystore.map = MockKeystore.map + (DC_FLAG -> "false")
//        implicit val hc = HeaderCarrier()
//        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
      // .withFormUrlEncodedBody(("year2015.definedBenefit_2015_p1" -> "40000"),("year2015.definedContribution_2015_p1" -> ""))
//
//        // test
//        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)
//
//        // check
//        status(result) shouldBe 303
//        MockKeystore.map should contain key ("definedBenefit_2015_p1")
//        MockKeystore.map should contain value ("4000000")
//      }

      "with invalid request redisplay page with errors" in new ControllerWithMockKeystore {
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                                             .withFormUrlEncodedBody(("year2015.definedBenefit_2015_p1" -> "-40000"),
                                                               ("isDefinedBenefit","true"),
                                                               ("isDefinedContribution","false"),
                                                               ("isEdit","false"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter an amount that is £5,000,000 or less.")
      }

      "with empty db amount redisplay page with errors" in new ControllerWithMockKeystore {
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                                             .withFormUrlEncodedBody(("year2015.definedBenefit_2015_p1" -> ""),
                                                               ("isDefinedBenefit","true"),
                                                               ("isDefinedContribution","false"),
                                                               ("isEdit","false"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter your total defined benefit pension savings for period 1 even if it is 0.")
      }

      "with empty dc amount redisplay page with errors" in new ControllerWithMockKeystore {
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                                             .withFormUrlEncodedBody(("year2015.definedContribution_2015_p1" -> ""),
                                                               ("isDefinedBenefit","false"),
                                                               ("isDefinedContribution","true"),
                                                               ("isEdit","false"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter your total defined contribution pension savings for period 1 even if it is 0.")
      }

//      "with valid definedBenefit_2015_p1 should save to keystore" in new ControllerWithMockKeystore {
//        // set up
//        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
//        MockKeystore.map = MockKeystore.map + (DB_FLAG -> "true")
//        MockKeystore.map = MockKeystore.map + (DC_FLAG -> "false")
//        implicit val hc = HeaderCarrier()
//        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
      // .withFormUrlEncodedBody(("year2015.definedBenefit_2015_p2" -> "40000"),("year2015.definedContribution_2015_p2" -> ""))
//
//        // test
//        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)
//
//        // check
//        status(result) shouldBe 303
//        MockKeystore.map should contain key ("definedBenefit_2015_p2")
//        MockKeystore.map should contain value ("4000000")
//      }
//
//      "with valid amounts should redirect to trigger question page if dc scheme" in new ControllerWithMockKeystore {
//        // set up
//        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
//        MockKeystore.map = MockKeystore.map + (DB_FLAG -> "false")
//        MockKeystore.map = MockKeystore.map + (DC_FLAG -> "true")
//        implicit val hc = HeaderCarrier()
//        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
      // .withFormUrlEncodedBody(("year2015.definedContribution_2015_p2" -> "40000"),("year2015.definedBenefit_2015_p2" -> ""))
//
//        // test
//        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)
//
//        // check
//        status(result) shouldBe 303
//        redirectLocation(result) shouldBe Some("/paac/yesnompaate")
//      }
//
//      "with valid amounts should redirect to review page if not dc scheme" in new ControllerWithMockKeystore {
//        // set up
//        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
//        MockKeystore.map = MockKeystore.map + (DB_FLAG -> "true")
//        MockKeystore.map = MockKeystore.map + (DC_FLAG -> "false")
//        MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "2015")
//        MockKeystore.map = MockKeystore.map + (SELECTED_INPUT_YEARS_KEY -> "2015")
//        implicit val hc = HeaderCarrier()
//        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
      // .withFormUrlEncodedBody(("year2015.definedBenefit_2015_p2" -> "40000"),("year2015.definedContribution_2015_p2" -> ""))
//
//        // test
//        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)
//
//        // check
//        status(result) shouldBe 303
//        redirectLocation(result) shouldBe Some("/paac/review")
//      }

      "with invalid request redisplay page with errors 2" in new ControllerWithMockKeystore {
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                                             .withFormUrlEncodedBody(("year2015.definedBenefit_2015_p2" -> "-40000"),
                                                               ("isDefinedBenefit","true"),
                                                               ("isDefinedContribution","false"),
                                                               ("isEdit","false"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter an amount that is £5,000,000 or less.")
      }

      "with empty db amount redisplay page with errors 2" in new ControllerWithMockKeystore {
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                                             .withFormUrlEncodedBody(("year2015.definedBenefit_2015_p2" -> ""),
                                                               ("isDefinedBenefit","true"),
                                                               ("isDefinedContribution","false"),
                                                               ("isEdit","false"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter your total defined benefit pension savings for period 1 even if it is 0.")
      }

      "with empty dc amount redisplay page with errors 2" in new ControllerWithMockKeystore {
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                                             .withFormUrlEncodedBody(("year2015.definedContribution_2015_p2" -> ""),
                                                               ("isDefinedBenefit","false"),
                                                               ("isDefinedContribution","true"),
                                                               ("isEdit","false"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter your total defined contribution pension savings for period 1 even if it is 0.")
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