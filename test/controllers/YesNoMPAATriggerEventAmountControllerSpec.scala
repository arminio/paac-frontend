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

import java.util.UUID
import org.scalatest.BeforeAndAfterAll
import play.api.Play
import play.api.mvc.{Result, Request}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, FakeApplication}
import service.KeystoreService
import service.KeystoreService._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future
import form._

class YesNoMPAATriggerEventAmountControllerSpec extends test.BaseSpec {
  val endPointURL = "/paac/yesnompaate"

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2015")
    MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2015")
    object MockYesNoMPAATriggerEventAmountControllerWithMockKeystore extends YesNoMPAATriggerEventAmountController with AppTestSettings {
      val yesNoKeystoreKey = "yesnoForMPAATriggerEvent"
      val yesNoFormKey = "yesNo"
      def keystore: KeystoreService = MockKeystore
    }
  }

  "YesNoMPAATriggerEventAmountController" when {
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
      "have keystore with no values and display Yes/NO option for MPAA Trigger Event Amount" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = MockYesNoMPAATriggerEventAmountControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
      }

      "have keystore with Yes/NO option for MPAA Trigger Event Amount value when we revisit the same page" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("yesnoForMPAATriggerEvent" -> "yes")

        // test
        val result : Future[Result] = MockYesNoMPAATriggerEventAmountControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        MockKeystore.map should contain key ("yesnoForMPAATriggerEvent")
        MockKeystore.map should contain value ("yes")
      }

    }

    "onSubmit with POST request" should {
      "not return result NOT_FOUND" in {
        val result: Option[Future[Result]] = route(FakeRequest(POST, endPointURL))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "return 303 for valid GET request" in {
        val result: Option[Future[Result]] = route(FakeRequest(POST, endPointURL))
        status(result.get) shouldBe 303
      }

      "with invalid yesno key name should show same form with errors and response code 200" in new ControllerWithMockKeystore{
        // set up
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (CURRENT_INPUT_YEAR_KEY,"2015"),
                                                                          (SELECTED_INPUT_YEARS_KEY,"2015"),
                                                                          (IS_EDIT_KEY,"false"))
          .withFormUrlEncodedBody("yesnoForMPAATriggerEvent" -> "Yes")

        // test
        val result: Future[Result] = MockYesNoMPAATriggerEventAmountControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
      }

      "with valid yesNo key and value should save yesnoMPAAATriggerEventAmount key to keystore" in new ControllerWithMockKeystore{
        // set up
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (CURRENT_INPUT_YEAR_KEY,"2015"),
                                                                          (SELECTED_INPUT_YEARS_KEY,"2015"),
                                                                          (IS_EDIT_KEY,"false")).withFormUrlEncodedBody("yesNo" -> "Yes")

        // test
        val result: Future[Result] = MockYesNoMPAATriggerEventAmountControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key ("yesnoForMPAATriggerEvent")
        MockKeystore.map should contain value ("Yes")
      }

      "with yesNo = Yes should forward to dateofmpaate" in new ControllerWithMockKeystore{
        // set up
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (CURRENT_INPUT_YEAR_KEY,"2015"),
                                                                          (SELECTED_INPUT_YEARS_KEY,"2015"),
                                                                          (IS_EDIT_KEY,"false")).withFormUrlEncodedBody(("yesNo" -> "Yes"))

        // test
        val result: Future[Result] = MockYesNoMPAATriggerEventAmountControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key ("yesnoForMPAATriggerEvent")
        MockKeystore.map should contain value ("Yes")
        redirectLocation(result) shouldBe Some("/paac/dateofmpaate")
      }

      "with yesNo = No should forward to review page" in new ControllerWithMockKeystore{
        // set up
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (CURRENT_INPUT_YEAR_KEY,"2015"),
                                                                          (SELECTED_INPUT_YEARS_KEY,"2015"),
                                                                          (IS_EDIT_KEY,"false")).withFormUrlEncodedBody(("yesNo" -> "No"))

        // test
        val result: Future[Result] = MockYesNoMPAATriggerEventAmountControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key ("yesnoForMPAATriggerEvent")
        MockKeystore.map should contain value ("No")
        redirectLocation(result) shouldBe Some("/paac/review")
      }

      "with yesNo = No should set trigger values to empty string" ignore new ControllerWithMockKeystore{
        // set up
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                          (CURRENT_INPUT_YEAR_KEY,"2015"),
                                                                          (SELECTED_INPUT_YEARS_KEY,"2015"),
                                                                          (IS_EDIT_KEY,"false")).withFormUrlEncodedBody(("yesNo" -> "No"))

        // test
        val result: Future[Result] = MockYesNoMPAATriggerEventAmountControllerWithMockKeystore.onSubmit()(request)

        // check
        MockKeystore.map.contains(KeystoreService.P1_TRIGGER_DC_KEY) shouldBe false
        MockKeystore.map.contains(KeystoreService.P2_TRIGGER_DC_KEY) shouldBe false
        MockKeystore.map.contains(KeystoreService.TRIGGER_DATE_KEY) shouldBe false
      }
    }

   "onBack" should {
      "redirect to yes/no threshold income page" in new ControllerWithMockKeystore {
        // set up
        implicit val request = FakeRequest(GET,"/paac/back").withSession((SessionKeys.sessionId,SESSION_ID),
                                                                         (CURRENT_INPUT_YEAR_KEY -> "2016"),
                                                                         (SELECTED_INPUT_YEARS_KEY -> "2016"))

        // test
        val result : Future[Result] = MockYesNoMPAATriggerEventAmountControllerWithMockKeystore.onBack()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnothresholdincome/2016")
      }
    }
  }

  "YesNoMPAATriggerEventForm" should {
    "correctly unbind" in {
      // set up
      val model = "Yes"
      val theForm = YesNoMPAATriggerEventForm.form.bind(Map("yesNo" -> "No"))

      // test
      val map = theForm.mapping.unbind(model)

      // check
      map("yesNo") shouldBe "Yes"
    }
  }
}
