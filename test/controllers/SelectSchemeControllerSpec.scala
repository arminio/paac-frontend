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

import form._
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.KeystoreService
import service.KeystoreService._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

class SelectSchemeControllerSpec extends test.BaseSpec {
  val endPointURL = "/paac/scheme/2015"
  val postEndPointURL = "/paac/scheme"

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object MockSelectSchemeControllerWithMockKeystore extends SelectSchemeController with AppTestSettings {
      def keystore: KeystoreService = MockKeystore
    }
  }

  "SelectSchemeController" when {
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
      "have keystore with no values and display select scheme options" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = MockSelectSchemeControllerWithMockKeystore.onPageLoad(2015)(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include (""" <input type="checkbox" id="definedBenefit"""")
        htmlPage should include (""" <input type="checkbox" id="definedContribution"""")
        dumpHtml("scheme", htmlPage)
      }

      "have keystore with DB and DC schemeType flag value when we revisit the same page" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (DB_FLAG_PREFIX + "2015" -> "true")
        MockKeystore.map = MockKeystore.map + (DC_FLAG_PREFIX + "2015" -> "true")

        // test
        val result : Future[Result] = MockSelectSchemeControllerWithMockKeystore.onPageLoad(2015)(request)

        // check
        status(result) shouldBe 200
      }

    }

    "onSubmit with POST request" should {
      "not return result NOT_FOUND" in {
        val result: Option[Future[Result]] = route(FakeRequest(POST, postEndPointURL))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "return 303 for valid GET request" in {
        val result: Option[Future[Result]] = route(FakeRequest(POST, postEndPointURL))
        status(result.get) shouldBe 303
      }

      "with valid DB and DC schemeType flag value should save to keystore" in new ControllerWithMockKeystore{
        // set up
        val sessionData = List((FIRST_DC_YEAR_KEY -> ""),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"),
                               (CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, postEndPointURL).withSession(sessionData: _*)
                                               .withFormUrlEncodedBody(("definedContribution" -> "true"),("definedBenefit" -> "true"),("year"->"2015"),("firstDCYear"->"2015"))

        // test
        val result: Future[Result] = MockSelectSchemeControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key (DB_FLAG_PREFIX + "2015")
        MockKeystore.map should contain key (DC_FLAG_PREFIX + "2015")
        MockKeystore.map should contain value ("true")
      }

      "with invalid DB and DC schemeType flag value should show errors" in new ControllerWithMockKeystore{
        // set up
        val sessionData = List((FIRST_DC_YEAR_KEY -> ""),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"),
                               (CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, postEndPointURL).withSession(sessionData: _*)
                                               .withFormUrlEncodedBody(("definedContribution" -> ""),
                                                                       ("definedBenefit" -> ""),
                                                                       ("year"->"2015"),
                                                                       ("firstDCYear"->"2015"))

        // test
        val result: Future[Result] = MockSelectSchemeControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("There is a problem")
        dumpHtml("error_scheme", htmlPage)
      }

      "with false DB and DC schemeType flag value should show errors" in new ControllerWithMockKeystore{
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, postEndPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                               .withFormUrlEncodedBody(("definedContribution" -> "false"),
                                                                       ("definedBenefit" -> "false"),
                                                                       ("year"->"2015"),
                                                                       ("firstDCYear"->"2015"))
        MockKeystore.map = MockKeystore.map + (FIRST_DC_YEAR_KEY -> "2015")

        // test
        val result: Future[Result] = MockSelectSchemeControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Choose at least 1 type of pension scheme.")
      }

      "set FirstDCYear flag" should {
        "when firstDCYear is empty set to year" in new ControllerWithMockKeystore {
          // set up
          val sessionData = List((FIRST_DC_YEAR_KEY -> ""),
                       (SELECTED_INPUT_YEARS_KEY -> "2015"),
                       (CURRENT_INPUT_YEAR_KEY -> "2015"),
                       (SessionKeys.sessionId,SESSION_ID))
          implicit val request = FakeRequest(POST, postEndPointURL).withSession(sessionData: _*)
                                                 .withFormUrlEncodedBody(("definedContribution" -> "true"),
                                                                         ("definedBenefit" -> "false"),
                                                                         ("year"->"2015"),
                                                                         ("firstDCYear"->""))

          // test
          val result: Future[Result] = MockSelectSchemeControllerWithMockKeystore.onSubmit()(request)

          // check
          status(result) shouldBe 303
          MockKeystore.map(FIRST_DC_YEAR_KEY) shouldBe "2015"
        }
        "when firstDCYear is not empty set to ''" in new ControllerWithMockKeystore {
          // set up
          val sessionData = List((FIRST_DC_YEAR_KEY -> ""),
                       (SELECTED_INPUT_YEARS_KEY -> "2015"),
                       (CURRENT_INPUT_YEAR_KEY -> "2015"),
                       (SessionKeys.sessionId,SESSION_ID))
          implicit val request = FakeRequest(POST, postEndPointURL).withSession(sessionData: _*)
                                                 .withFormUrlEncodedBody(("definedContribution" -> "false"),
                                                                         ("definedBenefit" -> "true"),
                                                                         ("year"->"2015"),
                                                                         ("firstDCYear"->"2015"))

          // test
          val result: Future[Result] = await(MockSelectSchemeControllerWithMockKeystore.onSubmit()(request))

          // check
          //info(contentAsString(result))
          status(result) shouldBe 303
          MockKeystore.map(FIRST_DC_YEAR_KEY) shouldBe ""
        }
      }
      "with valid schemeType flag for after 2015 year value should save to keystore" in new ControllerWithMockKeystore{
        // set up
        val sessionData = List((FIRST_DC_YEAR_KEY -> ""),
                               (SELECTED_INPUT_YEARS_KEY -> "2016"),
                               (CURRENT_INPUT_YEAR_KEY -> "2016"),
                               (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, postEndPointURL).withSession(sessionData: _*)
                                               .withFormUrlEncodedBody(("definedContribution" -> "true"),("definedBenefit" -> "false"),("year"->"2016"),("firstDCYear"->"2016"))

        // test
        val result: Future[Result] = MockSelectSchemeControllerWithMockKeystore.onSubmit()(request)

        // check
        val data = session(result).data
        data should contain key (DB_FLAG_PREFIX + "2016")
        data should contain key (DC_FLAG_PREFIX + "2016")
        data(DC_FLAG_PREFIX + "2016") shouldBe "true"
        data(DB_FLAG_PREFIX + "2016") shouldBe "false"
        data should not contain key (DC_PREFIX+"2016")
        data should not contain key (DB_PREFIX+"2016")
      }
      "with valid schemeType flag changing 2015 flags should update keystore" in new ControllerWithMockKeystore{
        // set up
        val sessionData = List((FIRST_DC_YEAR_KEY -> ""),
                               (SELECTED_INPUT_YEARS_KEY -> "2016,2015"),
                               (CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, postEndPointURL).withSession(sessionData: _*)
                                               .withFormUrlEncodedBody(("definedContribution" -> "true"),("definedBenefit" -> "false"),("year"->"2015"),("firstDCYear"->"2016"))

        // test
        val result: Future[Result] = MockSelectSchemeControllerWithMockKeystore.onSubmit()(request)

        // check
        val data = session(result).data
        data should contain key (DB_FLAG_PREFIX + "2015")
        data should contain key (DC_FLAG_PREFIX + "2015")
        data(DC_FLAG_PREFIX + "2015") shouldBe "true"
        data(DB_FLAG_PREFIX + "2015") shouldBe "false"
        data should not contain key (P1_DB_KEY)
        data should not contain key (P2_DB_KEY)
      }
      "with valid schemeType flag changing 2016 flags should update keystore" in new ControllerWithMockKeystore{
        // set up
        val sessionData = List((FIRST_DC_YEAR_KEY -> ""),
                               (SELECTED_INPUT_YEARS_KEY -> "2016,2015"),
                               (CURRENT_INPUT_YEAR_KEY -> "2016"),
                               (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, postEndPointURL).withSession(sessionData: _*)
                                               .withFormUrlEncodedBody(("definedContribution" -> "false"),("definedBenefit" -> "true"),("year"->"2016"),("firstDCYear"->"2016"))

        // test
        val result: Future[Result] = MockSelectSchemeControllerWithMockKeystore.onSubmit()(request)

        // check
        val data = session(result).data
        data should contain key (DB_FLAG_PREFIX + "2016")
        data should contain key (DC_FLAG_PREFIX + "2016")
        data(DC_FLAG_PREFIX + "2016") shouldBe "false"
        data(DB_FLAG_PREFIX + "2016") shouldBe "true"
        data should not contain key (DC_PREFIX+"2016")
        data should not contain key (DB_PREFIX+"2016")
      }
      "with valid schemeType flag changing flag should delete trigger date and amount" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((FIRST_DC_YEAR_KEY -> ""),
                               (SELECTED_INPUT_YEARS_KEY -> "2016,2015"),
                               (CURRENT_INPUT_YEAR_KEY -> "2016"),
                               (TRIGGER_DATE_KEY -> "2017-01-01"),
                               (TRIGGER_DC_KEY -> "123400"),
                               (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, postEndPointURL).withSession(sessionData: _*)
                                               .withFormUrlEncodedBody(("definedContribution" -> "false"),("definedBenefit" -> "true"),("year"->"2016"),("firstDCYear"->"2016"))

        // test
        val result: Future[Result] = MockSelectSchemeControllerWithMockKeystore.onSubmit()(request)

        // check
        val data = session(result).data
        data should not contain key (TRIGGER_DATE_KEY)
        data should not contain key (TRIGGER_DC_KEY)
      }
      "with valid schemeType flag changing flag should not delete trigger date and amount if not for current year" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((FIRST_DC_YEAR_KEY -> ""),
                               (SELECTED_INPUT_YEARS_KEY -> "2016,2015"),
                               (CURRENT_INPUT_YEAR_KEY -> "2016"),
                               (TRIGGER_DATE_KEY -> "2015-07-01"),
                               (TRIGGER_DC_KEY -> "123400"),
                               (SessionKeys.sessionId,SESSION_ID))
        implicit val request = FakeRequest(POST, postEndPointURL).withSession(sessionData: _*)
                                               .withFormUrlEncodedBody(("definedContribution" -> "false"),("definedBenefit" -> "true"),("year"->"2016"),("firstDCYear"->"2016"))

        // test
        val result: Future[Result] = MockSelectSchemeControllerWithMockKeystore.onSubmit()(request)

        // check
        val data = session(result).data
        data should contain key (TRIGGER_DATE_KEY)
        data should contain key (TRIGGER_DC_KEY)
      }
    }

   "onBack" should {
      "redirect to tax years selection page" in new ControllerWithMockKeystore {
        // set up
        implicit val request = FakeRequest(GET,"/paac/back").withSession((SessionKeys.sessionId,SESSION_ID),
                                                                         (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                         (SELECTED_INPUT_YEARS_KEY -> "2016"))

        // test
        val result : Future[Result] = MockSelectSchemeControllerWithMockKeystore.onBack(2015)(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/taxyearselection")
      }
    }
  }

  "SelectSchemeForm" should {
    "correctly unbind" in {
      // set up
      val model = SelectSchemeModel(true, true, "2015", 2015)
      val theForm = SelectSchemeForm.form.bind(Map("definedContribution" -> "false","definedContribution" -> "false","firstDCYear"->"2015","year"->"2015"))

      // test
      val map = theForm.mapping.unbind(model)

      // check
      map("definedContribution") shouldBe "true"
      map("definedContribution") shouldBe "true"
    }
  }
}
