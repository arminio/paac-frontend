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
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

class PensionInputs201617ControllerSpec extends test.BaseSpec {
  val endPointURL = "/paac/pensionInputs201617"
  val IS_DB = s"${KeystoreService.DB_FLAG_PREFIX}2016"
  val IS_DC = s"${KeystoreService.DC_FLAG_PREFIX}2016"

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object ControllerWithMockKeystore extends PensionInputs201617Controller {
      val kesystoreKey = "definedBenefit_2016"
      override val keystore: KeystoreService = MockKeystore
    }
  }

  "PensionInputs201617Controller" when {
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
        htmlPage should include ("""<input type="number" name="year2016.definedContribution" """)
      }

      "have keystore with definedBenefit flag = true value, should have DB input field" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="year2016.definedBenefit" """)
      }

      "have keystore with definedBenefit_2016 value when we revisit the same page"  in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("definedBenefit_2016" -> "40000")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        MockKeystore.map should contain key ("definedBenefit_2016")
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
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2016,2014")
        MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2016")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody("year2016.definedBenefit" -> "40000")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs")
      }

      "with dc flag false should go to review page when only 2016 selected" in new ControllerWithMockKeystore {
        // set up
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2016")
        MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2016")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody("year2016.definedBenefit" -> "40000")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/review")
      }

      "with invalid request redisplay page with errors" in new ControllerWithMockKeystore {
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("year2016.definedBenefit" -> "-40000"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("2016 amount was too small and must be either £0 or greater.")
      }

      "with empty db amount redisplay page with errors" in new ControllerWithMockKeystore {
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("year2016.definedBenefit" -> ""))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("2016 defined benefit amount was incorrect or empty.")
      }

      "with empty dc amount redisplay page with errors" in new ControllerWithMockKeystore {
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("year2016.definedContribution" -> ""))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("2016 defined contribution amount was incorrect or empty.")
      }

      "with invalid request redisplay page with errors 2" in new ControllerWithMockKeystore {
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("year2016.definedBenefit" -> "-40000"))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("2016 amount was too small and must be either £0 or greater.")
      }

      "with empty db amount redisplay page with errors 2" in new ControllerWithMockKeystore {
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("year2016.definedBenefit" -> ""))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("2016 defined benefit amount was incorrect or empty.")
      }

      "with empty dc amount redisplay page with errors 2" in new ControllerWithMockKeystore {
        MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("year2016.definedContribution2" -> ""))

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("2016 defined contribution amount was incorrect or empty.")
      }
    }
  }

  "onBack" should {
    "redirect to scheme selection page" in new ControllerWithMockKeystore {
      // set up
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(GET,"/paac/back").withSession{(SessionKeys.sessionId,SESSION_ID)}

      // test
      val result : Future[Result] = ControllerWithMockKeystore.onBack()(request)

      // check
      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some("/paac/scheme/2016")
    }
  }
}