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
import org.joda.time.LocalDate
import scala.concurrent.Future

class DateOfMPAATriggerEventControllerSpec extends test.BaseSpec {

  val endPointURL = "/paac/dateofmpaate"
  val DC_FLAG_PREFIX_2016 = s"${DC_FLAG_PREFIX}2016"
  val DC_FLAG_PREFIX_2015 = s"${DC_FLAG_PREFIX}2015"

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object ControllerWithMockKeystore extends DateOfMPAATriggerEventController with AppTestSettings {
      def keystore: KeystoreService = MockKeystore
    }
    MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "2015")
    MockKeystore.map = MockKeystore.map + (SELECTED_INPUT_YEARS_KEY -> "2015")
  }

  "DateOfMPAATriggerEventControllerSpec" when {
    "object should define keystore" in {
      DateOfMPAATriggerEventController.keystore should not be null
    }

    "onPageLoad" should {
      "display form page with no date if no keystore value" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (TRIGGER_DATE_KEY -> "")
        MockKeystore.map = MockKeystore.map + (P1_TRIGGER_DC_KEY -> "0")
        MockKeystore.map = MockKeystore.map + (P2_TRIGGER_DC_KEY -> "0")
        MockKeystore.map = MockKeystore.map + (IS_EDIT_KEY -> "false")


        // do it
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""""""")
        dumpHtml("empty_dateofmpaate", htmlPage)
      }

      "display form page with date if keystore value exists" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (TRIGGER_DATE_KEY -> "2015-11-1")
        MockKeystore.map = MockKeystore.map + (P1_TRIGGER_DC_KEY -> "0")
        MockKeystore.map = MockKeystore.map + (P2_TRIGGER_DC_KEY -> "0")
        MockKeystore.map = MockKeystore.map + (IS_EDIT_KEY -> "false")

        // do it
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""value="2015"""")
        htmlPage should include ("""value="11"""")
        htmlPage should include ("""value="1"""")
      }
    }

    "onSubmit" should {
      "with valid form save date into keystore" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                               (IS_EDIT_KEY -> "false"),
                               (TE_YES_NO_KEY -> "true"),
                               (CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (DC_FLAG_PREFIX_2015 -> "true"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "4"),
                                    ("dateOfMPAATriggerEvent.month" -> "7"),
                                    ("dateOfMPAATriggerEvent.year" -> "2015"),
                                    (P1_TRIGGER_DC_KEY -> "0"),
                                    (P2_TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DATE_KEY -> "2015-7-4"),
                                    ("originalDate" -> "2013-7-1"))

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key (TRIGGER_DATE_KEY)
        MockKeystore.map should contain value ("2015-07-04")
        redirectLocation(result) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
      }

      "with valid date redirect to moneyPurchasePostTriggerValue" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                               (IS_EDIT_KEY -> "false"),
                               (TE_YES_NO_KEY -> "true"),
                               (CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (DC_FLAG_PREFIX_2015 -> "true"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "4"),
                                    ("dateOfMPAATriggerEvent.month" -> "7"),
                                    ("dateOfMPAATriggerEvent.year" -> "2015"),
                                    (P1_TRIGGER_DC_KEY -> "0"),
                                    (P2_TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DATE_KEY -> "2015-7-4"),
                                    ("originalDate" -> "2013-7-1"))

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
      }

      "with last date in before p1 show invalid error" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                               (IS_EDIT_KEY -> "false"),
                               (TE_YES_NO_KEY -> "true"),
                               (CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (DC_FLAG_PREFIX_2015 -> "true"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "5"),
                                    ("dateOfMPAATriggerEvent.month" -> "4"),
                                    ("dateOfMPAATriggerEvent.year" -> "2015"),
                                    (P1_TRIGGER_DC_KEY -> "0"),
                                    (P2_TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DATE_KEY -> "2015-4-5"),
                                    (IS_EDIT_KEY -> "false"),
                                    ("originalDate" -> "2013-7-1"))

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter a date between 6 4 2015 and 5 4 2016 inclusive")
      }

      "with no date show invalid error" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                               (IS_EDIT_KEY -> "false"),
                               (TE_YES_NO_KEY -> "true"),
                               (CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (DC_FLAG_PREFIX_2015 -> "true"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> ""),
                                    ("dateOfMPAATriggerEvent.month" -> ""),
                                    ("dateOfMPAATriggerEvent.year" -> ""),
                                    (P1_TRIGGER_DC_KEY -> "0"),
                                    (P2_TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DATE_KEY -> ""),
                                    (IS_EDIT_KEY -> "false"),
                                    ("originalDate" -> "2013-7-1"))

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("You must specify a valid date")
      }

      "with bad date show invalid error" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                               (IS_EDIT_KEY -> "false"),
                               (TE_YES_NO_KEY -> "true"),
                               (CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (DC_FLAG_PREFIX_2015 -> "true"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "-1"),
                                    ("dateOfMPAATriggerEvent.month" -> "-1"),
                                    ("dateOfMPAATriggerEvent.year" -> "-1"),
                                    (P1_TRIGGER_DC_KEY -> "0"),
                                    (P2_TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DATE_KEY -> ""),
                                    (IS_EDIT_KEY -> "false"),
                                    ("originalDate" -> "2013-7-1"))

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("You must specify a valid date")
      }

      "with valid date redirect to next trigger amount page if edit flag not set" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                               (IS_EDIT_KEY -> "false"),
                               (TE_YES_NO_KEY -> "true"),
                               (CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (DC_FLAG_PREFIX_2015 -> "true"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015,2014"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "4"),
                                    ("dateOfMPAATriggerEvent.month" -> "7"),
                                    ("dateOfMPAATriggerEvent.year" -> "2015"),
                                    (P1_TRIGGER_DC_KEY -> "0"),
                                    (P2_TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DATE_KEY -> "2013-7-1"),
                                    ("originalDate" -> "2013-7-1"))

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
      }

      "with invalid form display page again" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                               (IS_EDIT_KEY -> "false"),
                               (TE_YES_NO_KEY -> "true"),
                               (CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (DC_FLAG_PREFIX_2015 -> "true"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "4"),
                                    ("dateOfMPAATriggerEvent.month" -> "7"),
                                    ("dateOfMPAATriggerEvent.year" -> "2013"),
                                    (P1_TRIGGER_DC_KEY -> "0"),
                                    (P2_TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DC_KEY -> "0"),
                                    (TRIGGER_DATE_KEY -> "2013-7-1"),
                                    (IS_EDIT_KEY -> "false"),
                                    ("originalDate" -> "2013-7-1"))

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter a date between 6 4 2015 and 5 4 2016 inclusive")
      }
    }


    "onSubmit for TE Date Restriction for Selected TaxYear(s)" should {

      "with Selected TaxYear= 2016, should accept any date in 2016/17 Tax year" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                                                              (IS_EDIT_KEY -> "false"),
                                                              (TE_YES_NO_KEY -> "true"),
                                                              (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                              (DC_FLAG_PREFIX_2016 -> "true"),
                                                              (SELECTED_INPUT_YEARS_KEY -> "2016"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                              .withSession((SessionKeys.sessionId,SESSION_ID))
                                                              .withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "1"),
                                                                ("dateOfMPAATriggerEvent.month" -> "5"),
                                                                ("dateOfMPAATriggerEvent.year" -> "2016"),
                                                                (P1_TRIGGER_DC_KEY -> "0"),
                                                                (P2_TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DATE_KEY -> ""),
                                                                (IS_EDIT_KEY -> "false"),
                                                                ("originalDate" -> "2015-7-1"))
        MockKeystore.map = MockKeystore.map + (TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe  Some("/paac/moneyPurchasePostTriggerValue")
      }

      "with Selected TaxYear= 2016, should accept start date of 2016/17 Tax year" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                                                              (IS_EDIT_KEY -> "false"),
                                                              (TE_YES_NO_KEY -> "true"),
                                                              (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                              (DC_FLAG_PREFIX_2016 -> "true"),
                                                              (SELECTED_INPUT_YEARS_KEY -> "2016"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                              .withSession((SessionKeys.sessionId,SESSION_ID))
                                                              .withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "6"),
                                                                ("dateOfMPAATriggerEvent.month" -> "4"),
                                                                ("dateOfMPAATriggerEvent.year" -> "2016"),
                                                                (P1_TRIGGER_DC_KEY -> "0"),
                                                                (P2_TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DATE_KEY -> ""),
                                                                (IS_EDIT_KEY -> "false"),
                                                                ("originalDate" -> "2015-7-1"))
        MockKeystore.map = MockKeystore.map + (TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe  Some("/paac/moneyPurchasePostTriggerValue")
      }

      "with Selected TaxYear= 2016, should accept end date of 2016/17 Tax year" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                                                              (IS_EDIT_KEY -> "false"),
                                                              (TE_YES_NO_KEY -> "true"),
                                                              (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                              (DC_FLAG_PREFIX_2016 -> "true"),
                                                              (SELECTED_INPUT_YEARS_KEY -> "2016"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                              .withSession((SessionKeys.sessionId,SESSION_ID))
                                                              .withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "5"),
                                                                ("dateOfMPAATriggerEvent.month" -> "4"),
                                                                ("dateOfMPAATriggerEvent.year" -> "2017"),
                                                                (P1_TRIGGER_DC_KEY -> "0"),
                                                                (P2_TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DATE_KEY -> ""),
                                                                (IS_EDIT_KEY -> "false"),
                                                                ("originalDate" -> "2015-7-1"))
        MockKeystore.map = MockKeystore.map + (TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe  Some("/paac/moneyPurchasePostTriggerValue")
      }

      "with Selected TaxYear= 2016, should not accept other than 2016/17 Tax year date and should show invalid date error" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                                                              (IS_EDIT_KEY -> "false"),
                                                              (TE_YES_NO_KEY -> "true"),
                                                              (DC_FLAG_PREFIX_2016 -> "true"),
                                                              (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                              (SELECTED_INPUT_YEARS_KEY -> "2016"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                              .withSession((SessionKeys.sessionId,SESSION_ID))
                                                              .withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "1"),
                                                                ("dateOfMPAATriggerEvent.month" -> "1"),
                                                                ("dateOfMPAATriggerEvent.year" -> "2016"),
                                                                (P1_TRIGGER_DC_KEY -> "0"),
                                                                (P2_TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DATE_KEY -> ""),
                                                                (IS_EDIT_KEY -> "false"),
                                                                ("originalDate" -> "2015-7-1"))
        MockKeystore.map = MockKeystore.map + (TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter a date between 6 4 2016 and 5 4 2017 inclusive")
      }

      "with Selected TaxYear= 2015, should accept any date in 2015/16 Tax year" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                                                              (IS_EDIT_KEY -> "false"),
                                                              (TE_YES_NO_KEY -> "true"),
                                                              (CURRENT_INPUT_YEAR_KEY -> "2015"),
                                                              (DC_FLAG_PREFIX_2015 -> "true"),
                                                              (SELECTED_INPUT_YEARS_KEY -> "2015"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                              .withSession((SessionKeys.sessionId,SESSION_ID))
                                                              .withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "1"),
                                                                ("dateOfMPAATriggerEvent.month" -> "5"),
                                                                ("dateOfMPAATriggerEvent.year" -> "2015"),
                                                                (P1_TRIGGER_DC_KEY -> "0"),
                                                                (P2_TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DATE_KEY -> ""),
                                                                (IS_EDIT_KEY -> "false"),
                                                                ("originalDate" -> "2015-7-1"))
        MockKeystore.map = MockKeystore.map + (TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe  Some("/paac/moneyPurchasePostTriggerValue")
      }

      "with Selected TaxYear= 2015, should accept start date of 2015/16 Tax year" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                                                              (IS_EDIT_KEY -> "false"),
                                                              (TE_YES_NO_KEY -> "true"),
                                                              (CURRENT_INPUT_YEAR_KEY -> "2015"),
                                                              (DC_FLAG_PREFIX_2015 -> "true"),
                                                              (SELECTED_INPUT_YEARS_KEY -> "2015"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                              .withSession((SessionKeys.sessionId,SESSION_ID))
                                                              .withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "6"),
                                                                ("dateOfMPAATriggerEvent.month" -> "4"),
                                                                ("dateOfMPAATriggerEvent.year" -> "2015"),
                                                                (P1_TRIGGER_DC_KEY -> "0"),
                                                                (P2_TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DATE_KEY -> ""),
                                                                (IS_EDIT_KEY -> "false"),
                                                                ("originalDate" -> "2015-7-1"))
        MockKeystore.map = MockKeystore.map + (TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe  Some("/paac/moneyPurchasePostTriggerValue")
      }

      "with Selected TaxYear= 2015, should accept end date of 2015/16 Tax year" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                                                              (IS_EDIT_KEY -> "false"),
                                                              (TE_YES_NO_KEY -> "true"),
                                                              (CURRENT_INPUT_YEAR_KEY -> "2015"),
                                                              (DC_FLAG_PREFIX_2015 -> "true"),
                                                              (SELECTED_INPUT_YEARS_KEY -> "2015"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                              .withSession((SessionKeys.sessionId,SESSION_ID))
                                                              .withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "5"),
                                                                ("dateOfMPAATriggerEvent.month" -> "4"),
                                                                ("dateOfMPAATriggerEvent.year" -> "2016"),
                                                                (P1_TRIGGER_DC_KEY -> "0"),
                                                                (P2_TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DATE_KEY -> ""),
                                                                (IS_EDIT_KEY -> "false"),
                                                                ("originalDate" -> "2015-7-1"))
        MockKeystore.map = MockKeystore.map + (TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe  Some("/paac/moneyPurchasePostTriggerValue")
      }

      "with Selected TaxYear= 2015, should not accept other than 2015/16 Tax year date and should show invalid date error" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                                                              (IS_EDIT_KEY -> "false"),
                                                              (TE_YES_NO_KEY -> "true"),
                                                              (CURRENT_INPUT_YEAR_KEY -> "2015"),
                                                              (DC_FLAG_PREFIX_2015 -> "true"),
                                                              (SELECTED_INPUT_YEARS_KEY -> "2015"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                              .withSession((SessionKeys.sessionId,SESSION_ID))
                                                              .withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "1"),
                                                                ("dateOfMPAATriggerEvent.month" -> "5"),
                                                                ("dateOfMPAATriggerEvent.year" -> "2016"),
                                                                (P1_TRIGGER_DC_KEY -> "0"),
                                                                (P2_TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DATE_KEY -> ""),
                                                                (IS_EDIT_KEY -> "false"),
                                                                ("originalDate" -> "2015-7-1"))
        MockKeystore.map = MockKeystore.map + (TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter a date between 6 4 2015 and 5 4 2016 inclusive")
      }


      "with Selected TaxYear= '2016,2015', should accept any date in 2016/17 and 2015/16 Tax years" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                                                              (IS_EDIT_KEY -> "false"),
                                                              (TE_YES_NO_KEY -> "true"),
                                                              (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                              (DC_FLAG_PREFIX_2016 -> "true"),(DC_FLAG_PREFIX_2015 -> "true"),
                                                              (SELECTED_INPUT_YEARS_KEY -> "2016,2015"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                              .withSession((SessionKeys.sessionId,SESSION_ID))
                                                              .withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "6"),
                                                                ("dateOfMPAATriggerEvent.month" -> "4"),
                                                                ("dateOfMPAATriggerEvent.year" -> "2016"),
                                                                (P1_TRIGGER_DC_KEY -> "0"),
                                                                (P2_TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DATE_KEY -> ""),
                                                                (IS_EDIT_KEY -> "false"),
                                                                ("originalDate" -> "2015-5-4"))
        MockKeystore.map = MockKeystore.map + (TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe  Some("/paac/moneyPurchasePostTriggerValue")
      }

      "with Selected Year= '2016,2015', should not accept other than 2016/17 & 2015/16 dates & show invalid date error" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                                                              (IS_EDIT_KEY -> "false"),
                                                              (TE_YES_NO_KEY -> "true"),
                                                              (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                              (DC_FLAG_PREFIX_2016 -> "true"),(DC_FLAG_PREFIX_2015 -> "true"),
                                                              (SELECTED_INPUT_YEARS_KEY -> "2016,2015"))
        implicit val request = FakeRequest(POST, endPointURL).withSession(sessionData: _*)
                                                              .withSession((SessionKeys.sessionId,SESSION_ID))
                                                              .withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "5"),
                                                                ("dateOfMPAATriggerEvent.month" -> "4"),
                                                                ("dateOfMPAATriggerEvent.year" -> "2015"),
                                                                (P1_TRIGGER_DC_KEY -> "0"),
                                                                (P2_TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DC_KEY -> "0"),
                                                                (TRIGGER_DATE_KEY -> ""),
                                                                (IS_EDIT_KEY -> "false"),
                                                                ("originalDate" -> "2015-7-1"))
        MockKeystore.map = MockKeystore.map + (TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Enter a date between 6 4 2015 and 5 4 2017 inclusive")
      }
    }

    "onBack" should {
      "during edit return to review page" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                               (IS_EDIT_KEY -> "true"),
                               (TE_YES_NO_KEY -> "true"),
                               (CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"))
        val request = FakeRequest(GET,"").withSession(sessionData: _*)
        MockKeystore.map = MockKeystore.map + (IS_EDIT_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onBack()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/review")
      }

      "during edit return to yes/no page" in new ControllerWithMockKeystore {
        // set up
        val sessionData = List((SessionKeys.sessionId,SESSION_ID),
                               (IS_EDIT_KEY -> "false"),
                               (FIRST_DC_YEAR_KEY -> "2015"),
                               (TE_YES_NO_KEY -> "Yes"),
                               (s"${DC_FLAG_PREFIX}2015" -> "true"),
                               (CURRENT_INPUT_YEAR_KEY -> "2015"),
                               (SELECTED_INPUT_YEARS_KEY -> "2015"))
        val request = FakeRequest(GET,"").withSession(sessionData: _*)

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onBack()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnompaate")
      }
    }
  }

  "DateOfMPAATriggerEventForm" when {
    "on validate" should {
      "return false when year is negative" in new ControllerWithMockKeystore {
        DateOfMPAATriggerEventForm.form.bind(Map(
          "dateOfMPAATriggerEvent.day" -> "-1",
          "dateOfMPAATriggerEvent.month" -> "-1",
          "dateOfMPAATriggerEvent.year" -> "-1111"
        )).fold(
          formWithErrors => {
            formWithErrors.errors.find(_.key == "dateOfMPAATriggerEvent") should not be None
          },
          success =>
            success should not be Some(-1)
        )
      }
      "return false when year is short" in new ControllerWithMockKeystore {
        DateOfMPAATriggerEventForm.form.bind(Map(
          "dateOfMPAATriggerEvent.day" -> "-1",
          "dateOfMPAATriggerEvent.month" -> "-1",
          "dateOfMPAATriggerEvent.year" -> "9"
        )).fold(
          formWithErrors => {
            formWithErrors.errors.find(_.key == "dateOfMPAATriggerEvent") should not be None
          },
          success =>
            success should not be Some(-1)
        )
      }
    }
  }

  "TriggerDateModel" should {
    "toSessionData" must {
      "in edit mode reset trigger date" in {
        // set up
        val model = new TriggerDateModel() {
          def dateOfMPAATriggerEvent: Option[LocalDate] = Some(new LocalDate("2015-07-11"))
          def originalDate: String = "2015-04-12"
          def p1dctrigger: String = "100"
          def p2dctrigger: String = "0"
          def dctrigger: String = "0"
        }

        // test
        val result = model.toSessionData(true)

        // check
        result(0) shouldBe (("100","triggerDefinedContribution_2015_p2"))
        result(1) shouldBe (("0","triggerDefinedContribution_2015_p1"))
        result(2) shouldBe (("2015-07-11","dateOfMPAATriggerEvent"))
      }
      "in edit mode reset trigger date (p2)" in {
        // set up
        val model = new TriggerDateModel() {
          def dateOfMPAATriggerEvent: Option[LocalDate] = Some(new LocalDate("2015-04-11"))
          def originalDate: String = "2015-07-12"
          def p1dctrigger: String = "0"
          def p2dctrigger: String = "100"
          def dctrigger: String = "100"
        }

        // test
        val result = model.toSessionData(true)

        // check
        result(0) shouldBe (("100","triggerDefinedContribution_2015_p1"))
        result(1) shouldBe (("0","triggerDefinedContribution_2015_p2"))
        result(2) shouldBe (("2015-04-11","dateOfMPAATriggerEvent"))
      }
      "in edit mode reset trigger date when new date is in 2016" in {
        // set up
        val model = new TriggerDateModel() {
          def dateOfMPAATriggerEvent: Option[LocalDate] = Some(new LocalDate("2016-07-11"))
          def originalDate: String = "2015-04-12"
          def p1dctrigger: String = "100"
          def p2dctrigger: String = "0"
          def dctrigger: String = "0"
        }

        // test
        val result = model.toSessionData(true)

        // check
        result(0) shouldBe (("0","triggerDefinedContribution_2015_p1"))
        result(1) shouldBe (("0","triggerDefinedContribution_2015_p2"))
        result(2) shouldBe (("100","triggerDefinedContribution"))
        result(3) shouldBe (("2016-07-11","dateOfMPAATriggerEvent"))
      }
      "in edit mode return trigger date when new and old date is in 2016" in {
        // set up
        val model = new TriggerDateModel() {
          def dateOfMPAATriggerEvent: Option[LocalDate] = Some(new LocalDate("2016-07-11"))
          def originalDate: String = "2016-04-12"
          def p1dctrigger: String = "0"
          def p2dctrigger: String = "0"
          def dctrigger: String = "200"
        }

        // test
        val result = model.toSessionData(true)

        // check
        result(0) shouldBe (("2016-07-11","dateOfMPAATriggerEvent"))
      }
      "not editing return trigger date" in {
        // set up
        val model = new TriggerDateModel() {
          def dateOfMPAATriggerEvent: Option[LocalDate] = Some(new LocalDate("2016-07-11"))
          def originalDate: String = "2016-04-12"
          def p1dctrigger: String = "0"
          def p2dctrigger: String = "0"
          def dctrigger: String = "0"
        }

        // test
        val result = model.toSessionData(false)

        // check
        result(0) shouldBe (("2016-07-11","dateOfMPAATriggerEvent"))
      }
      "return emtpy list when no date" in {
        // set up
        val model = new TriggerDateModel() {
          def dateOfMPAATriggerEvent: Option[LocalDate] = None
          def originalDate: String = "2016-04-12"
          def p1dctrigger: String = "0"
          def p2dctrigger: String = "0"
          def dctrigger: String = "0"
        }

        // test
        val result = model.toSessionData(false)

        // check
        result.size shouldBe 0
      }
    }
  }
}
