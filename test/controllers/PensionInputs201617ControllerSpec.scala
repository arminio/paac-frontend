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

class PensionInputs201617ControllerSpec extends test.BaseSpec {
  val endPointURL = "/paac/pensionInputsPost2015"
  val IS_DB = s"${KeystoreService.DB_FLAG_PREFIX}2016"
  val IS_DC = s"${KeystoreService.DC_FLAG_PREFIX}2016"

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object ControllerWithMockKeystore extends PensionInputs201617Controller with AppTestSettings {
      val kesystoreKey = "definedBenefit_2016"
      def keystore: KeystoreService = MockKeystore
    }
  }

  "PensionInputs201617Controller" when {
    "GET with routes" should {
      "not return result NOT_FOUND" in {
        val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL+"/2016"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "return 303 for valid GET request" in {
        val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL+"/2016"))
        status(result.get) shouldBe 303
      }

      "not return 200 for valid GET request" in {
        val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL+"/2016"))
        status(result.get) should not be 200
      }
    }

    "onPageLoad with GET request" should {
      "have keystore with DC flag = true and DB flag = false values, should have DC input field" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "false")
        MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "2016")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad(2016)(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include (s"""<input type="number" name="${DC_PREFIX}2016" id="${DC_PREFIX}2016" min="0" class="input--no-spinner" value='' """)
      }

      "have keystore with DC flag = false and DB flag = true values, should have DB input field" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "2016")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad(2016)(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include (s"""<input type="number" name="${DB_PREFIX}2016" id="${DB_PREFIX}2016" min="0" class="input--no-spinner" value='' """)
      }

      "have keystore with values when we revisit the same page displays values" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (s"${DB_PREFIX}2016" -> "40000")
        MockKeystore.map = MockKeystore.map + (s"${DC_PREFIX}2016" -> "60000")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "2016")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad(2016)(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include (s"""<input type="number" name="${DB_PREFIX}2016" id="${DB_PREFIX}2016" min="0" class="input--no-spinner" value='400' """)
        htmlPage should include (s"""<input type="number" name="${DC_PREFIX}2016" id="${DC_PREFIX}2016" min="0" class="input--no-spinner" value='600' """)
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

      "with DB and DC flags true should look for both DB and DC amounts and should go to next page" in new ControllerWithMockKeystore {
        // set up
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (IS_EDIT_KEY -> "false"),
                                                                          (DB_FLAG_PREFIX+2016 -> "true"),
                                                                          (DC_FLAG_PREFIX+2016 -> "true"),
                                                                          (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                          (SELECTED_INPUT_YEARS_KEY -> "2016"))
                                                             .withFormUrlEncodedBody((DB_PREFIX+"2016" -> "4"),
                                                                                     (DC_PREFIX+"2016" -> "6"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnothresholdincome")
        MockKeystore.map(DB_PREFIX+"2016") shouldBe "400"
        MockKeystore.map(DC_PREFIX+"2016") shouldBe "600"
      }

      "with DB flag true and DC flag false should go to next page" in new ControllerWithMockKeystore {
        // set up
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (IS_EDIT_KEY -> "false"),
                                                                          (DB_FLAG_PREFIX+2016 -> "true"),
                                                                          (DC_FLAG_PREFIX+2016 -> "false"),
                                                                          (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                          (SELECTED_INPUT_YEARS_KEY -> "2016"))
                                                             .withFormUrlEncodedBody((DB_PREFIX+"2016" -> "4"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnothresholdincome")
        MockKeystore.map(DB_PREFIX+"2016") shouldBe "400"
      }

      "with DB flag false and DC flag true should go to next page" in new ControllerWithMockKeystore {
        // set up
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (IS_EDIT_KEY -> "false"),
                                                                          (DB_FLAG_PREFIX+2016 -> "false"),
                                                                          (DC_FLAG_PREFIX+2016 -> "true"),
                                                                          (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                          (SELECTED_INPUT_YEARS_KEY -> "2016"))
                                                             .withFormUrlEncodedBody((DC_PREFIX+"2016" -> "6"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnothresholdincome")
        MockKeystore.map(DC_PREFIX+"2016") shouldBe "600"
      }

      "with isEdit = false, DB = true and DC = false with invalid DB Input should display the same page with errors" in new ControllerWithMockKeystore {
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (IS_EDIT_KEY -> "false"),
                                                                          (DB_FLAG_PREFIX+2016 -> "true"),
                                                                          (DC_FLAG_PREFIX+2016 -> "false"),
                                                                          (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                          (SELECTED_INPUT_YEARS_KEY -> "2016"))
                                                             .withFormUrlEncodedBody(DB_PREFIX+"2016" -> "-400")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include (s"""<a href="#${DB_PREFIX}2016" style="color:#b10e1e;font-weight: bold;">Enter an amount that is £5,000,000 or less.</a>""")
      }

      "with isEdit = false, DB = true and DC = false with empty DB Input should display the same page with errors" in new ControllerWithMockKeystore {
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (IS_EDIT_KEY -> "false"),
                                                                          (DB_FLAG_PREFIX+2016 -> "true"),
                                                                          (DC_FLAG_PREFIX+2016 -> "false"),
                                                                          (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                          (SELECTED_INPUT_YEARS_KEY -> "2016"))
                                                             .withFormUrlEncodedBody(DB_PREFIX+"2016" -> "")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include (s"""<a href="#${DB_PREFIX}2016" style="color:#b10e1e;font-weight: bold;">Enter an amount that contains only numbers.</a>""")
      }

      "with isEdit = false, DB = false and DC = true with empty DC Input should display the same page with errors" in new ControllerWithMockKeystore {
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (IS_EDIT_KEY -> "false"),
                                                                          (DB_FLAG_PREFIX+2016 -> "false"),
                                                                          (DC_FLAG_PREFIX+2016 -> "true"),
                                                                          (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                          (SELECTED_INPUT_YEARS_KEY -> "2016"))
                                                             .withFormUrlEncodedBody(DC_PREFIX+"2016" -> "")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include (s"""<a href="#${DC_PREFIX}2016" style="color:#b10e1e;font-weight: bold;">Enter an amount that contains only numbers.</a>""")
      }
    }
  }

  "onBack" should {
    "redirect to scheme selection page" in new ControllerWithMockKeystore {
      // set up
      implicit val request = FakeRequest(GET,"/paac/back").withSession((SessionKeys.sessionId,SESSION_ID),
                                                                       (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                       (SELECTED_INPUT_YEARS_KEY -> "2016"))

      // test
      val result : Future[Result] = ControllerWithMockKeystore.onBack(2016)(request)

      // check
      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some("/paac/scheme/2016")
    }
  }
}
