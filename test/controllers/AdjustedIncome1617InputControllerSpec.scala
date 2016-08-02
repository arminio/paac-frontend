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
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future

class AdjustedIncome1617InputControllerSpec extends test.BaseSpec {

  val endPointURL = "/paac/adjustedincome"
  val YES_NO_TI_KEY= s"${TI_YES_NO_KEY_PREFIX}2016"
  val AI_KEY= s"${AI_PREFIX}2016"

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object ControllerWithMockKeystore extends AdjustedIncome1617InputController {
      val kesystoreKey = "adjustedIncome_2016"
      def keystore: KeystoreService = MockKeystore
    }
  }


  "AdjustedIncome1617InputController" when {
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

    "onPageLoad with GET request" should {
      "have keystore with Current Year flag = 2016 value, should have AI input field" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET, "").withSession {
          (SessionKeys.sessionId, SESSION_ID)
        }
        MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "2016")
        MockKeystore.map = MockKeystore.map + (YES_NO_TI_KEY -> "Yes")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include("""<input type="number" name="adjustedIncome.amount_2016" id="adjustedIncome.amount_2016" """)
      }

      "have keystore with Current Year flag have empty string value, should NOT have AI input field" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET, "").withSession(
          (SessionKeys.sessionId, SESSION_ID),
          (SELECTED_INPUT_YEARS_KEY -> "")
        )
        MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "")
        MockKeystore.map = MockKeystore.map + (YES_NO_TI_KEY -> "Yes")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 303
        val htmlPage = contentAsString(await(result))
        htmlPage should include("")
        htmlPage should not include("""<input type="number" name="adjustedIncome.amount_2016" id="adjustedIncome.amount_2016" """)
      }

      "have keystore with Current Year flag = 2015 value, should NOT have AI input field" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET, "").withSession(
          (SessionKeys.sessionId, SESSION_ID),
          (SELECTED_INPUT_YEARS_KEY -> "2015")
        )
        MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "2015")
        MockKeystore.map = MockKeystore.map + (YES_NO_TI_KEY -> "Yes")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 303
        val htmlPage = contentAsString(await(result))
        htmlPage should include("")
        htmlPage should not include("""<input type="number" name="adjustedIncome.amount_2016" id="adjustedIncome.amount_2016" """)
      }


      "have keystore with adjustedIncome_2016 value when we revisit the same page"  in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (AI_KEY -> "1000")
        MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "2016")
        MockKeystore.map = MockKeystore.map + (YES_NO_TI_KEY -> "Yes")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        MockKeystore.map should contain key ("adjustedIncome_2016")
        MockKeystore.map should contain value ("1000")
      }
    }

    "onSubmit with POST request" should {
      "not return result NOT_FOUND" in {
        val result: Option[Future[Result]] = route(FakeRequest(POST, endPointURL))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "return 303 for valid GET request"  in {
        val result: Option[Future[Result]] = route(FakeRequest(POST, endPointURL))
        status(result.get) shouldBe 303
      }

      "with Current Year flag = 2016 and AI field has some value should not go to trigger question page" in new ControllerWithMockKeystore {
        // set up
        var sessionData = List(("isEdit" -> "false"),
                              (SELECTED_INPUT_YEARS_KEY -> "2016,2015"),
                              (CURRENT_INPUT_YEAR_KEY -> "2016"),
                              (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                             .withFormUrlEncodedBody("adjustedIncome.amount_2016" -> "20000",
                                                                                     "year" -> "2016",
                                                                                     "isEdit" -> "false")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/scheme/2015")
      }

      "with Current Year flag = 2016 and AI field does NOT have value should go to same page with some error message" in new ControllerWithMockKeystore {
        // set up
        var sessionData = List(("isEdit" -> "false"),
                              (SELECTED_INPUT_YEARS_KEY -> "2016,2015"),
                              (CURRENT_INPUT_YEAR_KEY -> "2016"),
                              (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                             .withFormUrlEncodedBody("adjustedIncome.amount_2016" -> "",
                                                                                     "year" -> "2016",
                                                                                     "isEdit" -> "false")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        redirectLocation(result) shouldBe None
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter your Adjusted Income for this year even if it is 0.")
      }

      "with Current Year flag = 2016 and AI field have invalid value should go to same page with some error message" in new ControllerWithMockKeystore {
        // set up
        var sessionData = List(("isEdit" -> "false"),
                              (SELECTED_INPUT_YEARS_KEY -> "2016,2015"),
                              (CURRENT_INPUT_YEAR_KEY -> "2016"),
                              (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                             .withFormUrlEncodedBody("adjustedIncome.amount_2016" -> "-1000",
                                                                                     "year" -> "2016",
                                                                                     "isEdit" -> "false")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        redirectLocation(result) shouldBe None
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("2016 amount was too small and must be either £0 or greater.")
      }

      "with Edit functionality Current Year flag = 2016 and AI field has some value should reviews page" in new ControllerWithMockKeystore {
        // set up
        var sessionData = List(("isEdit" -> "true"),
                              (SELECTED_INPUT_YEARS_KEY -> "2016,2015"),
                              (CURRENT_INPUT_YEAR_KEY -> "2016"),
                              (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                             .withFormUrlEncodedBody("adjustedIncome.amount_2016" -> "20000",
                                                                                     "year" -> "2016",
                                                                                     "isEdit" -> "true")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/review")
      }
    }

    "onBack" should {
      "redirect to scheme selection page" in new ControllerWithMockKeystore {
        // set up
        var sessionData = List(("isEdit" -> "false"),
                              (SELECTED_INPUT_YEARS_KEY -> "2016,2015"),
                              (CURRENT_INPUT_YEAR_KEY -> "2016"),
                              (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(GET,"/paac/back").withSession(sessionData: _*)

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onBack()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnothresholdincome")
      }
    }
  }

}
