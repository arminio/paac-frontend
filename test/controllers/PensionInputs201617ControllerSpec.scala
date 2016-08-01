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

class PensionInputs201617ControllerSpec extends test.BaseSpec {
  val endPointURL = "/paac/pensionInputsPost2015"
  val IS_DB = s"${KeystoreService.DB_FLAG_PREFIX}2016"
  val IS_DC = s"${KeystoreService.DC_FLAG_PREFIX}2016"

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object ControllerWithMockKeystore extends PensionInputs201617Controller {
      val kesystoreKey = "definedBenefit_2016"
      def keystore: KeystoreService = MockKeystore
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
      "have keystore with DC flag = true and DB flag = false values, should have DC input field" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "false")
        MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "2016")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="definedContributions.amount_2016" """)
      }

      "have keystore with DC flag = false and DB flag = true values, should have DB input field" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (IS_DC -> "false")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "2016")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="definedBenefits.amount_2016" """)
      }

      "have keystore with definedBenefit_2016 value when we revisit the same page"  in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("definedBenefit_2016" -> "40000")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "2016")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        MockKeystore.map should contain key ("definedBenefit_2016")
        MockKeystore.map should contain value ("40000")
      }

      "have keystore with definedBenefit_2016 value and year empty when we revisit the same page"  in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("definedBenefit_2016" -> "40000")
        MockKeystore.map = MockKeystore.map + (IS_DC -> "true")
        MockKeystore.map = MockKeystore.map + (IS_DB -> "true")
        MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "-1")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac")
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

      "with DC flag false should NOT look for DC amounts and should go to next page" in new ControllerWithMockKeystore {
        // set up
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (IS_EDIT_KEY -> "false"),
                                                                          (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                          (SELECTED_INPUT_YEARS_KEY -> "2016"))
                                                             .withFormUrlEncodedBody("year"->"2016","isEdit" -> "false",
                                                                                     "isDefinedBenefit"->"true",
                                                                                     "isDefinedContribution"->"false",
                                                                                     "definedBenefits.amount_2016" -> "400")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnothresholdincome")
      }

      "with DB flag false should NOT look for DB amounts and should go to next page" in new ControllerWithMockKeystore {
        // set up
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (IS_EDIT_KEY -> "false"),
                                                                          (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                          (SELECTED_INPUT_YEARS_KEY -> "2016"))
                                                             .withFormUrlEncodedBody("year"->"2016","isEdit" -> "false",
                                                                                     "isDefinedBenefit"->"false",
                                                                                     "isDefinedContribution"->"true",
                                                                                     "definedContributions.amount_2016" -> "400")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnothresholdincome")
      }

      "with DB and DC flags true should look for both DB and DC amounts and should go to next page" in new ControllerWithMockKeystore {
        // set up
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (IS_EDIT_KEY -> "false"),
                                                                          (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                          (SELECTED_INPUT_YEARS_KEY -> "2016"))
                                                             .withFormUrlEncodedBody("year"->"2016","isEdit" -> "false",
                                                                                     "isDefinedBenefit"->"true",
                                                                                     "isDefinedContribution"->"true",
                                                                                     "definedBenefits.amount_2016" -> "100",
                                                                                     "definedContributions.amount_2016" -> "200")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnothresholdincome")
      }

      "with isEdit = false, DB = true and DC = false with invalid DB Input should display the same page with errors" in new ControllerWithMockKeystore {
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (IS_EDIT_KEY -> "false"),
                                                                          (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                          (SELECTED_INPUT_YEARS_KEY -> "2016"))
                                                             .withFormUrlEncodedBody("year"->"2016","isEdit" -> "true",
                                                                                     "isDefinedBenefit"->"true",
                                                                                     "isDefinedContribution"->"false",
                                                                                     "definedBenefits.amount_2016" -> "-40000")

        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("2016 amount was too small and must be either £0 or greater.")
      }

      "with isEdit = false, DB = true and DC = false with empty DB Input should display the same page with errors" in new ControllerWithMockKeystore {
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (IS_EDIT_KEY -> "false"),
                                                                          (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                          (SELECTED_INPUT_YEARS_KEY -> "2016"))
                                                             .withFormUrlEncodedBody("year"->"2016","isEdit" -> "false",
                                                                                     "isDefinedBenefit"->"true",
                                                                                     "isDefinedContribution"->"false",
                                                                                     "definedBenefits.amount_2016" -> "")
        // test
        val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("2016 defined benefit amount was incorrect or empty.")
      }

      "with isEdit = false, DB = false and DC = true with empty DC Input should display the same page with errors" in new ControllerWithMockKeystore {
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (IS_EDIT_KEY -> "false"),
                                                                          (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                          (SELECTED_INPUT_YEARS_KEY -> "2016"))
                                                             .withFormUrlEncodedBody("year"->"2016","isEdit" -> "false",
                                                                                     "isDefinedBenefit"->"false",
                                                                                     "isDefinedContribution"->"true",
                                                                                     "definedContributions.amount_2016" -> "")

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
      implicit val request = FakeRequest(GET,"/paac/back").withSession((SessionKeys.sessionId,SESSION_ID),
                                                                       (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                       (SELECTED_INPUT_YEARS_KEY -> "2016"))

      // test
      val result : Future[Result] = ControllerWithMockKeystore.onBack()(request)

      // check
      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some("/paac/scheme/2016")
    }
  }
}
