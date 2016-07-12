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
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

class DateOfMPAATriggerEventControllerSpec extends test.BaseSpec {

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object ControllerWithMockKeystore extends DateOfMPAATriggerEventController {
      override val keystore: KeystoreService = MockKeystore
    }
  }

  "DateOfMPAATriggerEventControllerSpec" when {
    "onPageLoad" should {
      "display form page with no date if no keystore value" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "")
        MockKeystore.map = MockKeystore.map + (KeystoreService.P1_TRIGGER_DC_KEY -> "0")
        MockKeystore.map = MockKeystore.map + (KeystoreService.P2_TRIGGER_DC_KEY -> "0")
        MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")


        // do it
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""""""")
      }

      "display form page with date if keystore value exists" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-11-1")
        MockKeystore.map = MockKeystore.map + (KeystoreService.P1_TRIGGER_DC_KEY -> "0")
        MockKeystore.map = MockKeystore.map + (KeystoreService.P2_TRIGGER_DC_KEY -> "0")
        MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")

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
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, "/paac/d").withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "4"),
                                    ("dateOfMPAATriggerEvent.month" -> "7"),
                                    ("dateOfMPAATriggerEvent.year" -> "2015"),
                                    (KeystoreService.P1_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.P2_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.TRIGGER_DATE_KEY -> "2015-7-4"),
                                    ("originalDate" -> "2013-7-1"))
        MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")
        MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key (KeystoreService.TRIGGER_DATE_KEY)
        MockKeystore.map should contain value ("2015-07-04")
        redirectLocation(result) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
      }

      "with valid date redirect to moneyPurchasePostTriggerValue" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, "/paac/d").withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "4"),
                                    ("dateOfMPAATriggerEvent.month" -> "7"),
                                    ("dateOfMPAATriggerEvent.year" -> "2015"),
                                    (KeystoreService.P1_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.P2_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.TRIGGER_DATE_KEY -> "2015-7-4"),
                                    ("originalDate" -> "2013-7-1"))
        MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")
        MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
      }

      "with last date in before p1 show invalid error" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, "/paac/d").withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "5"),
                                    ("dateOfMPAATriggerEvent.month" -> "4"),
                                    ("dateOfMPAATriggerEvent.year" -> "2015"),
                                    (KeystoreService.P1_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.P2_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.TRIGGER_DATE_KEY -> "2015-4-5"),
                                    (KeystoreService.IS_EDIT_KEY -> "false"),
                                    ("originalDate" -> "2013-7-1"))
        MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("The date must fall within or after 2015")
      }

      "with no date show invalid error" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, "/paac/d").withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> ""),
                                    ("dateOfMPAATriggerEvent.month" -> ""),
                                    ("dateOfMPAATriggerEvent.year" -> ""),
                                    (KeystoreService.P1_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.P2_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.TRIGGER_DATE_KEY -> ""),
                                    (KeystoreService.IS_EDIT_KEY -> "false"),
                                    ("originalDate" -> "2013-7-1"))
        MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("You must specify a valid date")
      }

      "with bad date show invalid error" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, "/paac/d").withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "-1"),
                                    ("dateOfMPAATriggerEvent.month" -> "-1"),
                                    ("dateOfMPAATriggerEvent.year" -> "-1"),
                                    (KeystoreService.P1_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.P2_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.TRIGGER_DATE_KEY -> ""),
                                    (KeystoreService.IS_EDIT_KEY -> "false"),
                                    ("originalDate" -> "2013-7-1"))
        MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("You must specify a valid date")
      }

      "with valid date redirect to next trigger amount page if edit flag not set" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, "/paac/d").withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "4"),
                                    ("dateOfMPAATriggerEvent.month" -> "7"),
                                    ("dateOfMPAATriggerEvent.year" -> "2015"),
                                    (KeystoreService.P1_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.P2_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.TRIGGER_DATE_KEY -> "2013-7-1"),
                                    ("originalDate" -> "2013-7-1"))

        MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
      }

      "with valid date redirect to review page if edit flag set" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, "/paac/d").withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "4"),
                                    ("dateOfMPAATriggerEvent.month" -> "11"),
                                    ("dateOfMPAATriggerEvent.year" -> "2015"),
                                    (KeystoreService.P1_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.P2_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.TRIGGER_DATE_KEY -> "2013-7-1"),
                                    (KeystoreService.IS_EDIT_KEY -> "true"),
                                    ("originalDate" -> "2013-7-1"))

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/review")
      }

      "with invalid form display page again" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, "/paac/d").withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "4"),
                                    ("dateOfMPAATriggerEvent.month" -> "7"),
                                    ("dateOfMPAATriggerEvent.year" -> "2013"),
                                    (KeystoreService.P1_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.P2_TRIGGER_DC_KEY -> "0"),
                                    (KeystoreService.TRIGGER_DATE_KEY -> "2013-7-1"),
                                    (KeystoreService.IS_EDIT_KEY -> "false"),
                                    ("originalDate" -> "2013-7-1"))

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("The date must fall within or after 2015")
      }
    }

    "onBack" should {
      "during edit return to review page" in new ControllerWithMockKeystore {
        // set up
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "true")

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onBack()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/review")
      }

      "during edit return to yes/no page" in new ControllerWithMockKeystore {
        // set up
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")

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
}
